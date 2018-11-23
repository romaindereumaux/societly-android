package mobi.lab.societly.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.ThreadEnforcer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.Config;
import mobi.lab.societly.R;
import mobi.lab.societly.dto.Answer;
import mobi.lab.societly.dto.Candidate;
import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.dto.CandidateList;
import mobi.lab.societly.dto.Country;
import mobi.lab.societly.dto.Customer;
import mobi.lab.societly.dto.District;
import mobi.lab.societly.dto.LoginData;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.dto.State;
import mobi.lab.societly.dto.Submission;
import mobi.lab.societly.dto.SubmissionsItem;
import mobi.lab.societly.dto.SubmitAnswer;
import mobi.lab.societly.dto.SubmitData;
import mobi.lab.societly.dto.SubmitResponse;
import mobi.lab.societly.fragment.LoginFragment;
import mobi.lab.societly.network.BasicAuthInterceptor;
import mobi.lab.societly.network.SocietlyAPI;
import mobi.lab.societly.util.ApiCallFunction;
import mobi.lab.societly.util.SharedPrefsHelper;
import mobi.lab.societly.util.Util;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This is the central hub for in-memory data and data updates
 * NB: This gets lost then the app is serialized!
 * Created by Harri Kirik (harri35@gmail.com).
 */
// TODO refactor this to better separate different operations
// TODO improve background tasks and network calls with the added rxjava + retrofit2 plugins
public class Model {
    private static final String CACHE_CANDIDATE_RESULTS = "lab.mobi.societly.cache.RESULTS";

    private static volatile Model instance;
    private static Log log = Log.getInstance(Model.class);

    protected final Context context;
    protected final Gson gson;

    private final Bus bus;
    private final SocietlyAPI api;
    private final DatabaseHelper db;
    private final Handler ui;
    private final LruCache<String, CandidateCompatibiltyResult> cache;
    private String sessionCookie;
    private SubmitResultResp lastSubmitResult;

    public Model(final Context context) {
        this.context = context.getApplicationContext();
        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this); // Register itself as a provider
        gson = new Gson();

        final Retrofit retrofit = new Retrofit.Builder()
                .validateEagerly(true)
                .baseUrl(Util.getApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
                .client(getHttpClient())
                .build();

        api = retrofit.create(SocietlyAPI.class);
        db = new DatabaseHelper(this);
        ui = new Handler(Looper.getMainLooper());
        cache = new LruCache<>(1);
    }

    public OkHttpClient getHttpClient() {
        OkHttpClient httpClient = new OkHttpClient();
        OkHttpClient.Builder builder = httpClient.newBuilder();
        if (Config.DEBUG_LOGS) {
            HttpLoggingInterceptor.Logger logger = message -> log.v(message);
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(logger);
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logInterceptor);
        }
        httpClient = builder
                .addInterceptor(new BasicAuthInterceptor(Config.API_USERNAME, Config.API_PASSWORD))
                .addInterceptor(chain -> {
                    okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
                    requestBuilder.addHeader("Accept", "application/json");
                    addSessionHeader(requestBuilder, chain.request().url());
                    return chain.proceed(requestBuilder.build());
                })
                .connectionPool(new ConnectionPool(5, httpClient.connectTimeoutMillis(), TimeUnit.MILLISECONDS))
                .build();
        return httpClient;
    }

    private void addSessionHeader(okhttp3.Request.Builder builder, HttpUrl url) {
        List<String> ignoreUrls = new ArrayList<String>() {
            {
                add(String.format("%s%s", Util.getApiBaseUrl(), "customers"));
                add(String.format("%s%s", Util.getApiBaseUrl(), "customers/login"));
            }
        };
        log.d("addSessionHeader url=%s ignoreUrls=%s", url, ignoreUrls);
        if (hasSession() && !ignoreUrls.contains(url.toString())) {
            builder.addHeader("Cookie", getSessionCookie());
        }
    }

    public static Model getInstance(final Fragment fragment) {
        return getInstance(fragment.getActivity());
    }

    public static Model getInstance(final Context context) {
        if (instance == null) {
            // Only synchronize when we need to create the object
            // Yes, yes, I know, 'volatile' is also expensive
            synchronized (Model.class) {
                if (instance == null) {
                    instance = new Model(context);
                }
            }
        }
        return instance;
    }

    public void subscribe(final Object o) {
        if (o == null) {
            return;
        }
        log.d("subscribe: " + o);
        try {
            bus.register(o);
        } catch (Exception e) {
            log.e("subscribe", e);
        }
    }

    public void unsubscribe(final Object o) {
        if (o == null) {
            return;
        }
        log.d("unsubscribe: " + o);
        try {
            bus.unregister(o);
        } catch (Exception e) {
            log.e("unsubscribe", e);
        }
    }

    public void getQuestionnaire(final boolean prependTutorial) {
        new Thread(() -> {
            // use otto bus here in case we need to move this to another thread
            Question tutorial = null;
            final Questionnaire questionnaire;
            if (hasSavedUserQuestionnaire()) {
                questionnaire = db.loadUserQuestionnaire();
                if (prependTutorial && !(questionnaire.getQuestions().get(0).isDummyTutorial())) {
                    // Does not have a tutorial prepended
                    tutorial = Question.createDummyTutorialQuestion();
                    if (questionnaire.hasAnsweredQuestions()) {
                        // We need to mark the tutorial as answered if we have answered questions
                        // Otherwise our QuestionnaireAdapter will show the tutorial as the first unanswered question
                        log.d("Mark the dummy tutorial as answered");
                        tutorial.setAnswer(Answer.AnswerType.SKIP);
                    }
                }
            } else {
                log.d("getQuestionnaire: return a new questionnaire");
                questionnaire = db.loadTemplateQuestionnaire();
                if (prependTutorial) {
                    tutorial = Question.createDummyTutorialQuestion();
                }
            }
            log.d("Adding a dummy tutorial question to user's questionnaire");
            if (tutorial != null) {
                questionnaire.getQuestions().add(0, tutorial);
            }
            ui.post(() -> {
                log.d("getQuestionnaire: return saved questionnaire: " + gson.toJson(questionnaire));
                bus.post(new QuestionnaireResult(questionnaire));
            });
        }).start();
    }

    public void saveQuestionnaire(Questionnaire questionnaire) {
        // Filter out dummy tutorial questions in the questionnaire before saving
        // Do not modify the existing questionnaire object
        List<Question> filteredQuestions = new ArrayList<>();
        for (Question question : questionnaire) {
            if (question.isDummyTutorial()) {
                continue;
            }
            filteredQuestions.add(question);
        }
        db.saveUserQuestionnaire(new Questionnaire(questionnaire.getId(), filteredQuestions));
    }

    public boolean hasSavedUserQuestionnaire() {
        return db.loadUserQuestionnaire() != null;
    }

    public void clearResultsCache() {
        cache.remove(CACHE_CANDIDATE_RESULTS);
    }

    public void getCandidateResults() {
        CandidateCompatibiltyResult result = cache.get(CACHE_CANDIDATE_RESULTS);
        if (result != null) {
            bus.post(result);
            return;
        }
        new Thread(() -> {
            final Questionnaire questionnaire = db.loadUserQuestionnaire();

            CandidateList candidates = db.loadCandidates();

            List<CandidateCompatibility> countryList = createCompatibilityList(questionnaire, candidates.getCountryCandidates(), (c) -> 0L).get(0);
            LongSparseArray<List<CandidateCompatibility>> state = createCompatibilityList(questionnaire, candidates.getStateCandidates(), Candidate::getStateId);
            LongSparseArray<List<CandidateCompatibility>> district = createCompatibilityList(questionnaire, candidates.getDistrictCandidates(), Candidate::getDistrictId);
            if (countryList.isEmpty() || state == null || district == null) {
                ui.post(() -> bus.post(new CandidateCompatibiltyResult(new Error(context.getString(R.string.error_unanswered_questions)))));
                return;
            }

            final CandidateCompatibiltyResult newResult = new CandidateCompatibiltyResult(new Country(db.loadStates()), countryList, state, district);
            ui.post(() -> {
                cache.put(CACHE_CANDIDATE_RESULTS, newResult);
                bus.post(newResult);
            });
        }).start();
    }

    private LongSparseArray<List<CandidateCompatibility>> createCompatibilityList(Questionnaire questionnaire, List<Candidate> candidates, Func1<Candidate, Long> keyFunction) {
        LongSparseArray<List<CandidateCompatibility>> result = new LongSparseArray<>();
        for (Candidate candidate : candidates) {
            long key = keyFunction.call(candidate);
            result.put(key, Util.addOrCreateList(result.get(key), createCompatibility(candidate, questionnaire)));
        }
        // Sort all these lists
        for (int i = 0; i < result.size(); i++) {
            Collections.sort(result.get(result.keyAt(i)), Collections.reverseOrder());
        }
        return result;
    }

    private CandidateCompatibility createCompatibility(Candidate candidate, Questionnaire questionnaire) {
        CandidateCompatibility result = new CandidateCompatibility(candidate);
        result.calculateCompatibility(questionnaire);
        return result;
    }

    public void getCandidateOverview(final CandidateCompatibility compat) {
        Observable<CandidateOverviewResult> observable = Observable.create(sub -> {
            final Questionnaire userQuestionnaire = db.loadUserQuestionnaire();
            sub.onNext(new CandidateOverviewResult(compat, userQuestionnaire));
            sub.onCompleted();
        });
        observable.compose(applySchedulers())
                .subscribe(result -> {
                    bus.post(result);
                }, error -> {
                    bus.post(new CandidateOverviewResult(new Error(context.getString(R.string.error_candidate_not_found))));
                });
    }

    private AppState getLocalApplicationDataState(int delay) {
        if (db.loadTemplateQuestionnaire() == null || db.loadCandidates() == null || db.loadStates() == null) {
            // No saved data
            return AppState.NOT_LOADED;
        }

        // We already have data here, so let's just skip reloading
        log.d("getLocalApplicationDataState data exists");

        Questionnaire questionnaire = db.loadUserQuestionnaire();
        if (questionnaire == null && hasSession()) {
            log.d("getLocalApplicationDataState questionnaire==null and session exists");
            // We don't have an active questionnaire, let's get user's previous ones
            loadCustomerSubmissions();
        } else if (delay > 0) {
            // We have everything, let's just show the splash for some time
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                // Do nothing
            }
        }

        // Reload the user questionnaire
        questionnaire = db.loadUserQuestionnaire();
        final AppState state;
        if (questionnaire == null) {
            state = AppState.NEW;
        } else if (isResultSumbitted() && questionnaire.isCompleted()) {
            state = AppState.FINISHED;
        } else {
            state = AppState.IN_PROGRESS;
        }

        return state;
    }

    public void initApplicationData(int localDelay) {
        Observable<AppState> observable = Observable.create(sub -> {
            AppState localState = getLocalApplicationDataState(localDelay);
            log.d("initApplicationData localState=%s", localState);
            if (AppState.NOT_LOADED != localState) {
                sub.onNext(localState);
                sub.onCompleted();
                return;
            }

            log.d("initApplicationData load new data");
            Observable.merge(getQuestions(), getCandidates(), getStates())
                    .subscribe(aVoid -> {
                        // Don't care
                    }, error -> {
                        sub.onError(error);
                        log.e(error, "initApplicationData error");
                        bus.post(new ApplicationDataResult(new Error(error.getMessage())));
                    }, () -> sub.onNext(AppState.NEW));
        });
        observable.compose(applySchedulers())
                .subscribe(state -> {
                    bus.post(new ApplicationDataResult(state));
                }, error -> {
                    log.e(error, "initApplicationData error");
                    bus.post(new ApplicationDataResult(new Error(error.getMessage())));
                });
    }

    private Observable<Void> getQuestions() {
        return Observable.create(subscriber -> {
            api.getQuestions().enqueue(new Callback<List<Question>>() {
                @Override
                public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                    if (response.isSuccessful()) {
                        db.saveTemplateQuestionnaire(new Questionnaire(1, response.body()));
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(parseResponseException(response));
                    }
                }

                @Override
                public void onFailure(Call<List<Question>> call, Throwable t) {
                    subscriber.onError(t);
                }
            });
        });
    }

    private Observable<Void> getCandidates() {
        return Observable.create(subscriber -> {
            try {
                // Country
                Response<List<Candidate>> countryResp = api.getCandidates("country").execute();
                if (!countryResp.isSuccessful()) {
                    subscriber.onError(parseResponseException(countryResp));
                    return;
                }

                // State
                Response<List<Candidate>> stateResp = api.getCandidates("state").execute();
                if (!stateResp.isSuccessful()) {
                    subscriber.onError(parseResponseException(stateResp));
                    return;
                }

                // District
                Response<List<Candidate>> districtResp = api.getCandidates("district").execute();
                if (!districtResp.isSuccessful()) {
                    subscriber.onError(parseResponseException(districtResp));
                    return;
                }
                CandidateList result = new CandidateList(countryResp.body(), stateResp.body(), districtResp.body());
                db.saveCandidates(result);
                log.d("getCandidates result=%s", result);
                subscriber.onCompleted();
            } catch (IOException ioe) {
                subscriber.onError(ioe);
            }
        });
    }

    private Observable<Void> getStates() {
        return Observable.create(subscriber -> {
            // Get states sync
            // Get districts for each state

            try {
                Response<List<State>> stateResp = api.getStates().execute();
                if (!stateResp.isSuccessful()) {
                    subscriber.onError(parseResponseException(stateResp));
                    return;
                }
                List<State> states = stateResp.body();

                Response<List<District>> districtResp = api.getDistricts().execute();
                if (!districtResp.isSuccessful()) {
                    subscriber.onError(parseResponseException(districtResp));
                    return;
                }
                List<District> districts = districtResp.body();
                log.d("getStates states.size=%s districts.size=%s", states.size(), districts.size());

                addDistrictsToStates(states, districts);
                db.saveStates(states);

                subscriber.onCompleted();
            } catch (IOException ioe) {
                subscriber.onError(ioe);
            }
        });
    }

    private void addDistrictsToStates(List<State> states, List<District> districts) {
        LongSparseArray<List<District>> districtMap = new LongSparseArray<>();
        for (District district : districts) {
            long key = district.getStateId();
            // Negative number if not found
            if (districtMap.indexOfKey(key) >= 0) {
                districtMap.get(key).add(district);
            } else {
                districtMap.put(key, new ArrayList<District>() {
                    {
                        add(district);
                    }
                });
            }
        }
        for (State state : states) {
            state.setDistricts(districtMap.get(state.getId()));
        }
    }

    private Exception parseResponseException(Response response) {
        return new Exception(String.format(Locale.getDefault(), "%d: %s", response.code(), response.raw().request().url()));
    }

    public void resetQuestionnaire() {
        db.saveUserQuestionnaire(null);
        db.saveLandingPageShownTime(null);
        db.saveQuestionsShownTime(null);
        SharedPrefsHelper.setResultSubmitted(context, false);
        cache.evictAll();
    }

    public void resetHard() {
        logout();
        db.saveCandidates(null);
        db.saveUserQuestionnaire(null);
        db.saveTemplateQuestionnaire(null);
    }

    public void logout() {
        resetQuestionnaire();
        setSessionCookie(null);
    }

    public void submitResults(final String email) {
        Observable<SubmitResponse> observable = Observable.create(sub -> {
            Questionnaire userAnswers = db.loadUserQuestionnaire();
            List<SubmitAnswer> answers = new ArrayList<>(userAnswers.getSize());
            for (Question question : userAnswers) {
                Integer answerValue = question.getAnswer().getValue() == Answer.AnswerType.SKIP
                        ? null
                        : question.getAnswer().getNumbericalValue();
                answers.add(new SubmitAnswer(question.getId(), answerValue));
            }

            SubmitData data = createSubmitData(answers, email);
            try {
                Response<SubmitResponse> resp = api.submitResults(data).execute();
                if (!resp.isSuccessful()) {
                    sub.onError(parseResponseException(resp));
                }
                sub.onNext(resp.body());
                sub.onCompleted();
            } catch (IOException ioe) {
                sub.onError(ioe);
            }
        });
        observable.compose(applySchedulers())
                .subscribe(result -> {
                    final String clientHash = result.getClientHash();
                    log.d("submitResultsSuccess hash=%s", clientHash);
                    sendSubmitResultEvent(new SubmitResultResp(clientHash));
                }, error -> {
                    log.e(error, "submitResultsError error");
                    sendSubmitResultEvent(new SubmitResultResp(new Error(error.getMessage())));
                });
        SharedPrefsHelper.setResultSubmitted(context, true);
    }

    private SubmitData createSubmitData(List<SubmitAnswer> answers, String email) {
        String openPageAt = null;
        int questionToResult = 0;
        int startToResult = 0;
        GregorianCalendar landingShown = db.loadLandingPageShownTime();
        GregorianCalendar questionsShown = db.loadQuestionsShownTime();
        GregorianCalendar current = Util.getCurrentUTCCalendar();
        if (landingShown != null) {
            openPageAt = Util.getUTCString(landingShown);
            startToResult = Math.round((current.getTimeInMillis() - landingShown.getTimeInMillis()) / 1000.0f);
        }
        if (questionsShown != null) {
            questionToResult = Math.round((current.getTimeInMillis() - questionsShown.getTimeInMillis()) / 1000.0f);
        }
        log.d("createSubmitData landingShown=%s questionsShow=%s openPageAt=%s questionToResult=%d startToResult=%d",
                Util.getUTCString(landingShown), Util.getUTCString(questionsShown), openPageAt, questionToResult, startToResult);
        return new SubmitData(email, openPageAt, questionToResult, startToResult, answers);
    }

    private void sendSubmitResultEvent(SubmitResultResp event) {
        setLastSubmitResult(event);
        bus.post(event);
    }

    public boolean isResultSumbitted() {
        return SharedPrefsHelper.isResultSubmitted(context);
    }

    public void setQuestionsShownTime() {
        GregorianCalendar existing = db.loadQuestionsShownTime();
        if (existing != null) {
            // Value already exists
            return;
        }
        GregorianCalendar time = Util.getCurrentUTCCalendar();
        log.d("setQuestionsShownTime to " + Util.getUTCString(time));
        db.saveQuestionsShownTime(time);
    }

    public void setLandingPageShownTime() {
        GregorianCalendar existing = db.loadLandingPageShownTime();
        if (existing != null) {
            // Value already exists
            return;
        }
        GregorianCalendar time = Util.getCurrentUTCCalendar();
        log.d("setLandingPageShownTime to " + Util.getUTCString(time));
        db.saveLandingPageShownTime(time);
    }

    public void createAccount(LoginData loginData) {
        executeLoginAction(LoginFragment.Action.SIGN_UP, () -> api.signUp(loginData).execute());
    }

    public void signInAccount(LoginData loginData) {
        executeLoginAction(LoginFragment.Action.SIGN_IN, () -> api.signIn(loginData).execute());
    }

    private void executeLoginAction(LoginFragment.Action action, ApiCallFunction<Response<Customer>> apiCall) {
        Observable<Pair<Customer, AppState>> observable = Observable.create(sub -> {
            try {
                Response<Customer> resp = apiCall.call();
                if (!resp.isSuccessful()) {
                    sub.onError(parseResponseException(resp));
                    return;
                }
                parseSessionCookie(resp);
                Customer customer = resp.body();

                sub.onNext(Pair.create(customer, getLocalApplicationDataState(0)));
                sub.onCompleted();
            } catch (IOException ioe) {
                sub.onError(ioe);
            }
        });
        observable.compose(applySchedulers())
                .subscribe(pair -> {
                    bus.post(new LoginActionResult(action, pair.first, pair.second));
                }, throwable -> {
                    log.e(throwable, "executeLoginAction error");
                    bus.post(new LoginActionResult(action, new Error(throwable.getMessage())));
                });
    }

    private void loadCustomerSubmissions() {
        try {
            Response<List<SubmissionsItem>> submissionsResp = api.getCustomerSubmissions().execute();
            if (!submissionsResp.isSuccessful()) {
                log.e("getCustomerSubmissions error resp=%s", submissionsResp);
                return;
            }

            List<SubmissionsItem> submissions = submissionsResp.body();
            log.d("executeLoginAction submissions=%s", submissions);
            if (submissions.isEmpty()) {
                // No submissions
                return;
            }
            Response<Submission> submissionResp = api.getSubmission(submissions.get(0).getClientHash()).execute();
            if (!submissionResp.isSuccessful()) {
                log.e("getSubmission error resp=%s", submissionResp);
                return;
            }

            db.saveUserQuestionnaire(populateQuestionnaire(submissionResp.body()));
            SharedPrefsHelper.setResultSubmitted(context, true);
        } catch (IOException ioe) {
            log.e(ioe, "loadCustomerSubmissions");
            // Don't error when we cannot load the submissions
            // We'll just start a new questionnaire
        }
    }

    private Questionnaire populateQuestionnaire(Submission submission) {
        SparseArray<Answer> answers = new SparseArray<>();
        for (Answer answer : submission.getAnswers()) {
            answers.put(answer.getQuestionId(), answer);
        }

        Questionnaire src = db.loadTemplateQuestionnaire();
        log.d("populateQuestionnaire questons.size=%s answers.size=%s", src.getSize(), answers.size());
        for (Question question : src) {
            Answer answer = answers.get(question.getId());
            if (answer == null) {
                // TODO it should not be null, but it is for testing account
                log.e("populateQuestionnaire Q=%s answer not found", question.getId());
            }
            question.setAnswer(answer == null ? Answer.AnswerType.SKIP : answer.getValue());
            log.d("populateQuestionnaire Q=%s answer=%s", question.getId(), question.getAnswer());
        }
        return src;
    }

    private <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private synchronized void setSessionCookie(String token) {
        log.d("setSessionCookie %s", token);
        sessionCookie = token;
        SharedPrefsHelper.saveString(context, SharedPrefsHelper.PREF_SESSION, token);
    }

    private synchronized String getSessionCookie() {
        if (TextUtils.isEmpty(sessionCookie)) {
            sessionCookie = SharedPrefsHelper.getString(context, SharedPrefsHelper.PREF_SESSION);
        }
        log.d("getSessionCookie %s", sessionCookie);
        return sessionCookie;
    }

    public boolean hasSession() {
        return !TextUtils.isEmpty(getSessionCookie());
    }

    private boolean parseSessionCookie(Response<?> response) {
        String cookie = response.headers().get("Set-Cookie");
        if (TextUtils.isEmpty(cookie)) {
            return false;
        }
        setSessionCookie(cookie);
        return true;
    }

    public synchronized void setLastSubmitResult(SubmitResultResp lastSubmitResult) {
        log.d("setLastSubmitResult %s", lastSubmitResult);
        this.lastSubmitResult = lastSubmitResult;
    }

    @Produce
    public synchronized SubmitResultResp getLastSubmitResult() {
        log.d("getLastSubmitResult %s", lastSubmitResult);
        return lastSubmitResult;
    }
}

