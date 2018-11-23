package mobi.lab.societly.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.Config;
import mobi.lab.societly.activity.LandingPageActivity;
import mobi.lab.societly.activity.QuestionnaireActivity;
import mobi.lab.societly.activity.ResultsListActivity;
import mobi.lab.societly.dto.Answer;
import mobi.lab.societly.dto.Candidate;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.model.AppState;
import mobi.lab.societly.model.Model;

public class Util {

    public static boolean isValidEmail(String text) {
        return !TextUtils.isEmpty(text) && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    public static String getApiBaseUrl() {
        return Config.API_BASE_URL + Config.API_CAMPAIGN + "/" + Config.API_LANG + "/";
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageResId) {
        showToast(context, context.getResources().getString(messageResId));
    }

    public static GregorianCalendar getCurrentUTCCalendar() {
        return new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    }

    public static String getUTCString(GregorianCalendar calendar) {
        if (calendar == null) {
            return "null";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(calendar.getTime());
    }

    public Questionnaire createDummyQuestionnaire() {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(new Question(1, 1, "1 Are you ok?", null));
        questions.add(new Question(2, 2, "2 Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book?", null));
        questions.add(new Question(3, 3, "3 Is that man ok or are you blabla bla bla blabla blalalala okok?", null));
        questions.add(new Question(4, 4, "4 Is that woman ok or are you blabla bla bla blabla blalalala okok?", null));
        questions.add(new Question(5, 5, "5 Who is ok?", null));
        questions.add(new Question(6, 6, "6 oo?", null));
        questions.add(new Question(7, 7, "7 test ??", null));
        questions.add(new Question(8, 8, "8 Final question", null));
        return new Questionnaire(1, questions);
    }

    public List<Candidate> createDummyCandidates() {
        List<Candidate> results = new ArrayList<>();
        int questionCount = 8;
        // Let's create questionnaires here
        for (int i = 0; i < 6; i++) {
            // 6 Candidates
            //            Questionnaire candidateQuestionnaire = createDummyQuestionnaire();
            Questionnaire candidateQuestionnaire = createDummyQuestionnaire();
            // How many consecutive questions will have the same result
            int questionStep = i + 1;
            // index where we will start setting answers (for diversification)
            int startIndex = (i * questionStep);

            // We need to run the loop until all indices from 0 - questionCount have been run through
            int maxSum = 0;
            for (int index = 0; index < questionCount; index++) {
                maxSum += index;
                int sum = 0;
                for (int j = startIndex; sum < maxSum; j += questionStep) {
                    int questionIndex = j % questionCount;
                    // What type of answer we will use
                    int answerTypeIndex = questionIndex % 5;
                    // Now answer questionStep number on consecutive questions
                    for (int k = 0; k < questionStep; k++) {
                        // Iterate the loop from start if we have gone beyond the last item
                        int actualQuestionIndex = (questionIndex + k) % questionCount;
                        candidateQuestionnaire.getQuestions().get(actualQuestionIndex).setAnswer(Answer.AnswerType.values()[answerTypeIndex]);
                        sum += actualQuestionIndex;
                        // If we have answered all questions from 0 - questionCount then break
                        if (sum >= maxSum) {
                            break;
                        }
                    }
                }
            }
            List<Answer> answers = new ArrayList<>();
            for (Question q : candidateQuestionnaire) {
                answers.add(q.getAnswer());
            }

            Candidate candidate = new Candidate(i, "Candidate " + i, answers);
            results.add(candidate);
        }
        return results;
    }

    public static void showDialogFragment(final FragmentActivity activity, final DialogFragment dialog, final String tag, final Fragment targetFragment) {
        if (activity == null || dialog == null || TextUtils.isEmpty(tag)) {
            Log.getInstance(Util.class).e("showDialogFragment: activity == null || dialog == null || TextUtils.isEmpty(tag)");
            // Fail
            return;
        }

        if (activity.isFinishing()) {
            // No need
            Log.getInstance(Util.class).w("showDialogFragment: activity.isFinishing()");
            return;
        }

        FragmentManager manager = activity.getSupportFragmentManager();

        try {
            final FragmentTransaction ft = manager.beginTransaction();
            removePreviousFragmentIfAny(activity, ft, tag);
            // Create and show the dialog.
            dialog.setTargetFragment(targetFragment, 0);
            dialog.show(ft, tag);
        } catch (IllegalStateException e) {
            Log.getInstance(Util.class).e("showDialogFragment:", e);
        }

    }

    protected static void removePreviousFragmentIfAny(final FragmentActivity activity, final FragmentTransaction ft, final String tag) {
        if (activity == null || ft == null || TextUtils.isEmpty(tag)) {
            return;
        }
        final Fragment previousFragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (previousFragment != null) {
            // Remove the previous one?
            ft.remove(previousFragment);
        }
    }

    public static void dismissDialogFragment(final FragmentActivity activity, final String tag) {
        try {
            final DialogFragment fragment = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                return;
            }
            fragment.dismiss();
            Log.getInstance(Util.class).d("dismissDialogFragment: dismissed");
        } catch (IllegalStateException e) {
            Log.getInstance(Util.class).e("dismissDialogFragment:", e);
        }
    }

    public static void onNavigateUp(Activity activity) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(upIntent);
        activity.finish();
    }

    public static Intent getAppEntryIntent(Context context, AppState appState) {
        boolean hasSession = Model.getInstance(context).hasSession();
        Class cls;
        switch (appState) {
            case FINISHED:
                cls = ResultsListActivity.class;
                break;
            case IN_PROGRESS:
                cls = QuestionnaireActivity.class;
                break;
            default:
                if (hasSession) {
                    cls = QuestionnaireActivity.class;
                } else {
                    cls = LandingPageActivity.class;
                }

        }
        Log.getInstance("Util").d("getAppEntryIntent hasSession=%s state=%s cls=%s", hasSession, appState, cls.getName());
        return new Intent(context, cls);
    }

    public static void restartAppWithIntent(Activity activity, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void restartAppWithIntent(Activity activity, Class targetActivity) {
        restartAppWithIntent(activity, new Intent(activity, targetActivity));
    }

    public static int sizeOf(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T> List<T> addOrCreateList(List<T> source, T item) {
        if (source == null) {
            source = new ArrayList<T>();
        }
        source.add(item);
        return source;
    }
}
