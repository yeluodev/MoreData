package club.moredata.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author yeluodev1226
 */
public class ExcelUtil {

    public static void generateExcel(List<String> header, List<List<String>> body, OutputStream outputStream) {
        HSSFWorkbook excel = new HSSFWorkbook();
        HSSFSheet sheet = excel.createSheet();
        HSSFRow headerRow = sheet.createRow(0);
        for (int columnNum = 0; columnNum < header.size(); columnNum++) {
            HSSFCell cell = headerRow.createCell(columnNum);
            cell.setCellValue(header.get(columnNum));
        }

        for (int rowNum = 0; rowNum < body.size(); rowNum++) {
            HSSFRow row = sheet.createRow(rowNum + 1);
            List<String> data = body.get(rowNum);
            for (int columnNum = 0; columnNum < data.size(); columnNum++) {
                HSSFCell cell = row.createCell(columnNum);
                cell.setCellValue(data.get(columnNum));
            }
        }

        try {
            excel.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        try {
//            AnalysisTask task = new AnalysisTask();
//            LeekResult<AlsStock> stockLeekResult = task.stockRankList(1, 100, 3000, false, OrderType.WEIGHT_DESC);
//            String fileName = "stock_" + stockLeekResult.getCount();
//            List<String> header = new ArrayList<>();
//            header.add("序号");
//            header.add("名称");
//            header.add("代码");
//            header.add("所属板块");
//            header.add("持有组合数");
//            header.add("比重/%");
//            header.add("满仓比重/%");
//            List<List<String>> body = new ArrayList<>();
//            for (AlsStock stock : stockLeekResult.getList()) {
//                List<String> rowBody = new ArrayList<>();
//                rowBody.add(String.valueOf(stock.getRank()));
//                rowBody.add(String.valueOf(stock.getStockName()));
//                rowBody.add(String.valueOf(stock.getStockSymbol()));
//                rowBody.add(String.valueOf(stock.getSegmentName()));
//                rowBody.add(String.valueOf(stock.getCount()));
//                rowBody.add(String.valueOf(stock.getPercentWithCash()));
//                rowBody.add(String.valueOf(stock.getPercent()));
//                body.add(rowBody);
//            }
//            OutputStream outputStream = new FileOutputStream("D:/excel/"+fileName+".xls");
//            ExcelUtil.generateExcel(header,body,outputStream);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        String str = "2019-6-10 12:57:11";
        str = str.replaceAll("-|:| ", "");
        System.out.println(str);
    }

}
