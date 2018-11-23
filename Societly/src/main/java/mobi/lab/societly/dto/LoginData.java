package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

public class LoginData {

    private static final String PROVIDER_FACEBOOK = "facebook";
    private static final String PROVIDER_GOOGLE = "google";

    private String email;
    private String password;

    @SerializedName("access_token")
    private String token;
    private String provider;

    public LoginData() {}

    public static LoginData create(String email, String password) {
        LoginData data = new LoginData();
        data.email = email;
        data.password = password;
        return data;
    }

    public static LoginData createFacebook(String token) {
        LoginData data = new LoginData();
        data.token = token;
        data.provider = PROVIDER_FACEBOOK;
        return data;
    }

    public static LoginData createGoogle(String token) {
        LoginData data = new LoginData();
        data.token = token;
        data.provider = PROVIDER_GOOGLE;
        return data;
    }

    @Override
    public String toString() {
        return "LoginData{" +
                "email='" + email + '\'' +
                ", password='***'" +
                ", token='" + token + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
}
