package mobi.lab.societly.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mobi.lab.societly.R;
import mobi.lab.societly.activity.LoginActivity;
import mobi.lab.societly.activity.QuestionnaireActivity;
import mobi.lab.societly.model.AppState;
import mobi.lab.societly.util.Util;

public class LandingPageFragment extends BaseFragment {

    private static final int REQUEST_LOGIN = 1;

    public static LandingPageFragment newInstance() {
        return new LandingPageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_landing_page, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_skip).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuestionnaireActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        view.findViewById(R.id.btn_sign_in).setOnClickListener(v -> {
            startLogin(LoginActivity.ACTION_SIGN_IN);
        });
        view.findViewById(R.id.btn_sign_up).setOnClickListener(v -> {
            startLogin(LoginActivity.ACTION_SIGN_UP);
        });
        if (savedInstanceState == null) {
            model.setLandingPageShownTime();
        }
    }

    private void startLogin(String action) {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setAction(action);
        startActivityForResult(intent, REQUEST_LOGIN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log.d("onActivityResult requestCode=%d resultCode=%s", requestCode, resultCode);
        if (requestCode == REQUEST_LOGIN && resultCode == Activity.RESULT_OK) {
            startActivity(Util.getAppEntryIntent(getContext(), getAppStateExtra(data)));
            getActivity().finish();
        }
    }

    private AppState getAppStateExtra(Intent intent) {
        String rawString = intent.getStringExtra(LoginActivity.EXTRA_APP_DATA_STATE);
        if (TextUtils.isEmpty(rawString)) {
            return AppState.NEW;
        }
        return AppState.valueOf(rawString);
    }
}
