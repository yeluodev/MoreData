package club.moredata.task;

import club.moredata.entity.Account;
import club.moredata.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yeluodev1226
 */
public class RedisTask {

    /**
     * DB0-抓取队列数据库
     * DB1-等待抓取的用户数据库
     * DB2-已完成抓取或无需抓取的用户数据库
     * DB3-用户缓冲队列数据库
     * DB4-组合更新队列数据库
     * DB5-组合调仓次数数据库
     */

    public static final String KEY_ACCOUNT_UNFINISHED = "UnfinishedAccount";
    public static final String KEY_ACCOUNT_FINISHED = "FinishedAccount";
    public static final String KEY_ACCOUNT_CACHED = "CachedAccount";
    public static final String KEY_URLS_PENDING = "PendingUrls";
    public static final String KEY_URLS_FETCHING = "FetchingUrls";
    public static final String KEY_URLS_CACHED = "CachedUrls";
    public static final String KEY_URLS_FAIL = "FailUrls";
    public static final String KEY_REQUEST_REMAINING = "RemainingRequestCount";

    /**
     * 添加用户到缓冲
     *
     * @param account
     * @param cache
     */
    public static void addAccount(Account account, boolean cache) {
        String uid = String.valueOf(account.getId());
        Jedis jedis = RedisUtil.getJedis();
        String key;
        if (cache) {
            key = KEY_ACCOUNT_CACHED;
        } else {
            key = account.getFollowersCount() == 0 ? KEY_ACCOUNT_FINISHED : KEY_ACCOUNT_UNFINISHED;
        }
        jedis.zadd(key, account.getFollowersCount(), uid);
        jedis.close();
    }

    /**
     * 添加用户到缓冲
     *
     * @param accountList
     * @param cache
     * @param finished
     */
    public static void addAccounts(List<Account> accountList, boolean cache, boolean finished) {
        Map<String, Double> accountMap = new HashMap<>(accountList.size());
        accountList.forEach(account -> accountMap.put(String.valueOf(account.getId()), (double) account.getFollowersCount()));
        Jedis jedis = RedisUtil.getJedis();
        String key;
        if (cache) {
            key = KEY_ACCOUNT_CACHED;
        } else {
            key = finished ? KEY_ACCOUNT_FINISHED : KEY_ACCOUNT_UNFINISHED;
        }
        jedis.zadd(key, accountMap);
        jedis.close();
    }

    /**
     * 返回未完成用户中前1000位用户，粉丝数从大到小
     *
     * @return
     */
    public static Set<Tuple> listUnfinishedAccount() {
        Jedis jedis = RedisUtil.getJedis();
        Set<Tuple> accountSet = jedis.zrevrangeByScoreWithScores(KEY_ACCOUNT_UNFINISHED, "+inf", "-inf", 0, 1000);
        jedis.close();
        return accountSet;
    }

    /**
     * 转移用户
     *
     * @param account
     * @param source
     * @param destination
     */
    public static void moveAccount(Account account, String source, String destination) {
        Jedis jedis = RedisUtil.getJedis();
        String uid = String.valueOf(account.getId());
        jedis.zrem(source, uid);
        jedis.zadd(destination, account.getFollowersCount(), uid);
        jedis.close();
    }

    /**
     * 转移用户，由CachedAccount到FinishedAccount/UnfinishedAccount
     *
     * @param account
     */
    public static void moveAccountFromCachedToOther(Account account) {
        moveAccount(account, KEY_ACCOUNT_CACHED, account.getFollowersCount() > 0 ? KEY_ACCOUNT_UNFINISHED : KEY_ACCOUNT_FINISHED);
    }

    /**
     * 转移用户，由CachedAccount到FinishedAccount/UnfinishedAccount
     *
     * @param accountList
     */
    public static void moveAccountFromCachedToOther(List<Account> accountList) {
        accountList.forEach(RedisTask::moveAccountFromCachedToOther);
    }

    /**
     * 转移用户，由UnfinishedAccount到FinishedAccount
     *
     * @param account
     */
    public static void moveAccountFromUnfinishedToFinished(Account account) {
        moveAccount(account,KEY_ACCOUNT_UNFINISHED,KEY_ACCOUNT_FINISHED);
    }

    /**
     * 添加用户粉丝列表第一页请求地址，以及该用户剩余请求数
     *
     * @param account
     */
    public static void addAccountPendingUrlAndRemainingRequest(Tuple account) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.zadd(KEY_REQUEST_REMAINING, 1, account.getElement());
        jedis.zadd(KEY_URLS_PENDING, account.getScore(), "uid=" + account.getElement() + "&pageNo=1");
        jedis.close();
    }

    /**
     * 返回待请求地址列表，一次抓取300条
     *
     * @return
     */
    public static Set<Tuple> listPendingUrls() {
        Jedis jedis = RedisUtil.getJedis();
        Set<Tuple> urlSet = jedis.zrevrangeByScoreWithScores(KEY_URLS_PENDING, "+inf", "-inf", 0, 1000);
        urlSet.forEach(tuple -> {
            //url由pendingUrls转移到fetchingUrls
            jedis.zrem(KEY_URLS_PENDING, tuple.getElement());
            jedis.zadd(KEY_URLS_FETCHING, tuple.getScore(), tuple.getElement());
        });
        jedis.close();
        return urlSet;
    }

    /**
     * 移动url
     *
     * @param url
     * @param source
     * @param destination
     * @param score
     */
    public static void moveUrl(String url, String source, String destination, int score) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.zrem(source, url);
        jedis.zadd(destination, score, url);
        jedis.close();
    }

    /**
     * url PendingUrls-->CachedUrls
     *
     * @param url
     * @param followers
     */
    public static void moveUrlFromFetchingToCached(String url, int followers) {
        moveUrl(url, KEY_URLS_FETCHING, KEY_URLS_CACHED, followers);
    }

    /**
     * url PendingUrls-->FailUrls
     *
     * @param url
     * @param followers
     */
    public static void moveUrlFromFetchingToFail(String url, int followers) {
        moveUrl(url, KEY_URLS_FETCHING, KEY_URLS_FAIL, followers);
    }

    /**
     * url CachedUrls-->FailUrls
     *
     * @param url
     * @param followers
     */
    public static void moveUrlFromCachedToFail(String url, int followers) {
        moveUrl(url, KEY_URLS_CACHED, KEY_URLS_FAIL, followers);
    }

    /**
     * 移除缓存url
     * @param url
     */
    public static void removeUrlFromCached(String url){
        remove(KEY_URLS_CACHED,url);
    }

    /**
     * 添加url列表
     *
     * @param key
     * @param urls
     * @param score
     */
    public static void addUrls(String key, List<String> urls, int score) {
        Map<String, Double> urlMap = new HashMap<>(urls.size());
        urls.forEach(url -> urlMap.put(url, (double) score));
        Jedis jedis = RedisUtil.getJedis();
        jedis.zadd(key, urlMap);
        jedis.close();
    }

    /**
     * 添加url列表到PendingUrls
     *
     * @param urls
     * @param score
     */
    public static void addUrlsToPending(List<String> urls, int score) {
        addUrls(KEY_URLS_PENDING, urls, score);
    }

    /**
     * 更新剩余请求数
     *
     * @param uid
     * @param count
     */
    public static void setRemainingRequestCount(String uid, int count) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.zadd(KEY_REQUEST_REMAINING, count, uid);
        jedis.close();
    }

    /**
     * 剩余请求数减1
     *
     * @param uid
     */
    public static void decrRemainingRequest(String uid) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.zincrby(KEY_REQUEST_REMAINING, -1, uid);
        jedis.close();
    }

    /**
     * 移除成员
     *
     * @param key
     * @param member
     */
    public static void remove(String key, String member) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.zrem(key, member);
        jedis.close();
    }

    /**
     * 剩余请求数为0，移除掉
     *
     * @param uid
     */
    public static void removeRemainingRequest(String uid) {
        remove(KEY_REQUEST_REMAINING, uid);
    }

    /**
     * 获取成员分数
     *
     * @param key
     * @param member
     * @return
     */
    public static Double getScore(String key, String member) {
        Jedis jedis = RedisUtil.getJedis();
        Double score = jedis.zscore(key, member);
        jedis.close();
        return score;
    }

    /**
     * 获取指定用户的剩余请求数
     *
     * @param uid
     * @return
     */
    public static int getRemainingRequestCount(String uid) {
        Double count = getScore(KEY_REQUEST_REMAINING, uid);
        return count == null ? 0 : count.intValue();
    }


}
