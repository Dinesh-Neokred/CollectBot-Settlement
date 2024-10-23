package settlement;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.restassured.response.Response;

public class StatusCheck {
    private static final Logger logger = LoggerFactory.getLogger(StatusCheck.class);
    public static String filePath;
    public static FileInputStream fis;
    public static Workbook book;
    public Sheet sheetValue;
    public String client_secret;
    public String program_id;
    public String transactionId;

    @Test
    public void checkStatus() throws IOException {
        int LastRowNumber = 1;
        for (int i = 0; i < LastRowNumber; i++) {
            logger.info("Processing data for row: " + i);
            try {
                filePath = "C:\\Users\\Dinesh M\\Downloads\\DREAM.xlsx";
                fis = new FileInputStream(filePath);
                book = WorkbookFactory.create(fis);
                Sheet data = book.getSheetAt(0);
                LastRowNumber = data.getLastRowNum();
                sheetValue = data;

                client_secret = getStringCellValue(data.getRow(1 + i).getCell(0));
                program_id = getStringCellValue(data.getRow(1 + i).getCell(1));
                transactionId = getStringCellValue(data.getRow(1 + i).getCell(2));
            } catch (EncryptedDocumentException | IOException | NullPointerException e) {
                client_secret = "";
                program_id = "";
                transactionId = "";
            }

            if (client_secret.isEmpty()) {
                logger.warn("client_secret is empty, Executing Next Line.");
                continue;
            }
            if (program_id.isEmpty()) {
                logger.warn("program_id is empty, Executing Next Line.");
                continue;
            }
            if (transactionId.isEmpty()) {
                logger.warn("transactionId is empty, Executing Next Line.");
                continue;
            }

            // Ensure the transactionId is included in the requestPayload
            HashMap<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("transactionId", transactionId);

            Response response = given()
                    .headers("client_secret", client_secret)
                    .header("program_id", program_id)
                    .contentType("application/json")
                    .body(requestPayload)
                    .when()
                    .post("https://collectbot.neokred.tech/payin/fn/api/v1/external/upi/qr/status");

            logger.warn("API Response For The Transaction ID " + transactionId + "=============>" + response.then().log().all());
            response.then().log().all();
            sheetValue.getRow(1 + i).getCell(3).setCellValue(response.jsonPath().getString("message"));

            logger.info("Status Check processing completed for transaction ID: " + transactionId);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                book.write(fos);
            }
            book.close();
        }
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }
}
