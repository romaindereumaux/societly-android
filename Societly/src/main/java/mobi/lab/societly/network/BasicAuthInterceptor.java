package mobi.lab.societly.network;

import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthInterceptor implements Interceptor {

    private String username;
    private String password;

    public BasicAuthInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String createAuthorizationHeaderValue() {
        String userAndPassword = username + ":" + password;
        return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            final String authorizationValue = createAuthorizationHeaderValue();
            builder.addHeader("Authorization", authorizationValue);
        }
        return chain.proceed(builder.build());
    }
}
