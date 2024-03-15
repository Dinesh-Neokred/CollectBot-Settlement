package settlement;

import static io.restassured.RestAssured.given;

import org.json.simple.JSONObject;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class baseUrlForClass {

    String coreBaseUrl = "http://qa-collectbot.neokred.tech:9080/core-svc/api/v1/";
    String userBaseUrl = "http://qa-collectbot.neokred.tech:9080/user-svc/api/v1/";

    // String coreBaseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";
    // String userBaseUrl = "https://collectbot.neokred.tech/user-svc/api/v1/";

    // if (servicetype.equalsIgnoreCase("voucher")) {
    // JSONObject requestPayloadForVoucherObject = new JSONObject();

    // String debitedAmount = String.valueOf(settledAmount * 100);

    // requestPayloadForVoucherObject.put("client_ref_id", clientId);
    // requestPayloadForVoucherObject.put("program_id", programId);
    // requestPayloadForVoucherObject.put("debited", debitedAmount);

    // RequestSpecification requestPayloadForVoucher =
    // given().contentType("application/json")
    // .body(requestPayloadForVoucherObject.toString()).log().all();
    // Response responseForVoucher = requestPayloadForVoucher.when()
    // .post("http://10.10.10.72:9018/coupon-svc/api/v1/internal/wallet/balance/debit");
    // responseForVoucher.then().log().all();
    // String responseForVocherMessage =
    // responseForVoucher.jsonPath().getString("message");
    // data.getRow(1 + i).getCell(21).setCellValue(responseForVocherMessage);
    // }

}
