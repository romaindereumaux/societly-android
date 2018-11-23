package mobi.lab.societly.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.R;
import mobi.lab.societly.dto.Answer;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.util.ViewUtil;

public class QuestionnaireAdapter extends BaseAdapter {

    private Context context;
    private Questionnaire questionnaire;
    private LayoutInflater inflater;
    // Position of the active question
    private int currentPosition;
    private Log log = Log.getInstance(this);

    public QuestionnaireAdapter(Context context, Questionnaire questionnaire) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.questionnaire = questionnaire;
        this.context = context;
        initCurrentPosition();
    }

    /**
     * Looks for the first unanswered question in our questionnaire
     */
    public void initCurrentPosition() {
        currentPosition = 0;
        for (Question question : questionnaire) {
            if (question.isAnswered()) {
                log.d("Question " + question + " answered");
                currentPosition++;
            } else {
                log.d("First unanswered question is at index " + currentPosition);
                break;
            }
        }
        // In case all the questions have been answered, show the last question
        currentPosition = Math.min(currentPosition, questionnaire.getSize() - 1);
        log.d("initCurrentPosition currentPosition=" + currentPosition + " questionnaire.size=" + questionnaire.getSize());
    }

    @Override
    public int getCount() {
        return questionnaire.getSize() - currentPosition;
    }

    @Override
    public Question getItem(int position) {
        return questionnaire.getQuestions().get(position + currentPosition);
    }

    @Override
    public long getItemId(int position) {
        return position + currentPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Question question = getItem(position);
        if (question.isDummyTutorial()) {
            return inflater.inflate(R.layout.item_tutorial, parent, false);
        }

        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_question, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text_question);
            holder.card = (CardView) convertView.findViewById(R.id.question_card);
            holder.hintYes = convertView.findViewById(R.id.text_hint_yes);
            holder.hintNo = convertView.findViewById(R.id.text_hint_no);
            holder.hintSkip = convertView.findViewById(R.id.text_hint_skip);
            holder.hintNeutral = convertView.findViewById(R.id.text_hint_neutral);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(question.getText());
        Answer answer = question.getAnswer();
        if (answer != null) {
            setAnswerColor(answer, holder);
            setAnswerHint(answer, holder);
        }
        return convertView;
    }

    public void showPreviousItem() {
        setPosition(currentPosition - 1);
    }

    public void showNextItem() {
        if (isActiveQuestionAnswered()) {
            setPosition(currentPosition + 1);
        }
    }

    public boolean canShowPreviousItem() {
        return currentPosition > 0;
    }

    public boolean canShowNextItem() {
        return currentPosition < questionnaire.getSize() - 1 && isActiveQuestionAnswered();
    }

    private boolean isActiveQuestionAnswered() {
        return getItem(0).isAnswered();
    }

    public void setPosition(int index) {
        if (index < 0 || index >= questionnaire.getSize()) {
            return;
        }
        currentPosition = index;
        notifyDataSetChanged();
    }

    public int getPosition() {
        return currentPosition;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    private void setAnswerColor(Answer answer, ViewHolder holder) {
        int color = ViewUtil.getAnswerColor(context, answer);
        holder.card.setCardBackgroundColor(color);
    }

    private void setAnswerHint(Answer answer, ViewHolder holder) {
        float[] alphas = ViewUtil.getAnswerHintAlphas(answer);
        holder.hintYes.setAlpha(alphas[0]);
        holder.hintNo.setAlpha(alphas[1]);
        holder.hintNeutral.setAlpha(alphas[2]);
        holder.hintSkip.setAlpha(alphas[3]);
    }

    private static class ViewHolder {
        TextView text;
        CardView card;
        View hintYes;
        View hintNo;
        View hintNeutral;
        View hintSkip;
    }
}
