package mobi.lab.societly.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import mobi.lab.societly.R;
import mobi.lab.societly.adapter.CandidateAdapter;
import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.dto.Questionnaire;
import mobi.lab.societly.model.CandidateOverviewResult;
import mobi.lab.societly.util.Util;

public class CandidateFragment extends BaseFragment {

    public static final String EXTRA_CANDIDATE_COMPAT = "lab.mobi.societly.EXTRA_CANDIDATE_COMPAT";

    private CandidateCompatibility candidateCompat;
    private RecyclerView list;

    public static CandidateFragment newInstance(Bundle args) {
        CandidateFragment fragment = new CandidateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        candidateCompat = getArguments().getParcelable(EXTRA_CANDIDATE_COMPAT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_candidate_detail, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = (RecyclerView) view.findViewById(android.R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));

        View previousButton = view.findViewById(R.id.btn_toolbar_previous);
        previousButton.setVisibility(View.VISIBLE);
        previousButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        ((TextView) view.findViewById(R.id.text_toolbar_title)).setText(R.string.title_comparison);
        model.getCandidateOverview(candidateCompat);
    }


    @Subscribe
    public void onCandidateOverviewResultReceived(CandidateOverviewResult requestResult) {
        if (!requestResult.isSuccess()) {
            Util.showToast(getContext(), requestResult.getError().getMessage());
            return;
        }
        list.setVisibility(View.VISIBLE);
        CandidateCompatibility comp = requestResult.getCompatibility();
        Questionnaire userAnswers = requestResult.getUserAnswers();
        log.d("onCandidateResultReceived results=" + comp);
        list.setAdapter(new CandidateAdapter(userAnswers, comp));
    }


}
