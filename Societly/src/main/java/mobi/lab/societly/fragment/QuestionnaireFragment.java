package mobi.lab.societly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.otto.Subscribe;

import mobi.lab.societly.R;
import mobi.lab.societly.activity.EmailActivity;
import mobi.lab.societly.activity.ResultsListActivity;
import mobi.lab.societly.adapter.QuestionnaireAdapter;
import mobi.lab.societly.dto.Answer;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.model.QuestionnaireResult;
import mobi.lab.societly.util.Util;
import mobi.lab.societly.util.ViewUtil;

public class QuestionnaireFragment extends BaseFragment {

    private static final String STATE_POSITION = "lab.mobi.societly.STATE_POSITION";
    private static final int POSITION_NOT_SAVED = -1;

    private QuestionnaireAdapter adapter;
    private SwipeFlingAdapterView flingContainer;
    private float previousPercentage = 0f;

    private View buttonPrevious;
    private View buttonNext;
    private TextView title;
    private int savedPosition = POSITION_NOT_SAVED;

    public static QuestionnaireFragment newInstance() {
        return new QuestionnaireFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getInt(STATE_POSITION, POSITION_NOT_SAVED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questionnaire, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.container_questions);
        title = (TextView) view.findViewById(R.id.text_toolbar_title);
        buttonNext = view.findViewById(R.id.btn_toolbar_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.invalidateActiveCard();
                adapter.showNextItem();
                updateToolbar();
            }
        });
        buttonPrevious = view.findViewById(R.id.btn_toolbar_previous);
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.invalidateActiveCard();
                adapter.showPreviousItem();
                updateToolbar();
            }
        });
        model.getQuestionnaire(true);
        if (savedInstanceState == null) {
            model.setQuestionsShownTime();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && adapter.getCount() == 1 && adapter.getItem(0).isAnswered()) {
            /*
             Showing the last item and it has been answered.
             Call notifyDataSetChanged here to refresh the ui when we navigate back to this
             screen after we've swiped away the last item. This makes it visible again.
            */
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            model.saveQuestionnaire(adapter.getQuestionnaire());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putInt(STATE_POSITION, adapter.getPosition());
        }
    }

    @Subscribe
    public void onGetQuestionnaire(QuestionnaireResult result) {
        log.d("onGetQuestionnaire result=" + result);
        if (result.isSuccess()) {
            adapter = new QuestionnaireAdapter(getContext(), result.getQuestionnaire());
            if (savedPosition != POSITION_NOT_SAVED) {
                log.d("restore saved position to " + savedPosition);
                adapter.setPosition(savedPosition);
            }
            initFlingContainer();
        } else {
            Util.showToast(getContext(), result.getError().getMessage());
        }
    }

    private void continueToNextStep() {
        // Remove the dummy tutorial item if needed
        model.clearResultsCache();
        model.saveQuestionnaire(adapter.getQuestionnaire());

        Class target = EmailActivity.class;
        if (model.hasSession()) {
            model.submitResults(null);
            target = ResultsListActivity.class;
        }
        startActivity(new Intent(getContext(), target));
    }

    private void updateToolbar() {
        /*
         Our first element is the tutorial card, which means that size has 1 extra item.
         currentPosition would need an added one to make the indices star from 1 instead of 0,
         but since we need to remove the extra tutorial item, then we won't change the value
         */
        int size = adapter.getQuestionnaire().getSize() - 1;
        int currentPosition = Math.min(adapter.getPosition(), size);
        if (currentPosition == 0) {
            // Tutorial is showing
            title.setText("");
        } else {
            title.setText(getString(R.string.title_toolbar_position, ViewUtil.getPaddedNumber(currentPosition), ViewUtil.getPaddedNumber(size)));
        }
        int nextVisibility = adapter.canShowNextItem() ? View.VISIBLE : View.INVISIBLE;
        int prevVisibility = adapter.canShowPreviousItem() ? View.VISIBLE : View.INVISIBLE;
        buttonNext.setVisibility(nextVisibility);
        buttonPrevious.setVisibility(prevVisibility);
    }

    private void initFlingContainer() {
        flingContainer.setAdapter(adapter);
        flingContainer.requestLayout();
        updateToolbar();
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            private void answerQuestion(Object object, Answer.AnswerType answer) {
                Question question = (Question) object;
                question.setAnswer(answer);
                log.d("Answer question " + question + " with " + answer);
                adapter.showNextItem();
                if (!adapter.isEmpty()) {
                    // No need to update after all questions have been answered
                    updateToolbar();
                }
            }


            private View getAreaHintView(int area) {
                View view = null;
                View selectedView = flingContainer.getSelectedView();
                switch (area) {
                    case AREA_TOP:
                        view = selectedView.findViewById(R.id.text_hint_neutral);
                        break;
                    case AREA_RIGHT:
                        view = selectedView.findViewById(R.id.text_hint_yes);
                        break;
                    case AREA_BOTTOM:
                        view = selectedView.findViewById(R.id.text_hint_skip);
                        break;
                    case AREA_LEFT:
                        view = selectedView.findViewById(R.id.text_hint_no);
                        break;
                }
                return view;
            }

            private void setHintAlpha(int area, float alpha) {
                View hint = getAreaHintView(area);
                if (hint != null) {
                    hint.setAlpha(alpha);
                }
            }

            /**
             * If an exisitng answer is provided, then set the hints based on the answer type.
             * Otherwise hide all hints
             * @param answer
             */
            private void setAreaHints(Answer answer) {
                float[] alphas = ViewUtil.getAnswerHintAlphas(answer);
                log.v("setAreaHints answer=" + answer + " right=" + alphas[0] + " left=" + alphas[1] + " top=" + alphas[2] + " bottom=" + alphas[3]);
                setHintAlpha(AREA_RIGHT, alphas[0]);
                setHintAlpha(AREA_LEFT, alphas[1]);
                setHintAlpha(AREA_TOP, alphas[2]);
                setHintAlpha(AREA_BOTTOM, alphas[3]);
            }

            private void resetBackground(Answer answer) {
                int bgColor = ViewUtil.getColor(getResources(), R.color.bg_default);
                if (answer != null) {
                    bgColor = ViewUtil.getAnswerColor(getContext(), answer);
                }
                CardView card = (CardView) flingContainer.getSelectedView().findViewById(R.id.question_card);
                card.setCardBackgroundColor(bgColor);
            }

            @Override
            public void removeFirstObjectInAdapter() {
                // Do nothing
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                answerQuestion(dataObject, Answer.AnswerType.NO);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                answerQuestion(dataObject, Answer.AnswerType.YES);
            }

            @Override
            public void onTopCardExit(Object dataObject) {
                answerQuestion(dataObject, Answer.AnswerType.NEUTRAL);
            }

            @Override
            public void onBottomCardExit(Object dataObject) {
                answerQuestion(dataObject, Answer.AnswerType.SKIP);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                log.v("onAdapterAboutToEmpty itemsInAdapter=" + itemsInAdapter + " adapterCount=" + adapter.getCount());
                if (!isResumed()) {
                    return;
                }
                // We are showing the last item and it has been answered
                if (adapter.getCount() == 1 && adapter.getItem(0).isAnswered()) {
                    // We get multiple calls here because the library
                    // calls this on each layout creation

                    continueToNextStep();
                }
            }

            @Override
            public void onAreaChanged(int oldArea, int newArea) {
                log.v("onAreaChanged old=" + oldArea + " new=" + newArea);
                // Don't ignore onMove callbacks when the area has change
                previousPercentage = -1;
                if (adapter.getItem(0).isDummyTutorial()) {
                    log.v("onAreaChanged ignore, dummy tutorial is active");
                    return;
                }
                View selectedView = flingContainer.getSelectedView();
                if (selectedView == null) {
                    log.v("onAreaChanged ignore, selectedView == null");
                    // We get notifications here after the swiped view has animated
                    // and before a new view has been added, so skip these callbacks
                    return;
                }

                /**
                 * If we have moved to the center and our question has an existing answer
                 * then we need to reset the background and hint based on the answer.
                 * If we move to an area and the question has already been answered with an answer
                 * that belongs to the area, then we have to skip changing hint alpha
                 * Otherwise clear the background to default and clear all hints
                 */
                Question question = adapter.getItem(0);
                Answer answer = question.getAnswer();
                if (newArea == AREA_CENTER) {
                    resetBackground(answer);
                } else if (answer != null && !answer.getValue().equals(ViewUtil.getAreaAnswerType(newArea))) {
                    // Moved to a new area that is not the same as our existing answer's area -> clear all hints
                    answer = null;
                }

                // Skip resetting the hints when we show our dummy tutorial since the layout is different
                if (adapter.getItem(0).isDummyTutorial()) {
                    return;
                }

                /**
                 * If we have an existing answer object, then the hint alphas
                 * are set based on the answer value, otherwise all hints are cleared
                 */
                setAreaHints(answer);
            }

            @Override
            public void onMove(int area, float percentage) {
                log.v("onMove area=" + area + " percentage=" + percentage);
                if (AREA_OUT_OF_BOUNDS == area || AREA_CENTER == area) {
                    log.v("onMove ignore, CENTER or OUT_OF_BOUNDS");
                    return;
                }
                if (Math.abs(previousPercentage - percentage) < 0.01) {
                    log.v("onMove ignore, not enought movement");
                    // Skip update
                    return;
                }
                previousPercentage = percentage;

                if (adapter.getItem(0).isDummyTutorial()) {
                    log.v("onMove ignore, dummy tutorial is active");
                    return;
                }

                View selectedView = flingContainer.getSelectedView();
                if (selectedView == null) {
                    log.v("onMove ignore, dummy tutorial is active");
                    // We get notifications here after the swiped view has animated
                    // and before a new view has been added, so skip these callbacks
                    return;
                }

                View hintView = getAreaHintView(area);
                int foreground = ViewUtil.getAreaColor(getContext(), area);
                int background;
                float hintAlpha = percentage;

                Question question = adapter.getItem(0);
                if (question.isAnswered()) {
                    Answer answer = question.getAnswer();
                    Answer.AnswerType areaAnswer = ViewUtil.getAreaAnswerType(area);
                    if (answer.getValue().equals(areaAnswer)) {
                        // Moved to the same area as our existing answer, keep the hint visible
                        hintAlpha = 1.f;
                    }
                    background = ViewUtil.getAnswerColor(getContext(), answer);
                } else {
                    background = ViewUtil.getColor(getResources(), R.color.bg_default);
                }
                CardView card = (CardView) selectedView.findViewById(R.id.question_card);
                if (hintView == null) {
                    card.setCardBackgroundColor(foreground);
                } else {
                    hintView.setAlpha(hintAlpha);
                    // Blend our background and foreground colors into one
                    int foregroundWithAlpha = ViewUtil.setAlpha(percentage, foreground);
                    card.setCardBackgroundColor(ViewUtil.blendColors(foregroundWithAlpha, background));
                }
            }

            @Override
            public void onTopCardInit(View card) {
                if (card instanceof CardView) {
                    ((CardView) card).setCardElevation(getResources().getDimension(R.dimen.elevation_top_question_card));
                }
            }
        });
    }
}
