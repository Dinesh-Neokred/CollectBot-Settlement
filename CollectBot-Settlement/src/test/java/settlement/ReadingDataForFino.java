package settlement;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadingDataForFino {

    private static final Logger logger = LogManager.getLogger(ReadingDataForFino.class);

    public int LastRowNumber;

    public String filePath;

    public String CBS_Ref_Num;
    public String TXN_ID;
    public String CustomerVPA;
    public String CustomerName;
    public String CustomerIFSC;
    public String CustomerAccNum;
    public String merchantVpa;
    public String merchantName;
    public String customerAccountType;
    public String TransactionDateTime;
    public String transactionId;

    public double amount;
    public long utr; // UTR defined as a long for numeric values
    public long customerMobileNumber;

    public void setSettlementData(int number, String path) {
        try {
            filePath = path;
            FileInputStream fis = new FileInputStream(filePath);
            Workbook book = WorkbookFactory.create(fis);
            Sheet data = book.getSheetAt(0);
            LastRowNumber = data.getLastRowNum();

            logger.warn("Accessing data from Excel");
            if (number >= LastRowNumber) {
                logger.warn("Row number exceeds the limit.");
                return;
            }

            amount = getNumericCellValue(data.getRow(1 + number).getCell(9));
            utr = (long) getNumericCellValue(data.getRow(1 + number).getCell(15));
            TXN_ID = getStringCellValue(data.getRow(1 + number).getCell(13));
            CustomerVPA = getStringCellValue(data.getRow(1 + number).getCell(5));
            CustomerName = getStringCellValue(data.getRow(1 + number).getCell(4));
            customerMobileNumber = (long) getNumericCellValue(data.getRow(1 + number).getCell(6));
            merchantVpa = getStringCellValue(data.getRow(1 + number).getCell(7));
            merchantName = getStringCellValue(data.getRow(1 + number).getCell(1));
            TransactionDateTime = getDateCellValue(data.getRow(1 + number).getCell(3));
            transactionId = getStringCellValue(data.getRow(1 + number).getCell(16));

            CBS_Ref_Num = "UC" + utr + TXN_ID.substring(0, 3);

        } catch (EncryptedDocumentException | IOException | NullPointerException e) {
            logger.error("Exception occurred while reading settlement data", e);
            resetValues();
        }
    }

    private void resetValues() {
        TXN_ID = "";
        CustomerVPA = "";
        CustomerName = "";
        amount = 0.0;
        CustomerIFSC = "";
        CustomerAccNum = "";
        merchantVpa = "";
        merchantName = "";
        customerAccountType = "";
        TransactionDateTime = "";
        transactionId = "";
        utr = 0L;
        customerMobileNumber = 0L;
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }

    private double getNumericCellValue(Cell cell) {
        return (cell != null && (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA))
                ? cell.getNumericCellValue()
                : 0.0;
    }

    private String getDateCellValue(Cell cell) {
        try {
            if (cell != null && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                // Format the date to ISO 8601 with timezone offset
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                return sdf.format(date);
            }
        } catch (Exception e) {
            logger.error("Error parsing date cell", e);
        }
        return "";
    }
}
