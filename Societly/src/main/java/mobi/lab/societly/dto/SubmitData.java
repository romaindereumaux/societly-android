package mobi.lab.societly.dto;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmitData {

    @SerializedName("email")
    private String email;

    @SerializedName("publisher")
    private String publisher;

    // Time when the user saw the start screen
    @SerializedName("open_page_at")
    private String openPageAtTime;

    // Seconds from seeing the first question to result
    @SerializedName("start_to_result")
    private int questionToResult;

    // Seconds from seeing the start screen to result
    @SerializedName("seconds_to_result")
    private int startToResult;

    @SerializedName("answers")
    private List<SubmitAnswer> answers;

    public SubmitData(String email, String openPageAtTime, int questionToResult, int startToResult, List<SubmitAnswer> answers) {
        this.openPageAtTime = openPageAtTime;
        this.questionToResult = questionToResult;
        this.startToResult = startToResult;
        this.answers = answers;
        this.email = TextUtils.isEmpty(email) ? null : email;
        this.publisher = "mobile_android";
    }

    @Override
    public String toString() {
        return "SubmitData{" +
                "email=" + email +
                ", publisher=" + publisher +
                ", openPageAtTime='" + openPageAtTime + '\'' +
                ", questionToResult=" + questionToResult +
                ", startToResult=" + startToResult +
                ", answers=" + answers +
                '}';
    }
}
