package settlement;

import static io.restassured.RestAssured.given;

import org.json.simple.JSONObject;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseUrlForClass {

    // String coreBaseUrl =
    // "http://qa-collectbot.neokred.tech:9080/core-svc/api/v1/";
    // String userBaseUrl =
    // "http://qa-collectbot.neokred.tech:9080/user-svc/api/v1/";

    // String coreBaseUrl = "https://collectbot.neokred.tech/core-svc/api/v1/";
    // String userBaseUrl = "https://collectbot.neokred.tech/user-svc/api/v1/";
    String coreBaseUrl = "https://preprod-collectbot.neokred.tech/core-svc/api/v1/";
    String userBaseUrl = "https://preprod-collectbot.neokred.tech/user-svc/api/v1/";

}
