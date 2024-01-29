package settlement;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.mail.internet.AddressException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class settlementApi extends login {

    public static FileInputStream fis;
    public static Workbook book;
    public static Sheet data;
    public static String auth;
    public static String filePath;

    public static RequestSpecification requestPayload;

    @Test
    public void createSettlement() throws EncryptedDocumentException, IOException {
        String baseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";

        // Creating Class For calling Methods
        ReadingSettlementData settlementDate = new ReadingSettlementData();
        ReadAndWriteBeforeSettlememtBalance Balance = new ReadAndWriteBeforeSettlememtBalance();
        login token = new login();

        // Importing Settlement Sheet For Reading Settlement Details
        filePath = "C:\\Users\\Dinesh\\Downloads\\27.Fino Settlement 27th Jan-2024.xlsx";
        fis = new FileInputStream(filePath);
        book = WorkbookFactory.create(fis);
        data = book.getSheet("userID");
        int LastRowNumber = data.getLastRowNum();

        // Creating Loop for Reading Multiple data and Creating Multiple Settlement
        for (int i = 0; i < LastRowNumber; i++) {

            // Generating Auth Token with collectbot Credentials
            auth = token.getAuth("admin@neokred.tech", "Neokred@12345");
            settlementDate.setSettlementData(i, filePath);

            String clientId = settlementDate.clientId;
            String programId = settlementDate.programId;
            String dateRange = settlementDate.dateRange;
            double collectedAmount = settlementDate.collectedAmount;
            double settledAmount = settlementDate.settledAmount;
            double commissionAmount = settlementDate.commissionAmount;
            double commissionGstAmount = settlementDate.commissionGstAmount;
            String utr = settlementDate.utr;
            double rollingReserve = settlementDate.rollingReserve;
            String serviceProviderName = settlementDate.serviceProviderName;

            if (settledAmount > 0) {
                requestPayload = given()
                        .contentType("application/json")
                        .headers("Authorization", auth)
                        .header("client_id", clientId)
                        .header("program_id", programId)
                        .header("daterange", dateRange)
                        .header("totalactualamount", collectedAmount)
                        .header("totaltransferamount", settledAmount)
                        .header("totalcommissionamount", commissionAmount)
                        .header("totalcommissiongst", commissionGstAmount)
                        .header("utr", utr)
                        .header("reserves", rollingReserve)
                        .header("serviceProviderName", serviceProviderName)
                        .header("servicetype", "Payin")
                // .log().all()
                ;

                // Reading debited balanace for Client before hitting Settlement API
                double beforeDebitBalance = Balance.getBeforeDebitBalance(clientId, auth);

                // Storing before Debited balance In The excel File
                data.getRow(1 + i).getCell(14).setCellValue(beforeDebitBalance);

                // Calling or hitting Create Settlement API For Record Create
                Response createSettlementApi = requestPayload.when().get(baseUrl + "finance/settlement/record/create");
                createSettlementApi.then().log().all();

                // Storing After Debited balance In The excel File
                double aftereDebitBalance = Balance.getAfterDebitBalance(clientId, auth);
                data.getRow(1 + i).getCell(15).setCellValue(aftereDebitBalance);

                // Stroing The Response Message Of Create Settlement API For Record Create
                String settlementMessage = createSettlementApi.jsonPath().getString("message");

                // Storing Settlement API Response in the Excel File
                String response = createSettlementApi.jsonPath().get().toString();
                data.getRow(1 + i).getCell(18).setCellValue(response);

                // This Loop Will execute only when the Settlement Record is Created
                // SuccessFully otherwise it will not execute the Revenue create record API
                if (settlementMessage.equalsIgnoreCase("Settlement record created successfully")) {

                    RequestSpecification requestPayloadforRevenue = given()
                            // .log().all()
                            .contentType("application/json")
                            .headers("Authorization", auth)
                            .header("client_id", clientId)
                            .header("program_id", programId)
                            .header("daterange", dateRange)
                            .header("totalactualamount", collectedAmount)
                            .header("totaltransferamount", settledAmount)
                            .header("totalcommissionamount", commissionAmount)
                            .header("totalcommissiongst", commissionGstAmount)
                            .header("serviceProviderName", serviceProviderName)
                            .header("servicetype", "Payin");

                    // Calling or hitting Create Revenue API For Record Create
                    Response createRevenueApi = requestPayloadforRevenue.when()
                            .get(baseUrl + "finance/revenue/record/create");
                    createRevenueApi.then().log().all();

                    // Storing Revenue API Response in the Excel File
                    String createRevenueResponse = createRevenueApi.jsonPath().get().toString();
                    data.getRow(1 + i).getCell(19).setCellValue(createRevenueResponse);
                }

            }

        }
        FileOutputStream fos = new FileOutputStream(filePath);
        book.write(fos);
        book.close();
        System.out.println("Settlement Created SuccessFully");
        EmailSenderForSettlement email = new EmailSenderForSettlement();

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate todayDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate yesterdayDate = yesterday.atZone(ZoneId.systemDefault()).toLocalDate();
        String yesterdayDateWithoutTime = yesterdayDate.format(formatter);
        System.out.println(yesterdayDate);

        try {
            email.sendMailWithAttachment(filePath, "admin@neokred.tech", "Neokred@12345", yesterdayDateWithoutTime);
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
