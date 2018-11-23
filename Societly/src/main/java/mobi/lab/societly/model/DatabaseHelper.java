package mobi.lab.societly.model;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import mobi.lab.societly.dto.CandidateList;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.dto.State;
import mobi.lab.societly.util.SharedPrefsHelper;

public class DatabaseHelper {

    private Model model;

    public DatabaseHelper(Model model) {
        this.model = model;
    }

    public void saveUserQuestionnaire(Questionnaire questionnaire) {
        saveObject(SharedPrefsHelper.PREF_QUESTIONNAIRE, questionnaire);
    }

    public Questionnaire loadUserQuestionnaire() {
        return loadObject(SharedPrefsHelper.PREF_QUESTIONNAIRE, Questionnaire.class);
    }

    public void saveTemplateQuestionnaire(Questionnaire questionnaire) {
        saveObject(SharedPrefsHelper.PREF_TEMPLATE_QUESTIONNAIRE, questionnaire);
    }

    public Questionnaire loadTemplateQuestionnaire() {
        return loadObject(SharedPrefsHelper.PREF_TEMPLATE_QUESTIONNAIRE, Questionnaire.class);
    }

    public void saveCandidates(CandidateList candidates) {
        saveObject(SharedPrefsHelper.PREF_CANDIDATE_LIST, candidates);
    }

    public CandidateList loadCandidates() {
        return loadObject(SharedPrefsHelper.PREF_CANDIDATE_LIST, CandidateList.class);
    }

    public GregorianCalendar loadQuestionsShownTime() {
        return loadCalendar(SharedPrefsHelper.PREF_QUESTIONS_SHOWN);
    }

    public void saveQuestionsShownTime(GregorianCalendar time) {
        saveObject(SharedPrefsHelper.PREF_QUESTIONS_SHOWN, time);
    }

    public void saveStates(List<State> states) {
        Type token = new TypeToken<List<State>>() {}.getType();
        String data = model.gson.toJson(states, token);
        SharedPrefsHelper.saveString(model.context, SharedPrefsHelper.PREF_STATES, data);
    }

    public List<State> loadStates() {
        String raw = SharedPrefsHelper.getString(model.context, SharedPrefsHelper.PREF_STATES);
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        Type token = new TypeToken<List<State>>() {}.getType();
        return model.gson.fromJson(raw, token);
    }

    public GregorianCalendar loadLandingPageShownTime() {
        return loadCalendar(SharedPrefsHelper.PREF_LANDING_SHOWN);
    }

    public void saveLandingPageShownTime(GregorianCalendar time) {
        saveObject(SharedPrefsHelper.PREF_LANDING_SHOWN, time);
    }

    private GregorianCalendar loadCalendar(String key) {
        GregorianCalendar cal = loadObject(key, GregorianCalendar.class);
        if (cal != null) {
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return cal;
    }

    private void saveObject(String key, Object o) {
        String data = model.gson.toJson(o);
        SharedPrefsHelper.saveString(model.context, key, data);
    }

    private <T> T loadObject(String key, Class<T> cls) {
        String data = SharedPrefsHelper.getString(model.context, key);
        if (data == null) {
            return null;
        }
        return model.gson.fromJson(data, cls);
    }
}