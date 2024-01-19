package settlement;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    public static double commissionAmount;
    public static double settledAmount;
    public static double collectedAmount;
    public static String dateRange;
    public static String programId;
    public static String clientId;
    public static String filePath;

    @Test
    public void setSettlementData(int number, String path) throws EncryptedDocumentException, IOException {
        String baseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";
        filePath = path;
        FileInputStream fis = new FileInputStream(filePath);
        Workbook book = WorkbookFactory.create(fis);
        Sheet data = book.getSheet("userID");
        LastRowNumber = data.getLastRowNum();
        System.out.println(number);

        clientId = data.getRow(1 + number).getCell(12).getStringCellValue();
        programId = data.getRow(1 + number).getCell(13).getStringCellValue();
        dateRange = data.getRow(1 + number).getCell(2).getStringCellValue();
        collectedAmount = data.getRow(1 + number).getCell(3).getNumericCellValue();
        settledAmount = data.getRow(1 + number).getCell(4).getNumericCellValue();
        commissionAmount = data.getRow(1 + number).getCell(8).getNumericCellValue();
        commissionGstAmount = data.getRow(1 + number).getCell(9).getNumericCellValue();
        serviceProviderName = data.getRow(1 + number).getCell(16).getStringCellValue();
        rollingReserve = data.getRow(1 + number).getCell(17).getNumericCellValue();
        utr = data.getRow(1 + number).getCell(11).getStringCellValue();
        System.out.println(utr);
    }

}
