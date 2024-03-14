package settlement;

import static io.restassured.RestAssured.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import javax.mail.internet.AddressException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.simple.JSONObject;
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

        baseUrlForClass url = new baseUrlForClass();
        String envFilePath = "./.env";

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(envFilePath)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return; // Exit if unable to load properties
        }

        String mail = properties.getProperty("email");
        String mailPassword = properties.getProperty("password");
        String mailForCB = properties.getProperty("cbMail");
        String cbPassword = properties.getProperty("cbPassword");
        String baseUrl = url.coreBaseUrl;

        // Creating Class For calling Methods
        ReadingSettlementData settlementDate = new ReadingSettlementData();
        ReadAndWriteBeforeSettlememtBalance Balance = new ReadAndWriteBeforeSettlememtBalance();
        login token = new login();

        int LastRowNumber = 1;

        // Creating Loop for Reading Multiple data and Creating Multiple Settlement
        for (int i = 0; i < LastRowNumber; i++) {
            // Importing Settlement Sheet For Reading Settlement Details
            filePath = "D:\\New\\RestAssured\\data\\test.xlsx";
            fis = new FileInputStream(filePath);
            book = WorkbookFactory.create(fis);
            data = book.getSheet("userID");
            LastRowNumber = data.getLastRowNum();

            // Generating Auth Token with collectbot Credentials
            auth = token.getAuth(mailForCB, cbPassword);
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
            String servicetype = settlementDate.serviceType.toLowerCase();

            if (clientId.isEmpty()) {
                break;
            } else if (programId.isEmpty()) {
                break;
            } else if (dateRange.isEmpty()) {
                break;
            } else if (collectedAmount == 0.1) {
                break;
            } else if (settledAmount == 0.1) {
                break;
            } else if (commissionAmount == 0.1) {
                break;
            } else if (commissionGstAmount == 0.1) {
                break;
            } else if (rollingReserve == 0.1) {
                break;
            } else if (utr.isEmpty()) {
                break;
            } else if (serviceProviderName.isEmpty()) {
                break;
            } else if (servicetype.isEmpty()) {
                break;
            }

            if (settledAmount >= 1) {
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
                        // .header("servicetype", "Payin")
                        .header("servicetype", servicetype);
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
                System.out.println();
                System.out.println(settlementMessage + "    <================settlementMessage=============>");
                System.out.println();

                // Storing Settlement API Response in the Excel File
                String response = createSettlementApi.jsonPath().getString("message");
                data.getRow(1 + i).getCell(18).setCellValue(response);

                // This Loop Will execute only when the Settlement Record is Created
                // SuccessFully otherwise it will not execute the Revenue create record API
                if (settlementMessage.equalsIgnoreCase("Settlement record created successfully")) {

                    // This API will Hit only when the client is having Service type as Voucher to
                    // debit balance From Coupon service

                    if (servicetype.equalsIgnoreCase("voucher")) {
                        JSONObject requestPayloadForVoucherObject = new JSONObject();

                        String debitedAmount = String.valueOf(settledAmount * 100);

                        requestPayloadForVoucherObject.put("client_ref_id", clientId);
                        requestPayloadForVoucherObject.put("program_id", programId);
                        requestPayloadForVoucherObject.put("debited", debitedAmount);

                        RequestSpecification requestPayloadForVoucher = given().contentType("application/json")
                                .body(requestPayloadForVoucherObject.toString()).log().all();
                        Response responseForVoucher = requestPayloadForVoucher.when()
                                .post("http://10.10.10.72:9018/coupon-svc/api/v1/internal/wallet/balance/debit");
                        responseForVoucher.then().log().all();
                        String responseForVocherMessage = responseForVoucher.jsonPath().getString("message");
                        data.getRow(1 + i).getCell(21).setCellValue(responseForVocherMessage);
                    }

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
                            // .header("servicetype", "Payin");
                            .header("servicetype", servicetype);

                    // Calling or hitting Create Revenue API For Record Create
                    Response createRevenueApi = requestPayloadforRevenue.when()
                            .get(baseUrl + "finance/revenue/record/create");
                    createRevenueApi.then().log().all();

                    // Storing Revenue API Response in the Excel File
                    String createRevenueResponse = createRevenueApi.jsonPath().getString("message");
                    data.getRow(1 + i).getCell(19).setCellValue(createRevenueResponse);
                }

            }
            FileOutputStream fos = new FileOutputStream(filePath);
            book.write(fos);
            book.close();
        }

        System.out.println("Settlement Created SuccessFully");
        EmailSenderForSettlement email = new EmailSenderForSettlement();

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate yesterdayDate = yesterday.atZone(ZoneId.systemDefault()).toLocalDate();
        String yesterdayDateWithoutTime = yesterdayDate.format(formatter);
        System.out.println(yesterdayDate);

        try {
            email.sendMailWithAttachment(filePath, mail, mailPassword, yesterdayDateWithoutTime);
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}