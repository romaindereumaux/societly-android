package mobi.lab.societly.activity;

import android.os.Bundle;

import mobi.lab.societly.R;
import mobi.lab.societly.fragment.CandidateFragment;

public class CandidateOverviewActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, CandidateFragment.newInstance(getIntent().getExtras()))
                    .commit();
        }
    }
}
