package mobi.lab.societly.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import mobi.lab.societly.Config;
import mobi.lab.societly.R;
import mobi.lab.societly.activity.LoginActivity;
import mobi.lab.societly.dto.LoginData;
import mobi.lab.societly.model.LoginActionResult;
import mobi.lab.societly.util.Util;

public class LoginFragment extends BaseFragment {

    public enum Action {
        SIGN_UP, SIGN_IN
    }

    private static final String ARG_ACTION = "arg_action";
    private static final String TAG_PROGRESS_DIALOG = "mobi.lab.societly.fragment.LoginFragment.TAG_PROGRESS_DIALOG";
    private static final int REQUEST_GOOGLE_SIGN_IN = 1;

    private CallbackManager callbackManager;
    private Action action;
    private EditText inputUsername;
    private EditText inputPassword;
    private Button buttonAction;
    private GoogleApiClient googleClient;

    public static LoginFragment newInstance(Action action) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ACTION, action.name());
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        action = Action.valueOf(getArguments().getString(ARG_ACTION));
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        log.d("onSuccess");
                        executeLoginAction(LoginData.createFacebook(loginResult.getAccessToken().getToken()));
                    }

                    @Override
                    public void onCancel() {
                        log.w("onCancel");
                        Util.showToast(getContext(), getString(R.string.error_facebook_auth));
                    }

                    @Override
                    public void onError(FacebookException error) {
                        log.e(error, "onError");
                        Util.showToast(getContext(), getString(R.string.error_facebook_auth));
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputUsername = (EditText) view.findViewById(R.id.input_username);
        inputPassword = (EditText) view.findViewById(R.id.input_password);

        View previousButton = view.findViewById(R.id.btn_toolbar_previous);
        previousButton.setVisibility(View.VISIBLE);
        previousButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        ((TextView) view.findViewById(R.id.text_toolbar_title)).setText(getTitleResId());
        buttonAction = (Button) view.findViewById(R.id.btn_action);
        buttonAction.setText(getActionBtnTitleResId());
        buttonAction.setOnClickListener(v -> {
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();
            LoginData data = LoginData.create(username, password);
            if (action == Action.SIGN_IN) {
                model.signInAccount(data);
            } else {
                model.createAccount(data);
            }
        });

        Button btnFacebook = (Button) view.findViewById(R.id.btn_facebook);
        btnFacebook.setText(getFacebookBtnTitleResId());
        btnFacebook.setOnClickListener(v -> startFacebookLogin());

        Button btnGoogle = (Button) view.findViewById(R.id.btn_google);
        btnGoogle.setText(getGoogleBtnTitleResId());
        btnGoogle.setOnClickListener(v -> startGoogleLogin());

        initTextWatchers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (!result.isSuccess()) {
                log.w("onActivityResult google signin error status=%s", result.getStatus());
                Util.showToast(getContext(), R.string.error_google_auth);
                return;
            }
            // Login
            GoogleSignInAccount account = result.getSignInAccount();
            if (account == null) {
                log.w("onActivityResult google signin account is null status=%s", result.getStatus());
                Util.showToast(getContext(), R.string.error_google_auth);
                return;
            }
            executeLoginAction(LoginData.createGoogle(account.getServerAuthCode()));
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe
    public void onEvent(LoginActionResult event) {
        log.d("LoginActionResult event=%s", event);
        Util.dismissDialogFragment(getActivity(), TAG_PROGRESS_DIALOG);
        if (!event.isSuccess()) {
            Util.showToast(getContext(), R.string.error_login_auth);
            return;
        }

        // We have successfully logged in. Close this activity
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.EXTRA_APP_DATA_STATE, event.getAppDataState().name());
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void executeLoginAction(LoginData data) {
        if (getActivity() != null) {
            Util.showDialogFragment(getActivity(), ProgressDialogFragment.newInstance(), TAG_PROGRESS_DIALOG, this);
        }
        if (action == Action.SIGN_IN) {
            model.signInAccount(data);
        } else {
            model.createAccount(data);
        }
    }

    private int getTitleResId() {
        return action == Action.SIGN_IN ? R.string.label_btn_sign_in : R.string.label_btn_sign_up;
    }

    private int getFacebookBtnTitleResId() {
        return action == Action.SIGN_IN ? R.string.label_btn_sign_in_facebook : R.string.label_btn_sign_up_facebook;
    }

    private int getGoogleBtnTitleResId() {
        return action == Action.SIGN_IN ? R.string.label_btn_sign_in_google : R.string.label_btn_sign_up_google;
    }

    private int getActionBtnTitleResId() {
        return action == Action.SIGN_IN ? R.string.label_btn_sign_in : R.string.label_btn_sign_up;
    }

    private void updateActionButtonState() {
        String password = inputPassword.getText().toString();
        String username = inputPassword.getText().toString();
        buttonAction.setEnabled(!TextUtils.isEmpty(password) && !TextUtils.isEmpty(username));
    }

    private void initTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateActionButtonState();
            }
        };
        inputUsername.addTextChangedListener(watcher);
        inputPassword.addTextChangedListener(watcher);
        updateActionButtonState();
    }

    private void startFacebookLogin() {
        List<String> permissions = new ArrayList<>();
        permissions.add("email");
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, permissions);
    }

    private void startGoogleLogin() {
        if (googleClient == null) {
            initGoogleApiClient();
        }

        if (googleClient.isConnected()) {
            // So that we could pick the account after first try
            googleClient.clearDefaultAccountAndReconnect();
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
    }

    private void initGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(Config.GOOGLE_SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        googleClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(), connectionResult -> {
                    log.w("Connection failed result %s", connectionResult);
                    Util.showToast(getContext(), R.string.error_google_auth);
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
}
