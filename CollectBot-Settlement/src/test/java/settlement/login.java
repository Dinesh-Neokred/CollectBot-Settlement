package settlement;

import org.json.simple.JSONObject;

import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

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
        String token = ob.jsonPath().getString("data.token");
        auth = "Bearer" + " " + token;
        return auth;
    }

}
