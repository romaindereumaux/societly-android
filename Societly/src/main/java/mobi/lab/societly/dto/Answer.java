package mobi.lab.societly.dto;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;

public class Answer implements Parcelable {

    public enum AnswerType {
        NO(0), TEND_TO_DISAGREE(25), NEUTRAL(50), TEND_TO_AGREE(75), YES(100), SKIP(-1);

        private int value;

        AnswerType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AnswerType find(int type) {
            SparseArray<AnswerType> map = new SparseArray<>(6);
            map.put(SKIP.getValue(), SKIP);
            map.put(NO.getValue(), NO);
            map.put(TEND_TO_DISAGREE.getValue(), TEND_TO_DISAGREE);
            map.put(NEUTRAL.getValue(), NEUTRAL);
            map.put(TEND_TO_AGREE.getValue(), TEND_TO_AGREE);
            map.put(YES.getValue(), YES);
            return map.get(type);
        }
    }

    @SerializedName("question_id")
    private int questionId;

    @SerializedName("answer")
    private int value;

    public Answer(AnswerType answer) {
        this.value = answer.getValue();
    }

    public AnswerType getValue() {
        return AnswerType.find(value);
    }

    public int getNumbericalValue() {
        return value;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int id) {
        questionId = id;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "questionId=" + questionId +
                ", value=" + getValueString() +
                '}';
    }

    public String getValueString() {
        switch (getValue()) {
            case YES:
                return "Agree";
            case NO:
                return "Disagree";
            case NEUTRAL:
                return "Neutral";
            case SKIP:
                return "Skip";
            case TEND_TO_DISAGREE:
                return "Tend to disagree";
            case TEND_TO_AGREE:
                return "Tend to agree";
            default:
                return "N/A";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.questionId);
        dest.writeInt(this.value);
    }

    protected Answer(Parcel in) {
        this.questionId = in.readInt();
        this.value = in.readInt();
    }

    public static final Parcelable.Creator<Answer> CREATOR = new Parcelable.Creator<Answer>() {
        @Override
        public Answer createFromParcel(Parcel source) {
            return new Answer(source);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };
}
