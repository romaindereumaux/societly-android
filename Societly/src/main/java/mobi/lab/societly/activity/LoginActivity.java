package mobi.lab.societly.activity;

import android.os.Bundle;
import android.text.TextUtils;

import mobi.lab.societly.R;
import mobi.lab.societly.fragment.LoginFragment;

public class LoginActivity extends BaseActivity {

    public static final String ACTION_SIGN_IN = LoginActivity.class.getName() + ".ACTION_SIGN_IN";
    public static final String ACTION_SIGN_UP = LoginActivity.class.getName() + ".ACTION_SIGN_UP";
    public static final String EXTRA_APP_DATA_STATE = LoginActivity.class.getName() + ".EXTRA_APP_DATA_STATE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, LoginFragment.newInstance(getActionFromIntent()))
                    .commit();
        }
    }

    private LoginFragment.Action getActionFromIntent() {
        String intentAction = getIntent().getAction();
        if (TextUtils.equals(ACTION_SIGN_UP, intentAction)) {
            return LoginFragment.Action.SIGN_UP;
        }
        return LoginFragment.Action.SIGN_IN;
    }
}
