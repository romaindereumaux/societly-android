package mobi.lab.societly.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Candidate implements Parcelable {

    private int id;
    private String name;
    @SerializedName("image")
    private String imageUrl;
    private String party;
    private List<Answer> answers;
    @SerializedName("state_id")
    private long stateId;

    @SerializedName("district_id")
    private long districtId;
    private String level;

    public Candidate(int id, String name, List<Answer> answers) {
        this.id = id;
        this.name = name;
        this.imageUrl = null;
        this.answers = answers;
        this.party = "N/A";
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getParty() {
        return party;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public long getStateId() {
        return stateId;
    }

    public long getDistrictId() {
        return districtId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.imageUrl);
        dest.writeString(this.party);
        dest.writeList(this.answers);
        dest.writeLong(this.stateId);
        dest.writeLong(this.districtId);
        dest.writeString(this.level);
    }

    protected Candidate(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.imageUrl = in.readString();
        this.party = in.readString();
        this.answers = new ArrayList<>();
        in.readList(this.answers, Answer.class.getClassLoader());
        this.stateId = in.readLong();
        this.districtId = in.readLong();
        this.level = in.readString();
    }

    public static final Parcelable.Creator<Candidate> CREATOR = new Parcelable.Creator<Candidate>() {
        @Override
        public Candidate createFromParcel(Parcel source) {
            return new Candidate(source);
        }

        @Override
        public Candidate[] newArray(int size) {
            return new Candidate[size];
        }
    };
}
