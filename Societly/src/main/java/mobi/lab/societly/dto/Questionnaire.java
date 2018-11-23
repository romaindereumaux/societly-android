package mobi.lab.societly.dto;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Questionnaire implements Iterable<Question> {

    private List<Question> questions;
    private int id;

    public Questionnaire(int id, List<Question> questions) {
        this.id = id;
        this.questions = questions;
        Collections.sort(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return questions.size();
    }

    public boolean isCompleted() {
        for (Question question : questions) {
            if (!question.isAnswered()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAnsweredQuestions() {
        for (Question question : questions) {
            if (question.isAnswered()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<Question> iterator() {
        return questions.iterator();
    }

    @Override
    public String toString() {
        return "Questionnaire{" +
                "questions=" + questions +
                ", id=" + id +
                '}';
    }
}
