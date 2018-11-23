package mobi.lab.societly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import mobi.lab.societly.BuildConfig;
import mobi.lab.societly.R;
import mobi.lab.societly.activity.CandidateOverviewActivity;
import mobi.lab.societly.activity.SplashActivity;
import mobi.lab.societly.adapter.DistrictAdapter;
import mobi.lab.societly.adapter.NoHintSpinnerAdapter;
import mobi.lab.societly.adapter.ResultsAdapter;
import mobi.lab.societly.adapter.StateAdapter;
import mobi.lab.societly.dto.District;
import mobi.lab.societly.dto.State;
import mobi.lab.societly.model.AppState;
import mobi.lab.societly.model.CandidateCompatibiltyResult;
import mobi.lab.societly.model.Model;
import mobi.lab.societly.model.SubmitResultResp;
import mobi.lab.societly.ui.AdapterLinearLayout;
import mobi.lab.societly.util.Util;
import mobi.lab.societly.util.ViewUtil;

public class ResultFragment extends BaseFragment implements ConfirmDialogFragment.ConfirmDialogFragmentListener {

    private static String TAG_CONFIRM_RESET = "lab.mobi.societly.dialog.CONFIRM_RESET";
    private static String TAG_CONFIRM_LOGOUT = "lab.mobi.societly.dialog.CONFIRM_LOGOUT";
    private static String TAG_CONFIRM_RESUBMIT = "lab.mobi.societly.dialog.TAG_CONFIRM_RESUBMIT";
    private static String STATE_STATE = "lab.mobi.societly.dialog.STATE_STATE";
    private static String STATE_DISTRICT = "lab.mobi.societly.dialog.STATE_DISTRICT";

    // TODO refactor these adapters if we have time, a bit hacky atm
    private NoHintSpinnerAdapter<District> districtSpinnerAdapter;
    private NoHintSpinnerAdapter<State> stateSpinnerAdapter;
    private AdapterLinearLayout countryList;
    private AdapterLinearLayout stateList;
    private AdapterLinearLayout districtList;
    private AppCompatSpinner stateSpinner;
    private AppCompatSpinner districtSpinner;

    private View districtDivider;
    private View stateDivider;
    private View contentContainer;
    private View progress;

    private State selectedState;
    private District selectedDistrict;

    private CandidateCompatibiltyResult compatibiltyResult;
    private boolean showResubmitDialog = false;

    private final ResultsAdapter.Listener listener = (item, position) -> {
        Intent intent = new Intent(getContext(), CandidateOverviewActivity.class);
        intent.putExtra(CandidateFragment.EXTRA_CANDIDATE_COMPAT, item);
        startActivity(intent);
    };

    public static ResultFragment newInstance() {
        return new ResultFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedState = savedInstanceState == null ? null : savedInstanceState.getParcelable(STATE_STATE);
        selectedDistrict = savedInstanceState == null ? null : savedInstanceState.getParcelable(STATE_DISTRICT);

        contentContainer = view.findViewById(android.R.id.content);
        progress = view.findViewById(android.R.id.progress);
        districtDivider = view.findViewById(R.id.divider_district);
        stateDivider = view.findViewById(R.id.divider_state);

        countryList = (AdapterLinearLayout) view.findViewById(R.id.list_country);
        stateList = (AdapterLinearLayout) view.findViewById(R.id.list_state);
        districtList = (AdapterLinearLayout) view.findViewById(R.id.list_district);

        // Only state list can be empty. Districts won't be shown if empty
        stateList.setEmptyText(R.string.text_empty_results_state);

        stateSpinner = (AppCompatSpinner) view.findViewById(R.id.spinner_state);
        districtSpinner = (AppCompatSpinner) view.findViewById(R.id.spinner_district);

        initSpinners();
        initToolbar();
        initFooter();

        model.getCandidateResults();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showResubmitDialog) {
            showResubmitDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (stateSpinnerAdapter != null) {
            selectedState = stateSpinnerAdapter.getItem(stateSpinner.getSelectedItemPosition());
        }
        if (districtSpinnerAdapter != null) {
            selectedDistrict = districtSpinnerAdapter.getItem(districtSpinner.getSelectedItemPosition());
        }
        log.d("onSaveInstanceState district=%s state=%s", selectedDistrict, selectedState);
        if (selectedState != null) {
            outState.putParcelable(STATE_STATE, selectedState);
        }
        if (selectedDistrict != null) {
            outState.putParcelable(STATE_DISTRICT, selectedDistrict);
        }
    }

    @Subscribe
    public void onCandidateResultsReceived(CandidateCompatibiltyResult requestResult) {
        if (!requestResult.isSuccess()) {
            Util.showToast(getContext(), requestResult.getError().getMessage());
            return;
        }
        compatibiltyResult = requestResult;

        log.d("onCadidateResultsReceived %s", requestResult);
        if (requestResult.getCountry() == null) {
            // Something is very wrong
            log.e("onCadidateResultsReceived country is null");
            return;
        }
        countryList.setAdapter(new ResultsAdapter(requestResult.getCountryResults(), listener));
        setupStates(requestResult.getCountry().getStates());

        progress.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onEvent(SubmitResultResp event) {
        // Don't emit the same event
        log.d("SubmitResultResp success=%s hasSession=%s", event.isSuccess(), model.hasSession());
        if (event.isSuccess() || !model.hasSession()) {
            // If everything is ok or we have not registered, then ignore this event
            model.setLastSubmitResult(null);
            return;
        }

        if (!isResumed()) {
            // Set up a flag to show the dialog in resumed state
            showResubmitDialog = true;
            return;
        }
        showResubmitDialog();
    }

    private void showResubmitDialog() {
        showResubmitDialog = false;
        model.setLastSubmitResult(null);
        Util.showDialogFragment(getActivity(), ConfirmDialogFragment.newInstance(R.string.text_resubmit_confirm), TAG_CONFIRM_RESUBMIT, this);
    }

    private void initSpinners() {
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onStateSelected(stateSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onDistrictSelected(districtSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        int dropDownWidth = ViewUtil.getScreenWidth(getContext()) - 2 * ViewUtil.getDimensionInPx(getContext(), R.dimen.padding_result_item_edge);
        stateSpinner.setDropDownWidth(dropDownWidth);
        districtSpinner.setDropDownWidth(dropDownWidth);
    }

    private void setupStates(List<State> states) {
        int size = Util.sizeOf(states);

        log.d("setupStates size=%s selectedState=%s", size, selectedState);
        setStatesVisibility(size > 0);
        if (size == 0) {
            stateSpinner.setAdapter(null);
            stateSpinnerAdapter = null;
            stateList.setAdapter(null);
            setupDistricts(null);
            return;
            // Nothing more to do here
        }

        stateSpinnerAdapter = StateAdapter.createNoHintAdapter(states);
        stateSpinner.setAdapter(stateSpinnerAdapter);
        stateSpinner.setEnabled(Util.sizeOf(stateSpinnerAdapter.getItems()) != 1);

        stateSpinner.post(() -> {
            // Select the previous item, the only item if we have or the hint
            stateSpinner.setSelection(getSpinnerPosition(states, selectedState));
        });
    }

    private void onStateSelected(State state) {
        log.d("onStateSelected %s", state);
        ResultsAdapter adapter = null;
        List<District> districts = null;
        if (state != null) {
            // Hint selected at first
            adapter = new ResultsAdapter(compatibiltyResult.getStateResults().get(state.getId()), listener);
            districts = state.getDistricts();
        }
        stateList.setAdapter(adapter);
        setupDistricts(districts);
    }

    private void setDistrictsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        districtSpinner.setVisibility(visibility);
        districtDivider.setVisibility(visibility);
        districtList.setVisibility(visibility);
    }

    private void setStatesVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        stateSpinner.setVisibility(visibility);
        stateDivider.setVisibility(visibility);
        stateList.setVisibility(visibility);
    }

    private void setupDistricts(List<District> districts) {
        int size = Util.sizeOf(districts);
        log.d("setupDistricts size=%s selectedDistrict=%s", size, selectedDistrict);
        setDistrictsVisibility(size > 0);
        if (size == 0) {
            districtSpinner.setAdapter(null);
            districtSpinnerAdapter = null;
            districtList.setAdapter(null);
            return;
            // Nothing more to do here
        }

        districtSpinnerAdapter = DistrictAdapter.createNoHintAdapter(districts);
        districtSpinner.setAdapter(districtSpinnerAdapter);
        districtSpinner.setEnabled(size > 1);

        districtSpinner.post(() -> {
            // Select the previous item, the only item if we have or the hint
            districtSpinner.setSelection(getSpinnerPosition(districts, selectedDistrict));
        });
    }

    /**
     * 0 if none selected
     * 1 if items.size == 1
     * pos of selectedItem in items (+1 for the empty hint item)
     * @param items
     * @param selectedItem
     * @return
     */
    private <T> int getSpinnerPosition(List<T> items, T selectedItem) {
        int size = Util.sizeOf(items);
        int selectedPos = size == 1 ? 1 : 0;
        if (size > 1 && selectedItem != null) {
            int index = items.indexOf(selectedItem);
            if (index != -1) {
                // +1 for the empty hint
                selectedPos = index + 1;
            }
        }
        return selectedPos;
    }

    private void onDistrictSelected(District district) {
        log.d("onDistrictSelected %s", district);
        ResultsAdapter adapter = district == null
                ? null
                : new ResultsAdapter(compatibiltyResult.getDistrictResults().get(district.getId()), listener);
        districtList.setAdapter(adapter);
    }

    private void initToolbar() {
        ((TextView) getView().findViewById(R.id.text_toolbar_title)).setText(R.string.title_results);
        View previousButton = getView().findViewById(R.id.btn_toolbar_previous);
        previousButton.setVisibility(View.VISIBLE);
        previousButton.setOnClickListener(v -> Util.onNavigateUp(getActivity()));

        if (model.hasSession()) {
            // Add logout button
            ImageButton nextButton = (ImageButton) getView().findViewById(R.id.btn_toolbar_next);
            nextButton.setImageResource(R.drawable.ic_logout);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setOnClickListener(v -> {
                Util.showDialogFragment(getActivity(), ConfirmDialogFragment.newInstance(R.string.text_logout_confirm), TAG_CONFIRM_LOGOUT, ResultFragment.this);
            });
        }
    }

    private void initFooter() {
        getView().findViewById(R.id.btn_reset).setOnClickListener(v -> {
            Util.showDialogFragment(getActivity(), ConfirmDialogFragment.newInstance(R.string.text_reset_confirm), TAG_CONFIRM_RESET, ResultFragment.this);
        });

        View resetHard = getView().findViewById(R.id.btn_reset_hard);
        if (BuildConfig.DEBUG) {
            resetHard.setVisibility(View.VISIBLE);
            resetHard.setOnClickListener(v -> {
                Model.getInstance(getContext()).resetHard();
                Util.restartAppWithIntent(getActivity(), SplashActivity.class);
            });
        } else {
            resetHard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfirm(String tag) {
        log.d("confirmDialog onConfirm tag=%s", tag);
        if (getActivity() == null) {
            return;
        }
        if (TextUtils.equals(TAG_CONFIRM_RESUBMIT, tag)) {
            model.submitResults(null);
        } else {
            if (TextUtils.equals(TAG_CONFIRM_RESET, tag)) {
                model.resetQuestionnaire();
            } else {
                model.logout();
            }
            Util.restartAppWithIntent(getActivity(), Util.getAppEntryIntent(getContext(), AppState.NEW));
        }
    }

    @Override
    public void onDeny(String tag) {
        log.d("confirmDialog onDeny tag=%s", tag);
    }
}
