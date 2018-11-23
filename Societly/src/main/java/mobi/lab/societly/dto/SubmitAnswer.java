package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

public class SubmitAnswer {

    private int balance;
    private Integer answer;
    @SerializedName("question_id")
    private int questionId;

    public SubmitAnswer(int questionId, Integer answer) {
        // Mobile answers always have a balance of 1
        this.balance = 1;
        this.answer = answer;
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "SubmitAnswer{" +
                "answer=" + answer +
                ", questionId=" + questionId +
                ", balance=" + balance +
                '}';
    }
}
