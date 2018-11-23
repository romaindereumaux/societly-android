package mobi.lab.societly.activity;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Subscribe;

import mobi.lab.societly.R;
import mobi.lab.societly.model.ApplicationDataResult;
import mobi.lab.societly.util.Util;

public class SplashActivity extends BaseActivity {

    private View compass;
    private ObjectAnimator anim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        compass = findViewById(R.id.img_compass_needle);
        anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.anim_rotate);
        anim.setTarget(compass);
        anim.start();
        model.initApplicationData(2000);
    }

    @Subscribe
    public void onApplicationDataReceived(ApplicationDataResult result) {
        log.i("onApplicationDataReceived result=%s", result);
        if (result.isSuccess()) {
            startActivity(Util.getAppEntryIntent(this, result.getState()));
            anim.cancel();
            finish();
        } else {
            Util.showToast(this, getString(R.string.error_init_app_data));
        }
    }
}