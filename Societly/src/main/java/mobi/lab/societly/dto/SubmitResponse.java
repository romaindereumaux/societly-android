package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

public class SubmitResponse {

    @SerializedName("client_hash")
    private String clientHash;

    public String getClientHash() {
        return clientHash;
    }
}
