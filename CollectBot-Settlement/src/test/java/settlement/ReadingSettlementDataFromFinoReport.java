package settlement;

import org.apache.poi.ss.usermodel.CellType;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mongodb.client.MongoClients;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import utils.ConfigLoader;
import java.time.format.DateTimeFormatter;

public class ReadingSettlementDataFromFinoReport {

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
    public double chargeBackHold;
    public double chargeBackRelease;
    public int date;
    public String cycle;
    public String bankAccountNumber;
    public LocalDate dateConversion;

    @Test
    public void setSettlementData(int number, String path) throws EncryptedDocumentException, IOException {
        try {
            BaseUrlForClass url = new BaseUrlForClass();
            String baseUrl = url.coreBaseUrl;

            filePath = path;
            FileInputStream fis = new FileInputStream(filePath);
            Workbook book = WorkbookFactory.create(fis);
            Sheet data = book.getSheetAt(0);
            LastRowNumber = data.getLastRowNum();

            logger.warn("Accessing Bank Account Value from method");
            bankAccountNumber=getStringCellValue(data.getRow(3 + number).getCell(2));

            logger.warn("Accessing clientId Value from method");
            clientId = getClientID(bankAccountNumber);

            logger.warn("Accessing programId Value from Method");
            programId = getProgramId(clientId);

            logger.warn("Accessing Date value from Excel");
            date = (int) getNumericCellValueForDate(data.getRow(3+ number).getCell(0));

            dateConversion = getDateConversion(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = dateConversion.format(formatter);

            logger.warn("Accessing Date value from Excel");
            cycle = getStringCellValue(data.getRow(3 + number).getCell(3));

             logger.warn("Accessing dateRange Value from Method");
            dateRange = cycleDetails(formattedDate, cycle);

            logger.warn("Accessing collectedAmount value from Excel");
            collectedAmount = getNumericCellValue(data.getRow(3 + number).getCell(9));

            logger.warn("Accessing commissionAmount value from Excel");
            commissionAmount = getNumericCellValue(data.getRow(3 + number).getCell(10));

            logger.warn("Accessing settledAmount value from Excel");
            settledAmount = getNumericCellValue(data.getRow(3 + number).getCell(14));

            commissionGstAmount = 0.00;
            serviceProviderName = "Fino";
            serviceType = "payin";
            rollingReserve = 0.00;

            logger.warn("Accessing utr value from Excel");
            utr = getStringCellValue(data.getRow(3 + number).getCell(18));
            
            logger.warn("Accessing chargeBackHold value from Excel");
            chargeBackHold = getNumericCellValue(data.getRow(3 + number).getCell(11));

            logger.warn("Accessing chargeBackRelease value from Excel");
            chargeBackRelease = getNumericCellValue(data.getRow(3 + number).getCell(14));

        } catch (EncryptedDocumentException | IOException | NullPointerException e) {
            logger.warn("Inside catch BLock of ReadingSettlementData class");
            clientId = "";
            programId = "";
            dateRange = "";
            collectedAmount = 0.01;
            settledAmount = 0.01;
            commissionAmount = 0.01;
            commissionGstAmount = 0.01;
            serviceProviderName = "Fino";
            rollingReserve = 0.01;
            utr = "";
            serviceType = "payin";
            chargeBackHold=0.00;
            chargeBackRelease=0.00;
            e.printStackTrace();
        }
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }
    private double getNumericCellValueForDate(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA)
                ? cell.getNumericCellValue()
                : 0;
    }

    private double getNumericCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA)
                ? cell.getNumericCellValue()
                : 0.1;
    }


    public String cycleDetails(String date,String cycle){

        if (cycle.equalsIgnoreCase("CYCLE_1")) {
            return date+":"+"00.00.00-"+date+":"+"07.59.59";
        }
         else if(cycle.equalsIgnoreCase("CYCLE_2")) {
            return date+":"+"08.00.00-"+date+":"+"13.29.59";
        }
        else if(cycle.equalsIgnoreCase("CYCLE_3")){
            return date+":"+"13.30.00-"+date+":"+"18.29.59";
        }
        else{
            return date+":"+"18.30.00-"+date+":"+"23.59.59";
        }
    }

    public String getClientID(String bankAccountNumber){
        
        MongoClient mongoClient = MongoClients.create(ConfigLoader.getDb());
        MongoDatabase database = mongoClient.getDatabase("cb_core_db");
        MongoCollection<Document> collection = database.getCollection("clients");

        Document query = new Document("service.serviceProviderName", "Fino")
        .append("bankAccountNumber", bankAccountNumber);

         Document projection = new Document("userId", 1).append("_id", 0);
         Document result = collection.find(query).projection(projection).first();
        if (result != null) {
            String userId = result.getString("userId");
            System.out.println("User ID associated with the specified criteria: " + userId);
            mongoClient.close();
            return userId;

        } 
        else {
            System.out.println("No document found for the specified criteria.");
            mongoClient.close();
            return "";
        }
        
    }

    public String getProgramId(String clientId) {
        MongoClient mongoClient = null;
        try {
            // Create MongoDB client and connect to the database
            mongoClient = MongoClients.create(ConfigLoader.getDb());
            MongoDatabase database = mongoClient.getDatabase("cb_fino_service_db");
            MongoCollection<Document> collection = database.getCollection("clients");
    
            // Define the query to find the document with the matching client_ref_id
            Document query = new Document("client_ref_id", clientId);
    
            // Execute the find operation with the specified query
            Document result = collection.find(query).first();
    
            // Check if the result is not null
            if (result != null && result.containsKey("programs")) {
                List<Document> programs = (List<Document>) result.get("programs");
                for (Document program : programs) {
                    if (program.containsKey("program_id")) {
                        // Retrieve the program_id value
                        String programId = program.getString("program_id");
                        System.out.println("Program ID associated with the specified criteria:======>" + programId);
                        return programId;
                    }
                }
            } else {
                System.out.println("No document found for the specified criteria or programs array is empty.=========>");
                return ""; // or handle the case where no programs array or program_id is found
            }
        } catch (Exception e) {
            // Handle exceptions, log errors, etc.
            e.printStackTrace();
        } finally {
            // Close the MongoDB client in a finally block to ensure it's always closed
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    
        return "";
}


    public LocalDate  getDateConversion(int date) {
       
        LocalDate baseDate = LocalDate.of(1900, 1, 1);

        // Add the number of days (excelSerialDate - 1) to the base date
        LocalDate dateFormat = baseDate.plusDays(date - 1);
        System.out.println("Date corresponding to Excel serial 45410: " + date);
        return dateFormat;
    }

}