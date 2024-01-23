package settlement;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.mail.internet.AddressException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class settlementApi extends login {
    public static String baseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";
    public static int amount;
    public static String clientId;
    public static String programId;
    public static String dateRange;
    public static double collectedAmount;
    public static double settledAmount;
    public static double commissionAmount;
    public static double commissionGstAmount;
    public static String utr;
    public static double rollingReserve;
    public static String serviceProviderName;
    public static FileInputStream fis;
    public static Workbook book;
    public static Sheet data;
    public static String auth;
    public static String filePath;

    public static RequestSpecification requestPayload;

    @Test
    public void createSettlement() throws EncryptedDocumentException, IOException {

        // Creating Class For calling Methods
        ReadingSettlementData settlementDate = new ReadingSettlementData();
        ReadAndWriteBeforeSettlememtBalance Balance = new ReadAndWriteBeforeSettlememtBalance();
        login token = new login();

        // Importing Settlement Sheet For Reading Settlement Details
        filePath = "C:\\Users\\Dinesh\\Downloads\\22.Fino Settlement 22nd Jan-2024 (1).xlsx";
        fis = new FileInputStream(filePath);
        book = WorkbookFactory.create(fis);
        data = book.getSheet("userID");
        int LastRowNumber = data.getLastRowNum();

        // Creating Loop for Reading Multiple data and Creating Multiple Settlement
        for (int i = 0; i < LastRowNumber; i++) {
            auth = token.getAuth("admin@neokred.tech", "Neokred@12345");
            settlementDate.setSettlementData(i, filePath);
            clientId = settlementDate.clientId;
            programId = settlementDate.programId;
            dateRange = settlementDate.dateRange;
            collectedAmount = settlementDate.collectedAmount;
            settledAmount = settlementDate.settledAmount;
            commissionAmount = settlementDate.commissionAmount;
            commissionGstAmount = settlementDate.commissionGstAmount;
            utr = settlementDate.utr;
            rollingReserve = settlementDate.rollingReserve;
            serviceProviderName = settlementDate.serviceProviderName;

            /// amount = Integer.valueOf(settledAmount);

            if (settledAmount > 0) {
                requestPayload = given()
                        // .log()
                        // .all()
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

                Response createSettlementApi = requestPayload.when().get(baseUrl + "finance/settlement/record/create");
                createSettlementApi.then().log().all();

                // Storing After Debited balance In The excel File
                double aftereDebitBalance = Balance.getAfterDebitBalance(clientId, auth);
                data.getRow(1 + i).getCell(15).setCellValue(aftereDebitBalance);

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

                    Response createRevenueApi = requestPayloadforRevenue.when()
                            .get(baseUrl + "finance/revenue/record/create");
                    createRevenueApi.then()
                            .log().all();
                    String createRevenueResponse = createRevenueApi.jsonPath().get().toString();
                    // Sheet data = book.getSheet("userID");
                    data.getRow(1 + i).getCell(19).setCellValue(createRevenueResponse);
                }

            }

        }
        FileOutputStream fos = new FileOutputStream(filePath);
        book.write(fos);
        book.close();
        System.out.println("Settlement Created SuccessFully");
        EmailSenderForSettlement email = new EmailSenderForSettlement();

        try {
            email.sendMailWithAttachment(filePath, "admin@neokred.tech", "Neokred@12345");
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
