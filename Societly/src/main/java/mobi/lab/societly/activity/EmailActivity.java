package mobi.lab.societly.activity;

import android.os.Bundle;

import mobi.lab.societly.R;
import mobi.lab.societly.fragment.EmailFragment;

public class EmailActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, EmailFragment.newInstance())
                    .commit();
        }
    }
}
