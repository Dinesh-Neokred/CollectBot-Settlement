package utils;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigLoader {

    private static final Dotenv dotenv = Dotenv.load();

    public static String getEmail() {
        return dotenv.get("email");
    }

    public static String getMailPassword() {
        return dotenv.get("mailPassword");
    }

    public static String getEmailForCollectBot() {
        return dotenv.get("collectbotEmail");
    }

    public static String getPasswordForCollectbot() {
        return dotenv.get("collectbotPassword");
    }

    public static String getBaseurl(String url) {
        return dotenv.get("url");
    }
    public static String getDb() {
        return dotenv.get("db");
    }
    public static String getFinoSubCore() {
        return dotenv.get("fino");
    }
    public static String getEqPayoutSubCore() {
        return dotenv.get("eqPayout");
    }
}