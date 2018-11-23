package mobi.lab.societly.dto;

import android.support.annotation.NonNull;

public class Question implements Comparable {

    /* We need to have a type field here to be able to save our tutorial card
    answered state via gson. Otherwise we could create a subclass of Question
      */
    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_TUTORIAL = 1;

    private int id;
    private int position;
    private String name;
    private Answer answer;
    private int type;

    public Question(int id, int position, String name, Answer answer) {
        this.id = id;
        this.position = position;
        this.name = name;
        this.answer = answer;
        this.type = TYPE_REGULAR;
    }

    public static Question createDummyTutorialQuestion() {
        Question q = new Question(0, 0, "tutorial", null);
        q.type = Question.TYPE_TUTORIAL;
        return q;
    }

    public void setAnswer(Answer.AnswerType type) {
        this.answer = new Answer(type);
        answer.setQuestionId(id);
    }

    public Answer getAnswer() {
        return answer;
    }

    public String getText() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isAnswered() {
        return this.answer != null;
    }

    public boolean isDummyTutorial() {
        return TYPE_TUTORIAL == type;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", position=" + position + 
                ", text='" + getText() + '\'' +
                ", answer=" + answer +
                '}';
    }

    @Override
    public int compareTo(@NonNull Object object) {
        Question another = (Question) object;
        return position - another.position;
    }
}
