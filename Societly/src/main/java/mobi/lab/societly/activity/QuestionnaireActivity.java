package mobi.lab.societly.activity;

import android.os.Bundle;

import mobi.lab.societly.R;
import mobi.lab.societly.fragment.QuestionnaireFragment;

public class QuestionnaireActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, QuestionnaireFragment.newInstance())
                    .commit();
        }
    }
}
