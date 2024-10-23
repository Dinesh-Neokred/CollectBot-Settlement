package VoucherCallback;

import static io.restassured.RestAssured.given;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.bson.Document;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class Callback {
    public static String filePath;
    public static FileInputStream fis;
    public static Workbook book;
    public Sheet sheetValue;
    HashMap requestPayloadBody = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(Callback.class);
    @SuppressWarnings("unchecked")
    @Test
    public void clientCallback() throws IOException  {
        String upiId;
        double amount ;
        String custRefNo;
        String orderId ;
        String transactionId;
        String txnStatus;
        String txnTime;
        String cbsRefNo ;
        String upiTxnId ;

        int LastRowNumber = 1;
        for (int i = 0; i <= LastRowNumber; i++) {

            filePath = "C:\\Users\\Dinesh M\\Downloads\\status Check.xlsx";
            try {
                fis = new FileInputStream(filePath);
                book = WorkbookFactory.create(fis);
                Sheet callbackSheet = book.getSheetAt(0);
                LastRowNumber = callbackSheet.getLastRowNum();
                sheetValue=callbackSheet;
    
    
                transactionId = callbackSheet.getRow(1 + i).getCell(0).getStringCellValue();
                upiTxnId = callbackSheet.getRow(1 + i).getCell(7).getStringCellValue();
                custRefNo = callbackSheet.getRow(1 + i).getCell(6).getStringCellValue();
                orderId = callbackSheet.getRow(1 + i).getCell(1).getStringCellValue();
                upiId = callbackSheet.getRow(1 + i).getCell(2).getStringCellValue();
                amount = callbackSheet.getRow(1 + i).getCell(3).getNumericCellValue();
                txnStatus = callbackSheet.getRow(1 + i).getCell(4).getStringCellValue();
                txnTime = callbackSheet.getRow(1 + i).getCell(5).getStringCellValue();
                cbsRefNo = callbackSheet.getRow(1 + i).getCell(8).getStringCellValue();
              
    
              
    
                requestPayloadBody.put("upiId", upiId);
                requestPayloadBody.put("amount", amount);
                requestPayloadBody.put("customerName", "GiftOn Vouchers");
                requestPayloadBody.put("custRefNo", custRefNo);
                requestPayloadBody.put("mcc", "0000");
                requestPayloadBody.put("transactionId", transactionId);
                requestPayloadBody.put("cbsRefNo", cbsRefNo);
                requestPayloadBody.put("upiTxnId", upiTxnId);
                requestPayloadBody.put("orderId", orderId);
                requestPayloadBody.put("txnTime", txnTime);
                requestPayloadBody.put("txnStatus", txnStatus);

            } catch (EncryptedDocumentException | IOException | NullPointerException e) {
                upiId="";
                amount=0.00;
                custRefNo="";
                orderId="";
                transactionId="";
                txnStatus="";
                txnTime="";
                cbsRefNo="";
                upiTxnId="";
            }
            if(upiId.isEmpty()){
                logger.warn("upiId is empty, Executting Next Line.");
                continue;  
            }
            if(amount==0.00){
                logger.warn("amount is empty, Executting Next Line.");
                continue;  
            }
            if(custRefNo.isEmpty()){
                logger.warn("custRefNo is empty, Executting Next Line.");
                continue;  
            }
            if(orderId.isEmpty()){
                logger.warn("orderId is empty, Executting Next Line.");
                continue;  
            }
            if(transactionId.isEmpty()){
                logger.warn("transactionId is empty, Executting Next Line.");
                continue;  
            }
            if(txnStatus.isEmpty()){
                logger.warn("txnStatus is empty, Executting Next Line.");
                continue;  
            }
            if(txnTime.isEmpty()){
                logger.warn("txnTime is empty, Executting Next Line.");
                continue;  
            }
            if(cbsRefNo.isEmpty()){
                logger.warn("cbsRefNo is empty, Executting Next Line.");
                continue;  
            }
            if(upiTxnId.isEmpty()){
                logger.warn("upiTxnId is empty, Executting Next Line.");
                continue;  
            }

             Response clientResponse = given().contentType("application/json").body(requestPayloadBody).when()
                    .post("https://www.gifton.app/coupon-svc/api/v1/internal/callback/create/coupon");
                    clientResponse.then().log().all();

            logger.info(clientResponse.then().toString());

            // sheetValue.getRow(1+i).getCell(9).setCellValue(clientResponse.toString());
            logger.info("Callback processing completed." + transactionId+" ");
            FileOutputStream fos = new FileOutputStream(filePath);
            book.write(fos);
            book.close();
        }
    }

}
