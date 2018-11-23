package mobi.lab.societly.dto;

import com.google.gson.annotations.SerializedName;

public class Customer {

    private String email;
    @SerializedName("full_name")
    private String fullName;

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
