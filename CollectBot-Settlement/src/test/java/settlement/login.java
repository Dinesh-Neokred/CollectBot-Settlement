package settlement;

import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;

public class login {
    private String auth;

    // @Test(priority = 0)
    public String getAuth(String email, String pass) {
        baseUrlForClass url = new baseUrlForClass();
        String UserBaseUrl = url.userBaseUrl;

        JSONObject data = new JSONObject();
        // HashMap data= new HashMap();
        data.put("email", email);
        data.put("password", pass);
        Response ob = given().contentType("application/json").body(data.toString()).when()
                .post(UserBaseUrl + "user/login/single-signin");
        String re = ob.jsonPath().get().toString();
        System.out.println("==================================================");
        // System.out.println(re);
        // System.out.println("=================================================");
        String token = ob.jsonPath().getString("data.token");
        auth = "Bearer" + " " + token;
        // ob.then().log().all();
        // System.out.println(auth);
        return auth;
    }

}
