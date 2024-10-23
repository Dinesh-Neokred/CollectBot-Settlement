package VoucherCallback;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Date;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import settlement.settlementApi;
import utils.ConfigLoader;

public class VoucherStatusCheckAPI {
    private static final Logger logger = LoggerFactory.getLogger(settlementApi.class);
    public static String filePath;
    public static FileInputStream fis;
    public static Workbook book;
    public String client_secret;
    public String program_id;
    public String transactionId;
    public String txnType;
    public Sheet sheetValue;

    @SuppressWarnings("unchecked")
    @Test
    public void statusCheck() throws IOException {
        HashMap<String, Object> requestBody = new HashMap<>();

        int LastRowNumber = 1;
        for (int i = 0; i < LastRowNumber; i++) {
            logger.info("Processing data for row: " + i);

            try {
                filePath = "C:\\Users\\Dinesh M\\Downloads\\status Check.xlsx";
                fis = new FileInputStream(filePath);
                book = WorkbookFactory.create(fis);
                Sheet data = book.getSheetAt(0);
                LastRowNumber = data.getLastRowNum();
                sheetValue = data;

                client_secret = getStringCellValue(data.getRow(1 + i).getCell(0));
                program_id = getStringCellValue(data.getRow(1 + i).getCell(1));
                txnType = getStringCellValue(data.getRow(1 + i).getCell(2)).toLowerCase();
                transactionId = getStringCellValue(data.getRow(1 + i).getCell(3));
                requestBody.put("transactionId", transactionId);
                requestBody.put("txnType", txnType);

            } catch (EncryptedDocumentException | IOException | NullPointerException e) {
                client_secret = "";
                program_id = "";
                transactionId = "";
                txnType = "";
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
            if (txnType.isEmpty()) {
                logger.warn("txnType is empty, Executing Next Line.");
                continue;
            }

            RequestSpecification requestHeaders = given()
                .header("client_secret", client_secret)
                .header("program_id", program_id)
                .contentType("application/json")
                .body(requestBody);
            Response response = requestHeaders.when().post("https://collectbot.neokred.tech/payin/fn/api/v1/external/upi/coupon/status");
            logger.warn("API Response For The Transaction ID " + transactionId + "=============>" + response.then().log().all());

            String responseMessage = response.jsonPath().getString("message");
            sheetValue.getRow(1 + i).getCell(4).setCellValue(responseMessage);

            if (responseMessage.equalsIgnoreCase("UPI Status Fetched successful")) {
                String upiId = response.jsonPath().getString("data.upiId");
                Object amountObject = response.jsonPath().get("data.amount");
                int amount = 0;
                if (amountObject instanceof Integer) {
                    amount = (Integer) amountObject;
                } else if (amountObject instanceof String) {
                    amount = Integer.parseInt((String) amountObject);
                }
                String custRefNo = response.jsonPath().getString("data.custRefNo");
                String upiTxnId = response.jsonPath().getString("data.upiTxnId");
                String orderId = response.jsonPath().getString("data.orderId");
                String txnStatus = response.jsonPath().getString("data.txnStatus");

                sheetValue.getRow(1 + i).getCell(5).setCellValue(upiId);
                sheetValue.getRow(1 + i).getCell(6).setCellValue(amount);
                sheetValue.getRow(1 + i).getCell(7).setCellValue(custRefNo);
                sheetValue.getRow(1 + i).getCell(9).setCellValue(upiTxnId);
                sheetValue.getRow(1 + i).getCell(10).setCellValue(orderId);
                sheetValue.getRow(1 + i).getCell(11).setCellValue(txnStatus);
                Date date = getPaymentDate(transactionId);
                // sheetValue.getRow(1 + i).getCell(16).setCellValue(date);
            }
            logger.info("Status Check processing completed for transaction ID: " + transactionId);
            FileOutputStream fos = new FileOutputStream(filePath);
            book.write(fos);
            book.close();
        }
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }

    public Date getPaymentDate(String transactionId) {
        MongoClient mongoClient = MongoClients.create(ConfigLoader.getDb());
        MongoDatabase database = mongoClient.getDatabase(ConfigLoader.getFinoSubCore());
        MongoCollection<Document> collection = database.getCollection("transactions");
        Document query = new Document("transactionId", transactionId);
        Document result = collection.find(query).first();
        if (result != null) {
            Date modifiedDate = result.getDate("modified_at");
            return modifiedDate;
        } else {
            throw new IllegalArgumentException("Transaction ID not found: " + transactionId);
        }
    }
}
