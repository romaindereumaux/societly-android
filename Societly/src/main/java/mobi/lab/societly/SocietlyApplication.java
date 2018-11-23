package mobi.lab.societly;

import android.app.ActivityManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.preference.PreferenceManager;

import com.facebook.FacebookSdk;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.lang.Thread.UncaughtExceptionHandler;

import ee.mobi.scrolls.Log;
import ee.mobi.scrolls.LogDeleteImplAge;
import ee.mobi.scrolls.LogImplCat;
import ee.mobi.scrolls.LogImplComposite;
import ee.mobi.scrolls.LogImplFile;
import ee.mobi.scrolls.LogPost;
import ee.mobi.scrolls.LogPostBuilder;
import ee.mobi.scrolls.LogPostImpl;

public class SocietlyApplication extends Application {

    public static final String LOG_PROJECT_NAME = "societly";

    @Override
    public void onCreate() {
        super.onCreate();
        // Add okhttp3 downloader for picasso
        Picasso.setSingletonInstance(new Picasso.Builder(getApplicationContext()).downloader(new OkHttp3Downloader(getApplicationContext())).build());

        FacebookSdk.sdkInitialize(this);
        FacebookSdk.setIsDebugEnabled(BuildConfig.DEBUG);

        if (Config.DEBUG_LOGS) {
            /* Init Logging to go both to logcat and file when we have a debug build*/
            LogImplFile.init(getFilesDir(), new LogDeleteImplAge(LogDeleteImplAge.AGE_KEEP_1_MONTH));
            LogImplComposite.init(new Class[]{LogImplCat.class, LogImplFile.class});
            Log.setImplementation(LogImplComposite.class);

            /* Configure posting to backend */
            LogPostImpl.configure(this, LOG_PROJECT_NAME);
        } else {
            /* On release builds (e.g. Google Play Store releases) lets just log errors */
            Log.setImplementation(LogImplCat.class);
            Log.setVerbosity(Log.VERBOSITY_LOG_ERRORS);
        }

        final Log log = Log.getInstance(this.getClass().getSimpleName());

        if (Config.DEBUG_LOGS) {
            for (ActivityManager.RunningAppProcessInfo inf : ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningAppProcesses()) {
                if (inf.pid == android.os.Process.myPid()) {
                    if (".LogPostProcess".equals(inf.processName)) {
                        android.util.Log.d("LcLogs", "Halting app onCreate as we are in logpost");
                        return;
                    }
                }
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("STRICTMODE_ENABLED", false)) {
                ThreadPolicy.Builder builder = new ThreadPolicy.Builder();
                builder.detectDiskWrites().detectNetwork().penaltyDialog();
                StrictMode.setThreadPolicy(builder.build());
            }

            final UncaughtExceptionHandler defaultExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                public void uncaughtException(Thread thread, Throwable ex) {
                    // Do the things we need to do
                    log.e("FATAL EXCEPTION", ex);

                    new LogPostBuilder()
                            .setConfirmEnabled(true)
                            .setShowResultEnabled(true)
                            .addTags(LogPost.LOG_TAG_CRASH)
                            .launchActivity(getApplicationContext());

                    // Call the system default handler
                    defaultExceptionHandler.uncaughtException(thread, ex);
                }
            });
        }
        log.d("societly app started");
    }
}