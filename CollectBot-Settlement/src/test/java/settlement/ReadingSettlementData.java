package settlement;

import org.apache.poi.ss.usermodel.CellType;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;

public class ReadingSettlementData {
    public static int LastRowNumber;
    public static String utr;
    public static double rollingReserve;
    public static String serviceProviderName;
    public static double commissionGstAmount;
    public static String serviceType;
    public static double commissionAmount;
    public static double settledAmount;
    public static double collectedAmount;
    public static String dateRange;
    public static String programId;
    public static String clientId;
    public static String filePath;

    @Test
    public void setSettlementData(int number, String path) throws EncryptedDocumentException, IOException {
        baseUrlForClass url = new baseUrlForClass();
        String baseUrl = url.coreBaseUrl;

        filePath = path;
        FileInputStream fis = new FileInputStream(filePath);
        Workbook book = WorkbookFactory.create(fis);
        Sheet data = book.getSheet("Sheet1");
        LastRowNumber = data.getLastRowNum();
        System.out.println(number);

        clientId = getStringCellValue(data.getRow(1 + number).getCell(12));
        programId = getStringCellValue(data.getRow(1 + number).getCell(13));
        dateRange = getStringCellValue(data.getRow(1 + number).getCell(2));
        collectedAmount = getNumericCellValue(data.getRow(1 + number).getCell(3));
        settledAmount = getNumericCellValue(data.getRow(1 + number).getCell(4));
        commissionAmount = getNumericCellValue(data.getRow(1 + number).getCell(8));
        commissionGstAmount = getNumericCellValue(data.getRow(1 + number).getCell(9));
        serviceProviderName = getStringCellValue(data.getRow(1 + number).getCell(16));
        rollingReserve = getNumericCellValue(data.getRow(1 + number).getCell(17));
        utr = getStringCellValue(data.getRow(1 + number).getCell(11));
        serviceType = getStringCellValue(data.getRow(1 + number).getCell(20));
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";

    }

    private double getNumericCellValue(Cell cell) {

        return (cell != null && cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA)
                ? cell.getNumericCellValue()
                : 0.1;
    }
}