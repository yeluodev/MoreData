package club.moredata.util;

import java.util.Arrays;

public class Util {

    /**
     * 将cubeIds参数转换为引号模式
     * @param cubeIds
     * @return
     */
    public static String dealCubeIds(String cubeIds){
        String[] idArr = cubeIds.replaceAll(" ","").split(",");
        StringBuilder sb = new StringBuilder();
        Arrays.asList(idArr).forEach(id-> sb.append("'").append(id).append("',"));
        String idsWithQuote = sb.toString();
        idsWithQuote = idsWithQuote.substring(0,idsWithQuote.length()-1);
        return idsWithQuote;
    }
}
