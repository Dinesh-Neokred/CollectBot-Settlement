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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadingSettlementData {

    private static final Logger logger = LogManager.getLogger(settlementApi.class);

    public int LastRowNumber;
    public String utr;
    public double rollingReserve;
    public String serviceProviderName;
    public double commissionGstAmount;
    public String serviceType;
    public double commissionAmount;
    public double settledAmount;
    public double collectedAmount;
    public String dateRange;
    public String programId;
    public String clientId;
    public String filePath;

    @Test
    public void setSettlementData(int number, String path) throws EncryptedDocumentException, IOException {
        try {
            baseUrlForClass url = new baseUrlForClass();
            String baseUrl = url.coreBaseUrl;

            filePath = path;
            FileInputStream fis = new FileInputStream(filePath);
            Workbook book = WorkbookFactory.create(fis);
            Sheet data = book.getSheetAt(0);
            LastRowNumber = data.getLastRowNum();

            logger.warn("Accessing clientId Value from Excel");
            clientId = getStringCellValue(data.getRow(1 + number).getCell(12));

            logger.warn("Accessing programId Value from Excel");
            programId = getStringCellValue(data.getRow(1 + number).getCell(13));

            logger.warn("Accessing dateRange Value from Excel");
            dateRange = getStringCellValue(data.getRow(1 + number).getCell(2));

            logger.warn("Accessing collectedAmount value from Excel");
            collectedAmount = getNumericCellValue(data.getRow(1 + number).getCell(3));

            logger.warn("Accessing settledAmount value from Excel");
            settledAmount = getNumericCellValue(data.getRow(1 + number).getCell(4));

            logger.warn("Accessing commissionAmount value from Excel");
            commissionAmount = getNumericCellValue(data.getRow(1 + number).getCell(8));

            logger.warn("Accessing commissionGstAmount value from Excel");
            commissionGstAmount = getNumericCellValue(data.getRow(1 + number).getCell(9));

            logger.warn("Accessing serviceProviderName value from Excel");
            serviceProviderName = getStringCellValue(data.getRow(1 + number).getCell(16));

            logger.warn("Accessing rollingReserve value from Excel");
            rollingReserve = getNumericCellValue(data.getRow(1 + number).getCell(17));

            logger.warn("Accessing utr value from Excel");
            utr = getStringCellValue(data.getRow(1 + number).getCell(11));

            logger.warn("Accessing serviceType value from Excel");
            serviceType = getStringCellValue(data.getRow(1 + number).getCell(20));

            logger.warn("Accessing value from Excel");

        } catch (EncryptedDocumentException | IOException | NullPointerException e) {
            logger.warn("Inside catch BLock of ReadingSettlementData class");
            clientId = "";
            programId = "";
            dateRange = "";
            collectedAmount = 0.01;
            settledAmount = 0.01;
            commissionAmount = 0.01;
            commissionGstAmount = 0.01;
            serviceProviderName = "";
            rollingReserve = 0.01;
            utr = "";
            serviceType = "";
            e.printStackTrace();
        }
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