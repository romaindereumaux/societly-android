package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Submission {

    @SerializedName("state_id")
    private long stateId;
    @SerializedName("district_id")
    private long districtId;

    private List<Answer> answers;
    private String title;
    private Customer customer;

    public long getStateId() {
        return stateId;
    }

    public long getDistrictId() {
        return districtId;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public String getTitle() {
        return title;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "stateId=" + stateId +
                ", districtId=" + districtId +
                ", answers=" + answers +
                ", title='" + title + '\'' +
                ", customer=" + customer +
                '}';
    }
}
