package club.moredata.task;

import club.moredata.api.ApiCallback;
import club.moredata.api.AsyncApi;
import club.moredata.api.SyncApi;
import club.moredata.db.DbHelper;
import club.moredata.entity.Account;
import club.moredata.entity.FollowerResult;
import com.google.gson.Gson;
import okhttp3.Response;
import redis.clients.jedis.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 爬虫任务
 *
 * @author yeluodev1226
 */
public class CrawlTask {

    /*
        流程
       1、种子用户
            插入mysql数据库user表
            插入redis数据库UnfinishedAccount     zadd uid-followers
       2、添加请求队列
            查询redis中UnfinishedAccount，根据设置的大小，获取到uid的list
            遍历uid list，生成url形如uid=xxxxxx&pageNo=1
            插入redis数据库PendingUrls           zadd url-followers
            插入redis数据库RemainingRequestCount zadd uid-1
       3、实际爬取
            查询redis中PendingUrls，根据设置的大小，获取到url的list
            遍历url list，将其从PendingUrls转移到FetchingUrls
            发起请求
                success:
                    得到accountList，遍历插入redis数据库CachedAccount  zadd uid-followers
                    将本次请求地址url从FetchingUrls转移到CachedUrls
                        page=1:
                            更新mysql数据库user表，page、maxPage、anonymous_count字段
                            maxPage>1:
                                生成url list,pageNo=2到pageNo=maxPage，插入redis数据库PendingUrls    zadd url-followers
                                修正redis数据库RemainingRequestCount，uid-(maxPage-1)
                            maxPage<=1:
                                将uid从redis数据库RemainingRequestCount中移除
                        page>1:
                            查询redis数据库RemainingRequestCount中uid的score值，即剩余待请求数
                            score<=1:
                                本次为该uid的最后一次请求，抓取完毕，从RemainingRequestCount中移除
                                更新mysql数据库user表，page、maxPage、anonymous_count、finished字段
                            score>1:
                                score--，更新RemainingRequestCount中uid分数
                fail：
                    将url从PendingUrls转移到FailUrls
            上传用户列表数据
                遍历accountList，插入mysql数据库user表
                    success:
                        转移用户由CachedAccount到UnfinishedAccount（followers>0）/ FinishedAccount(followers=0)
                        将url从CachedUrls中移除
                    fail:
                        url从CachedUrls转移至FailUrls

            重复上述过程
     */

    /**
     * 种子用户，'玩赚组合'--粉丝数最多18229649
     */
    public void fetchSourceAccount() {
        AsyncApi.getInstance().getAccount("5171159182", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Account account = gson.fromJson(response, Account.class);
                //插入user表
                DbHelper.addAccount(account);
                //插入redis队列
                RedisTask.addAccount(account, false);
            }
        });
    }

    public static void main(String[] args) {
        CrawlTask task = new CrawlTask();
//        task.fetchSourceAccount();
//        task.addAccountPendingUrls();
        task.startFetchFollowerList();
    }

    /**
     * 用户待抓取队列添加
     */
    public void addAccountPendingUrls() {
        System.out.println("pendingUrls队列添加");
        Set<Tuple> accountSet = RedisTask.listUnfinishedAccount();
        accountSet.forEach(RedisTask::addAccountPendingUrlAndRemainingRequest);
    }

    /**
     * 修正待抓取队列 TODO
     */
    public void repairPendingUrls() {

    }

    /**
     * 抓取url列表，遍历请求
     */
    public void startFetchFollowerList() {
        Set<Tuple> reqUrlSet = RedisTask.listPendingUrls();
        reqUrlSet.forEach(this::fetchFollowerList);
    }

    /**
     * 请求粉丝列表
     *
     * @param tuple
     */
    public void fetchFollowerList(Tuple tuple) {
        String url = tuple.getElement();
        String uid = url.split("&")[0].split("=")[1];
        try {
            Response response = SyncApi.getInstance().fetchFollowerList(url);
            String responseStr = response.body().string();
            int code = response.code();
            if (code >= 200 && code < 300) {
                Gson gson = new Gson();
                FollowerResult result = gson.fromJson(responseStr, FollowerResult.class);
                System.out.println(String.format("[uid-%s, count-%d, page-%d, maxPage-%d, anonymous-%d]", uid,
                        result.getCount(), result.getPage(), result.getMaxPage(), result.getAnonymousCount()));

                RedisTask.addAccounts(result.getFollowers(), true, false);
                RedisTask.moveUrlFromFetchingToCached(url, result.getCount());

                if (result.getPage() == 1) {
                    DbHelper.updateAccount(uid, result.getCount(), 1, result.getMaxPage(), result.getAnonymousCount());
                    if (result.getMaxPage() > 1) {
                        List<String> urlList = new ArrayList<>();
                        for (int i = 2; i < result.getMaxPage(); i++) {
                            urlList.add("uid=" + uid + "&pageNo=" + i);
                        }
                        RedisTask.addUrlsToPending(urlList, result.getCount());
                        RedisTask.setRemainingRequestCount(uid, result.getMaxPage() - 1);
                    } else {
                        RedisTask.removeRemainingRequest(uid);
                    }
                } else {
                    int count = RedisTask.getRemainingRequestCount(uid);
                    if (count <= 1) {
                        RedisTask.removeRemainingRequest(uid);
                        Account account = new Account();
                        account.setId(Long.valueOf(uid));
                        account.setFollowersCount(result.getCount());
                        RedisTask.moveAccountFromUnfinishedToFinished(account);
                        DbHelper.updateAccount(uid, result.getCount(), result.getMaxPage(), result.getMaxPage(), result.getAnonymousCount());
                    } else {
                        RedisTask.decrRemainingRequest(uid);
                    }
                }

                uploadAccount(result.getFollowers(), url, (int) tuple.getScore());
            } else {
                System.out.println("接口请求发生错误" + response.code());
                RedisTask.moveUrlFromFetchingToFail(url, (int) tuple.getScore());
            }
        } catch (IOException e) {
            e.printStackTrace();
            RedisTask.moveUrlFromFetchingToFail(url, (int) tuple.getScore());
        }

    }

    /**
     * 数据上传，插入mysql数据库user表
     *
     * @param accountList
     * @param url
     * @param followers
     */
    public void uploadAccount(List<Account> accountList, String url, int followers) {
        boolean success = DbHelper.addAccountList(accountList);
        if (success) {
            RedisTask.moveAccountFromCachedToOther(accountList);
            RedisTask.removeUrlFromCached(url);
        } else {
            RedisTask.moveUrlFromFetchingToCached(url, followers);
        }
    }

}
