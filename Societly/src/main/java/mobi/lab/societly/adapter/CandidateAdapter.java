package mobi.lab.societly.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mobi.lab.societly.R;
import mobi.lab.societly.dto.Answer;
import mobi.lab.societly.dto.Candidate;
import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.ui.RoundedImageTransformation;
import mobi.lab.societly.util.ViewUtil;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {

    private static final int VIEWTYPE_ITEM = 0;
    private static final int VIEWTYPE_HEADER = 1;

    private CandidateCompatibility compatibility;
    private List<Question> userQuestions;

    public CandidateAdapter(Questionnaire userQuestionnaire, CandidateCompatibility compatibility) {
        this.userQuestions = userQuestionnaire.getQuestions();
        this.compatibility = compatibility;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEWTYPE_HEADER : VIEWTYPE_ITEM;
    }

    @Override
    public CandidateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root;
        if (VIEWTYPE_HEADER == viewType) {
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate_header, parent, false);
        } else {
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate_answer, parent, false);
        }
        return ViewHolder.create(root, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            bindItemViewHolder((ItemViewHolder) holder, position);
        } else {
            bindHeaderViewHolder((HeaderViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return userQuestions.size() + 1;
    }

    private void bindHeaderViewHolder(HeaderViewHolder holder, final int position) {
        Candidate candidate = compatibility.getCandidate();
        holder.name.setText(candidate.getName());
        holder.description.setText(candidate.getParty());
        Context context = holder.name.getContext();
        Picasso.with(context)
                .load(candidate.getImageUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.placeholder_rounded_image)
                .transform(new RoundedImageTransformation(context.getResources().getDimensionPixelSize(R.dimen.height_result_avatar) / 2))
                .into(holder.image);
    }

    private void bindItemViewHolder(ItemViewHolder holder, final int position) {
        // Position 0 has our header view..
        int questionPosition = position - 1;
        Question question = userQuestions.get(questionPosition);
        Answer userAnswer = question.getAnswer();
        Answer candidateAnswer = compatibility.getCandidateAnswer(question.getId());

        int bgResId = getAnswerBackground(userAnswer, candidateAnswer);
        holder.background.setBackgroundResource(bgResId);
        holder.question.setText(question.getText());
        holder.userAnswer.setText(question.getAnswer().getValueString());
        holder.candidateAnswer.setText(candidateAnswer.getValueString());
        holder.position.setText(ViewUtil.getPaddedNumber(questionPosition + 1));
    }

    private int getAnswerBackground(Answer a, Answer b) {
        int numA = a.getNumbericalValue();
        int numB = b.getNumbericalValue();

        int skipValue = Answer.AnswerType.SKIP.getValue();

        if (numA == skipValue || numB == skipValue) {
            return R.color.bg_answer_skipped;
        }

        int diff = Math.abs(numA - numB);
        if (diff == 0) {
            return R.color.bg_yes;
        } else if (diff == 25) {
            return R.color.bg_neutral;
        }
        return R.color.bg_no;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View root;

        public static ViewHolder create(View view, int viewType) {
            if (VIEWTYPE_ITEM == viewType) {
                return new ItemViewHolder(view);
            }
            return new HeaderViewHolder(view);
        }

        public ViewHolder(View v) {
            super(v);
            root = v;
        }
    }

    public static class ItemViewHolder extends ViewHolder {

        TextView question;
        TextView userAnswer;
        TextView candidateAnswer;
        TextView position;
        View background;

        public ItemViewHolder(View v) {
            super(v);
            question = (TextView) v.findViewById(R.id.text_question);
            userAnswer = (TextView) v.findViewById(R.id.text_user_answer);
            candidateAnswer = (TextView) v.findViewById(R.id.text_candidate_answer);
            position = (TextView) v.findViewById(R.id.text_question_position);
            background = v.findViewById(R.id.container_answers);
        }
    }

    public static class HeaderViewHolder extends ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView description;

        public HeaderViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.img_rounded);
            name = (TextView) v.findViewById(R.id.text_candidate_name);
            description = (TextView) v.findViewById(R.id.text_candidate_description);
        }
    }
}
