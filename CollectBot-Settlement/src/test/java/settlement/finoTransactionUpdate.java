package settlement;

import static io.restassured.RestAssured.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class finoTransactionUpdate extends login {

    private static final Logger logger = LoggerFactory.getLogger(settlementApi.class);

    public static FileInputStream fis;
    public static Workbook book;
    public static Sheet data;
    public static String auth;
    public static String filePath;

    public static RequestSpecification requestPayload;

    @SuppressWarnings("unchecked")
    @Test
    public void createSettlement() throws EncryptedDocumentException, IOException {
        logger.info("Starting createSettlement method...");
        BaseUrlForClass url = new BaseUrlForClass();

        ReadingDataForFino transactionData = new ReadingDataForFino();
        ReadAndWriteBeforeSettlememtBalance Balance = new ReadAndWriteBeforeSettlememtBalance();
        login token = new login();

        int LastRowNumber = 1;

        for (int i = 0; i < LastRowNumber; i++) {
            logger.info("Processing data for row: " + i);

            filePath = "//Users//Dinesh//Dinesh Lap Backup//Fino Date//SEP//SEP 8.xlsx";
            fis = new FileInputStream(filePath);
            book = WorkbookFactory.create(fis);
            Sheet data = book.getSheetAt(0);
            LastRowNumber = data.getLastRowNum();

            transactionData.setSettlementData(i, filePath);

            String CBS_Ref_Num = transactionData.CBS_Ref_Num;
            String TXN_ID = transactionData.TXN_ID;
            String CustomerVPA = transactionData.CustomerVPA;
            String CustomerName = transactionData.CustomerName;
            String merchantVpa = transactionData.merchantVpa;
            String merchantName = transactionData.merchantName;
            String TransactionDateTime = transactionData.TransactionDateTime;
            String transactionId = transactionData.transactionId;

            double amount = transactionData.amount;
            long utr = transactionData.utr; // UTR as long
            long customerMobileNumber = transactionData.customerMobileNumber;

            if (CBS_Ref_Num.isEmpty()) {
                logger.warn("CBS_Ref_Num is empty, Executting Next Line.");
                continue;
            } else if (TXN_ID.isEmpty()) {
                logger.warn("TXN_ID is empty, Executting Next Line.");
                continue;
            } else if (CustomerVPA.isEmpty()) {
                logger.warn("CustomerVPA is empty, Executting Next Line.");
                continue;
            } else if (amount == 0.00) {
                logger.warn("amount is empty, Executting Next Line.");
                continue;
            } else if (utr == 0L) {
                logger.warn("utr is empty, Executting Next Line.");
                continue;
            } else if (customerMobileNumber == 0L) {
                logger.warn("customerMobileNumber is empty, Executting Next Line.");
                continue;
            } else if (merchantVpa.isEmpty()) {
                logger.warn("merchantVpa is empty, Executting Next Line.");
                continue;
            } else if (merchantName.isEmpty()) {
                logger.warn("merchantName is empty, Executting Next Line.");
                continue;
            } else if (TransactionDateTime.isEmpty()) {
                logger.warn("TransactionDateTime is empty, Executting Next Line.");
                continue;
            } else if (transactionId.isEmpty()) {
                logger.warn("transactionId is empty, Executting Next Line.");
                continue;
            }

            @SuppressWarnings("rawtypes")
            HashMap requestPayloadforTransactionDetails = new HashMap<>();

            requestPayloadforTransactionDetails.put("TxnStatus", "0");
            requestPayloadforTransactionDetails.put("TxnAmt", String.format("%.2f", amount)); // Amount with 2 decimal places
            requestPayloadforTransactionDetails.put("TxnType", "CREDIT");
            requestPayloadforTransactionDetails.put("RRN", String.valueOf(utr)); // UTR as string without decimal
            requestPayloadforTransactionDetails.put("CBS_Ref_Num", CBS_Ref_Num);
            requestPayloadforTransactionDetails.put("TXN_ID", TXN_ID);
            requestPayloadforTransactionDetails.put("InitiationMode", "05");
            requestPayloadforTransactionDetails.put("CustomerVPA", merchantVpa);
            requestPayloadforTransactionDetails.put("CustomerName", merchantName);
            requestPayloadforTransactionDetails.put("CustomerIFSC", "FINO0000001");
            requestPayloadforTransactionDetails.put("CustomerAccNum", "FINO0000001");
            requestPayloadforTransactionDetails.put("CustomerAccType", "SAVINGS");
            requestPayloadforTransactionDetails.put("PayerMobileNumber", String.valueOf(customerMobileNumber));
            requestPayloadforTransactionDetails.put("PayerVPA", CustomerVPA);
            requestPayloadforTransactionDetails.put("PayerName", CustomerName);
            requestPayloadforTransactionDetails.put("PayerAccType", "SAVINGS");
            requestPayloadforTransactionDetails.put("TransactionDateTime", TransactionDateTime);
            requestPayloadforTransactionDetails.put("PartnerID", "1911067421368647680");
            requestPayloadforTransactionDetails.put("PartnerName", "Neokred Technologies Pvt Ltd");
            requestPayloadforTransactionDetails.put("UPIRefID", transactionId);
            requestPayloadforTransactionDetails.put("MsgId", "Update By Script");
            requestPayloadforTransactionDetails.put("TransactionNote", "payin");

            HashMap requestPayloadForUpdate = new HashMap<>();
            requestPayloadForUpdate.put("returnCode", "0");
            requestPayloadForUpdate.put("partner", "neokred technologies pvt ltd");
            requestPayloadForUpdate.put("responseMessage", "SUCCESS");
            requestPayloadForUpdate.put("CallBackResponse", requestPayloadforTransactionDetails);

            if (amount >= 1.00) {
                requestPayload = given()
                        .contentType("application/json")
                        .body(requestPayloadForUpdate)
                        .log().all();

                logger.info("Calling Fino Callback ..." + utr);

                Response callback = requestPayload.when().get("http://qa-collectbot.neokred.tech:9080/payin/fn/api/v1/callback/notify");
                logger.info("Create Settlement API response received: " + utr);
                logger.info("Settlement Record Create API Response: " + utr + " " + callback.then().log().all());

                String settlementMessage = callback.jsonPath().getString("responseMessage");

                logger.info("Storing Settlement API response received In Excel for UTR: " + utr);
                data.getRow(1 + i).getCell(17).setCellValue(settlementMessage);
            }
            logger.info("Callback processing completed: " + utr);
            FileOutputStream fos = new FileOutputStream(filePath);
            book.write(fos);
            book.close();
        }

        System.out.println("Callback sent Successfully");
    }
}
