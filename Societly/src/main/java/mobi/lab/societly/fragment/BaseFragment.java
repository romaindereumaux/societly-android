package mobi.lab.societly.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import ee.mobi.scrolls.Log;
import mobi.lab.societly.model.Model;

public class BaseFragment extends Fragment {

    protected Log log = Log.getInstance(this);
    protected Model model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.d("onCreate" + " self=" + this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        log.d("onAttach, activity=" + getActivity().getClass().getName() + " self=" + this);
        model = Model.getInstance(this);
        model.subscribe(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log.d("onViewCreated self=" + this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log.d("onDetach" + " self=" + this);
        model.unsubscribe(this);
        model = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("onDestroy" + " self=" + this);
    }

    @Override
    public void onPause() {
        super.onPause();
        log.d("onPause" + " self=" + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        log.d("onResume" + " self=" + this);
    }

    @Override
    public void onStart() {
        super.onStart();
        log.d("onStart" + " self=" + this);
    }

    @Override
    public void onStop() {
        super.onStop();
        log.d("onStop" + " self=" + this);
    }
}
