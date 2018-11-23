package mobi.lab.societly.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.BuildConfig;
import mobi.lab.societly.model.Model;

public class BaseActivity extends FragmentActivity {

    protected Log log = Log.getInstance(this);
    protected Model model;
    protected boolean resumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = Model.getInstance(this);
        model.subscribe(this);
        if (!BuildConfig.DEBUG) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        log.d("onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.d("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.d("onResume");
        resumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.d("onPause");
        resumed = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.d("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.d("onDestroy");
        model.unsubscribe(this);
        model = null;
    }
}
