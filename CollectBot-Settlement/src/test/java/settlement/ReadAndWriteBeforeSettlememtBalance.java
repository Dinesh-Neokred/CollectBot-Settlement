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
        public static String baseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";

        @Test
        public double getBeforeDebitBalance(String clientId)
                        throws EncryptedDocumentException, IOException {
                String auth = getAuth("admin@neokred.tech", "Neokred@12345");

                RequestSpecification requestPayload = given().contentType("application/json")
                                .header("Authorization", auth)
                                .header("userid", clientId);
                double balance = requestPayload.when().get(baseUrl + "service/client/balance").jsonPath()
                                .getDouble("data.debited");
                return balance;

                // FileInputStream fis = new FileInputStream("./data/Settlement.xlsx");
                // Workbook book = WorkbookFactory.create(fis);
                // Sheet data = book.getSheet("userID");
                // data.getRow(1 + rowNumber).getCell(15).setCellValue(balance);
                // FileOutputStream fos = new FileOutputStream("./data/Settlement.xlsx");
                // book.write(fos);
                // book.close();

        }

        @Test()
        public double getAfterDebitBalance(String clientId)
                        throws EncryptedDocumentException, IOException {
                RequestSpecification requestPayload = given().contentType("application/json")
                                .header("Authorization", getAuth("admin@neokred.tech", "Neokred@12345"))
                                .header("userid", clientId);
                double balance = requestPayload.when().get(baseUrl + "service/client/balance").jsonPath()
                                .getDouble("data.debited");
                return balance;
                // FileInputStream fis = new FileInputStream("./data/Settlement.xlsx");
                // Workbook book = WorkbookFactory.create(fis);
                // Sheet data = book.getSheet("userID");
                // data.getRow(1 + rowNumber).getCell(16).setCellValue(balance);
                // FileOutputStream fos = new FileOutputStream("./data/Settlement.xlsx");
                // book.write(fos);
                // book.close();

        }
}
