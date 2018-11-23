package mobi.lab.societly.dto;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.List;

public class CandidateCompatibility implements Comparable, Parcelable {

    private Candidate candidate;
    private float compatibility;
    private SparseArray<Answer> answerMap;

    public CandidateCompatibility(Candidate candidate) {
        this.candidate = candidate;
        this.answerMap = createAnswerMap();
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public int getCompatibility() {
        return Math.round(compatibility);
    }

    public void calculateCompatibility(Questionnaire questionnaire) {
        int distanceValue = 0;
        int distanceCount = 0;
        List<Question> questionsA = questionnaire.getQuestions();
        for (int i = 0; i < questionsA.size(); i++) {
            Question question = questionsA.get(i);
            Answer userAnswer = question.getAnswer();
            if (Answer.AnswerType.SKIP == userAnswer.getValue()) {
                continue;
            }
            Answer candidateAnswer = answerMap.get(question.getId());
            distanceCount++;
            distanceValue += Math.abs(userAnswer.getNumbericalValue() - candidateAnswer.getNumbericalValue());
        }
        compatibility = (distanceCount == 0) ? 0 : (100.0f - distanceValue * 1.0f / distanceCount * 1.0f);
    }

    @Override
    public int compareTo(@NonNull Object object) {
        CandidateCompatibility another = (CandidateCompatibility) object;
        if (compatibility == another.compatibility) {
            // Ascending alphabetical ordering if compatibility is the same
            return 0 - getCandidate().getName().compareToIgnoreCase(another.getCandidate().getName());
        } else if (compatibility < another.compatibility) {
            return -1;
        } else {
            return 1;
        }
    }

    public Answer getCandidateAnswer(int questionId) {
        return answerMap.get(questionId);
    }

    private SparseArray<Answer> createAnswerMap() {
        List<Answer> answers = candidate.getAnswers();
        SparseArray<Answer> result = new SparseArray<>(answers.size());
        for (Answer answer : answers) {
            result.put(answer.getQuestionId(), answer);
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.candidate, flags);
        dest.writeFloat(this.compatibility);
        dest.writeSparseArray((SparseArray) this.answerMap);
    }

    protected CandidateCompatibility(Parcel in) {
        this.candidate = in.readParcelable(Candidate.class.getClassLoader());
        this.compatibility = in.readFloat();
        this.answerMap = in.readSparseArray(Answer.class.getClassLoader());
    }

    public static final Parcelable.Creator<CandidateCompatibility> CREATOR = new Parcelable.Creator<CandidateCompatibility>() {
        @Override
        public CandidateCompatibility createFromParcel(Parcel source) {
            return new CandidateCompatibility(source);
        }

        @Override
        public CandidateCompatibility[] newArray(int size) {
            return new CandidateCompatibility[size];
        }
    };
}
