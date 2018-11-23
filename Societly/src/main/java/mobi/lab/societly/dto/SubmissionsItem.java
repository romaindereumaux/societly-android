package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

public class SubmissionsItem {

    String title;

    @SerializedName("client_hash")
    String clientHash;


    public String getTitle() {
        return title;
    }

    public String getClientHash() {
        return clientHash;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "title='" + title + '\'' +
                ", clientHash='" + clientHash + '\'' +
                '}';
    }
}
