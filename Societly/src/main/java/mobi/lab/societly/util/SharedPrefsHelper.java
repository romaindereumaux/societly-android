package mobi.lab.societly.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class for keeping all the shared prefs at one place
 */
public class SharedPrefsHelper {

    public static final String PREF_QUESTIONNAIRE = "lab.mobi.societly.PREF_QUESTIONNAIRE";
    public static final String PREF_TEMPLATE_QUESTIONNAIRE = "lab.mobi.societly.PREF_TEMPLATE_QUESTIONNAIRE";
    public static final String PREF_CANDIDATES = "lab.mobi.societly.PREF_CANDIDATES";
    public static final String PREF_CANDIDATE_LIST = "lab.mobi.societly.PREF_CANDIDATE_LIST";
    public static final String PREF_RESULT_SUBMITTED = "lab.mobi.societly.RESULT_SUBMITTED";
    public static final String PREF_QUESTIONS_SHOWN = "lab.mobi.societly.QUESTIONS_SHOWN";
    public static final String PREF_LANDING_SHOWN = "lab.mobi.societly.LANDING_SHOWN";
    public static final String PREF_STATES = "lab.mobi.societly.STATES";
    public static final String PREF_SESSION = "lab.mobi.societly.PREF_SESSION";

    public static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setResultSubmitted(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(PREF_RESULT_SUBMITTED, value);
        editor.apply();
    }

    public static boolean isResultSubmitted(Context context) {
        return getSharedPrefs(context).getBoolean(PREF_RESULT_SUBMITTED, false);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        return getSharedPrefs(context).getString(key, null);
    }
}
