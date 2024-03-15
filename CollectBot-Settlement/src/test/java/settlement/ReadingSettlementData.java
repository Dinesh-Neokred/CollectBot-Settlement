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

        } catch (EncryptedDocumentException | IOException | NullPointerException e) {

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