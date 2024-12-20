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
import javax.mail.internet.AddressException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.ConfigLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class settlementApi extends login {

    private static final Logger logger = LoggerFactory.getLogger(settlementApi.class);

    public static FileInputStream fis;
    public static Workbook book;
    public static Sheet data;
    public static String auth;
    public static String filePath;

    public static RequestSpecification requestPayload;

    @Test
    public void createSettlement() throws EncryptedDocumentException, IOException {
        logger.info("Starting createSettlement method...");
        BaseUrlForClass url = new BaseUrlForClass();

        String mail = ConfigLoader.getEmail();
        String mailPassword = ConfigLoader.getMailPassword();
        String mailForCB = ConfigLoader.getEmailForCollectBot();
        String cbPassword = ConfigLoader.getPasswordForCollectbot();
        String baseUrl = url.coreBaseUrl;

        ReadingSettlementData settlementDate = new ReadingSettlementData();
        ReadAndWriteBeforeSettlememtBalance Balance = new ReadAndWriteBeforeSettlememtBalance();
        login token = new login();

        int LastRowNumber = 1;

        for (int i = 0; i < LastRowNumber; i++) {

            logger.info("Processing data for row: " + i);

            filePath = "C:\\Users\\Dinesh M\\Downloads\\23.Fino Settlement for 23rd April-2024.xlsx";
            fis = new FileInputStream(filePath);
            book = WorkbookFactory.create(fis);
            Sheet data = book.getSheetAt(0);
            LastRowNumber = data.getLastRowNum();

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
                logger.warn("clientId is empty, Executting Next Line.");
                continue;
            } else if (programId.isEmpty()) {
                logger.warn("programId is empty, Executting Next Line.");
                continue;
            } else if (dateRange.isEmpty()) {
                logger.warn("dateRange is empty, Executting Next Line.");
                continue;
            } else if (collectedAmount == 0.1) {
                logger.warn("collectedAmount is empty, Executting Next Line.");
                continue;
            } else if (settledAmount == 0.1) {
                logger.warn("settledAmount is empty, Executting Next Line.");
                continue;
            } else if (commissionAmount == 0.1) {
                logger.warn("commissionAmount is empty, Executting Next Line.");
                continue;
            } else if (commissionGstAmount == 0.1) {
                logger.warn("commissionGstAmount is empty, Executting Next Line.");
                continue;
            } else if (rollingReserve == 0.1) {
                logger.warn("rollingReserve is empty, Executting Next Line.");
                continue;
            } else if (utr.isEmpty()) {
                logger.warn("utr is empty, Executting Next Line.");
                continue;
            } else if (serviceProviderName.isEmpty()) {
                logger.warn("serviceProviderName is empty, Executting Next Line.");
                continue;
            } else if (servicetype.isEmpty()) {
                logger.warn("servicetype is empty, Executting Next Line.");
                continue;
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
                        .header("servicetype", servicetype)
                        .header("chargebackRelease", 0)
                        .header("chargebackHold", 0)
                // .log().all()
                ;
                // logger.info("Settlement Record Create API Request====>" +
                // requestPayload.log().all());
                logger.info("Calling get Before DebitBalance Method..." + utr + " ");

                double beforeDebitBalance = Balance.getBeforeDebitBalance(clientId, auth);

                logger.info("Storing Before DebitBalance In Excel..." + utr + " ");
                data.getRow(1 + i).getCell(14).setCellValue(beforeDebitBalance);

                logger.info("Calling Create Settlement API..." + utr + " ");
                Response createSettlementApi = requestPayload.when().get(baseUrl + "finance/settlement/record/create");
                // createSettlementApi.then().log().all();
                logger.info("Create Settlement API response received." + utr + " ");
                logger.info(
                        "Settlement Record Create API Response====>" + utr + " "
                                + createSettlementApi.then().log().all());

                logger.info("Calling get Before DebitBalance Method..." + utr + " ");
                double aftereDebitBalance = Balance.getAfterDebitBalance(clientId, auth);
                logger.info("Storing After DebitBalance In Excel..." + utr + " ");
                data.getRow(1 + i).getCell(15).setCellValue(aftereDebitBalance);

                String settlementMessage = createSettlementApi.jsonPath().getString("message");

                String response = createSettlementApi.jsonPath().getString("message");
                logger.info("Storing Settlement API response received In Excel" + utr + " ");
                data.getRow(1 + i).getCell(18).setCellValue(response);

                if (settlementMessage.equalsIgnoreCase("Settlement record created successfully")) {
                    logger.info(
                            "Settlement record created successfully, proceeding with Revenue API call." + utr + " ");

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

                    logger.info("Calling Create Revenue API..." + utr + " ");
                    // logger.info("Revenue Record Create API Request====>" +
                    // requestPayloadforRevenue.log().all());
                    Response createRevenueApi = requestPayloadforRevenue.when()
                            .get(baseUrl + "finance/revenue/record/create");
                    // createRevenueApi.then().log().all();
                    logger.info("Create Revenue API response received." + utr + " ");
                    logger.info("Revenue Record Create API Response====>" + utr + " "
                            + createRevenueApi.then().log().all());

                    String createRevenueResponse = createRevenueApi.jsonPath().getString("message");
                    logger.info("Storing Revenue API response received In Excel" + utr + " ");
                    data.getRow(1 + i).getCell(19).setCellValue(createRevenueResponse);
                }

            }
            logger.info("Settlement processing completed." + utr + " ");
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