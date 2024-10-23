package QR_API_VALIDTAION;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import static io.restassured.RestAssured.given;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import java.util.Locale;
import java.util.Properties;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class IntentAPIValidation {
    String envFilePath = "./.env";
    public static Properties properties = new Properties();

    String baseUrl;
    String maskedServiceName;
    String client_secret;
    String program_id;

    public static Faker fakeValue = new Faker((new Locale("en", "IN")));

    @Test(priority = 0)
    public void verifyAPI() {
        HashMap requestPayload = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(envFilePath)) {
            properties.load(fis);
        } catch (IOException e) {

            e.printStackTrace();
            return; // Exit if unable to load properties
        }
        String firstFiveChars = RandomStringUtils.randomAlphabetic(5).toUpperCase();
        LocalDateTime timeStamp = fakeValue.date().past(3650, java.util.concurrent.TimeUnit.DAYS).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

        String orderId = firstFiveChars + timeStamp;
        requestPayload.put("amount", fakeValue.number().randomDouble(2, 1, 99999));
        requestPayload.put("remark", "");
        requestPayload.put("orderId", orderId);
        requestPayload.put("expireAfter", fakeValue.number().numberBetween(1, 90));

        baseUrl = properties.getProperty("finoBaseUrl");
        maskedServiceName = properties.getProperty("maskedServiceName");
        client_secret = properties.getProperty("client_secret");
        program_id = properties.getProperty("program_id");

        Response response = given().contentType("application/json").headers("client_secret", client_secret)
                .header("program_id", program_id)
                .body(requestPayload).when()
                .post(baseUrl + "/payin/" + maskedServiceName + "/api/v1/external/upi/qr/generate");
        response.then().log().all();
        String statusCode = response.jsonPath().getString("statusCode");
        SoftAssert assertion = new SoftAssert();
        assertion.assertEquals(statusCode, "200");
    }

    @Test(priority = 1)
    public void verifylessThan1Ruppes() {
        HashMap requestPayload = new HashMap<>();
        requestPayload.put("amount", "0");
        requestPayload.put("remark", "");

        Response response = given().contentType("application/json").headers("client_secret", client_secret)
                .header("program_id", program_id)
                .body(requestPayload).when()
                .post(baseUrl + "/payin/" + maskedServiceName + "/api/v1/external/upi/qr/generate");
        response.then().log().all();
        String responseMessage = response.jsonPath().getString("message");
        SoftAssert assertion = new SoftAssert();
        assertion.assertEquals(responseMessage, "QR Generation Failed, Amount can not be zero.");
    }

    public void verifyMoreThan1lakhs() {
        HashMap requestPayload = new HashMap<>();
        requestPayload.put("amount", "100001.00");
        requestPayload.put("remark", "");

        Response response = given().contentType("application/json").headers("client_secret", client_secret)
                .header("program_id", program_id)
                .body(requestPayload).when()
                .post(baseUrl + "/payin/" + maskedServiceName + "/api/v1/external/upi/qr/generate");
        response.then().log().all();
        String responseMessage = response.jsonPath().getString("message");
        SoftAssert assertion = new SoftAssert();
        assertion.assertEquals(responseMessage, "Amount should not be greater than 100000");
    }

    public void mininumTransactionLimitCheckFromDB() {

        MongoClient mongoClient = MongoClients.create(
                "mongodb+srv://mongodb:uwnvfDGYtNpC7gZU@collectbot-preprod.8ooov.mongodb.net/?authMechanism=DEFAULT");
        MongoDatabase database = mongoClient.getDatabase("cb_fino_service_db");
        MongoCollection<Document> collection = database.getCollection("clients");
        Document query = new Document("client_ref_id", client_secret);
        Document result = collection.find(query).first();

        Integer minAmount = (result.getInteger("programs.minTransactionAmt") / 100) - 1;

        String amount = String.valueOf(minAmount);

        HashMap requestPayload = new HashMap<>();
        requestPayload.put("amount", amount + ".00");
        requestPayload.put("remark", "");

        Response response = given().contentType("application/json").headers("client_secret", client_secret)
                .header("program_id", program_id)
                .body(requestPayload).when()
                .post(baseUrl + "/payin/" + maskedServiceName + "/api/v1/external/upi/qr/generate");
        response.then().log().all();
        String responseMessage = response.jsonPath().getString("message");
        SoftAssert assertion = new SoftAssert();
        assertion.assertEquals(responseMessage, "Amount should not be greater than 100000");
    }
    

}
