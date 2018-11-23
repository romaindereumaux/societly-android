package mobi.lab.societly.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import mobi.lab.societly.R;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .widgetColorRes(R.color.color_primary)
                .content(R.string.text_loading_dialog)
                .build();
    }
}
