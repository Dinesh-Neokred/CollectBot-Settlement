package settlement;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.Test;

import io.restassured.specification.RequestSpecification;

public class ReadAndWriteBeforeSettlememtBalance extends login {
        BaseUrlForClass url = new BaseUrlForClass();
        String baseUrl = url.coreBaseUrl;

        @Test
        public double getBeforeDebitBalance(String clientId, String token)
                        throws EncryptedDocumentException, IOException {

                RequestSpecification requestPayload = given().contentType("application/json")
                                .header("Authorization", token)
                                .header("userid", clientId);
                double balance = requestPayload.when().get(baseUrl + "service/client/balance").jsonPath()
                                .getDouble("data.debited");
                return balance;

        }

        @Test()
        public double getAfterDebitBalance(String clientId, String token)
                        throws EncryptedDocumentException, IOException {
                RequestSpecification requestPayload = given().contentType("application/json")
                                .header("Authorization", token)
                                .header("userid", clientId);
                double balance = requestPayload.when().get(baseUrl + "service/client/balance").jsonPath()
                                .getDouble("data.debited");
                return balance;
        }
}
