package settlement;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.ConfigLoader;

public class TransactionUpdate {

    private static final Logger logger = LoggerFactory.getLogger(TransactionUpdate.class); // Logger initialization

    @Test
    public void statusUpdate() throws EncryptedDocumentException, IOException {
        logger.info("Starting status update process.");

        String filePath = "/Users/Dinesh/Downloads/Some status pendinf updated.xlsx"; // Update the path
                                                                           // accordingly

        // Try-with-resources to ensure proper closure of resources
        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = WorkbookFactory.create(fis);
                MongoClient mongoClient = MongoClients.create(ConfigLoader.getDb())) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            int lastRowNumber = sheet.getLastRowNum();
            logger.info("Total number of rows to process: {}", lastRowNumber);

            MongoDatabase database = mongoClient.getDatabase(ConfigLoader.getEqPayoutSubCore());
            MongoCollection<Document> collection = database.getCollection("transactions");

            // Process each row in the Excel sheet
            for (int i = 1; i <= lastRowNumber; i++) { // Skip header row
                Row row = sheet.getRow(i);

                String transferID = getStringCellValue(row.getCell(2));
                String rrn = getNumericCellValueAsString(row.getCell(5));
                String status = getStringCellValue(row.getCell(7)).toUpperCase();

                logger.info("Processing row {}: CNVID={}, RRN={}, Status={}", i, transferID, rrn, status);

                // Update MongoDB with the extracted data
                dbUpdate(collection, transferID, rrn, status);
            }

        } catch (Exception e) {
            logger.error("Error during status update process: {}", e.getMessage());
        }

        logger.info("Status update process completed.");
    }

    private String getNumericCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            return "0";
        }
        return String.valueOf((long) cell.getNumericCellValue());
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }

    private void dbUpdate(MongoCollection<Document> collection, String transferID, String rrn, String status) {
        logger.debug("Updating MongoDB document for transferID: {}", transferID);

        try {
            Document query = new Document("transferId", transferID);

            // Map status to predefined values
            status = mapStatus(status);

            BsonDateTime currentTime = new BsonDateTime(Instant.now().toEpochMilli());

            Document update = new Document("$set", new Document("status", status)
                    .append("modified_at", currentTime));

            // Include UTR if RRN is valid
            if (!"0".equals(rrn)) {
                update.get("$set", Document.class).append("utr", rrn);
            }

            collection.findOneAndUpdate(query, update);
            logger.info("Successfully updated document for transferID: {}", transferID);

        } catch (Exception e) {
            logger.error("Error updating document for transferID: {}. Error: {}", transferID, e.getMessage());
        }
    }

    // Method to map status to the corresponding predefined values
    private String mapStatus(String status) {
        switch (status) {
            case "TRANSACTION CREDIT CONFIRMATION":
            case "SUCCESS":
                return "SUCCESS";
            case "NO HITS":
                return "FAILED";
            case "STATUS PENDING":
                return "DEEMED";
            default:
                return "FAILED";
        }
    }
}
