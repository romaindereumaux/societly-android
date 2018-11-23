package mobi.lab.societly.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import mobi.lab.societly.R;

public class ConfirmDialogFragment extends DialogFragment {

    private static final String ARG_MSG = ConfirmDialogFragment.class.getName() + ".ARG_MSG";

    public static ConfirmDialogFragment newInstance(int messageResId) {
        Bundle args = new Bundle();
        args.putInt(ARG_MSG, messageResId);
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.content(getMessageId())
                .positiveText(R.string.label_confirm_yes)
                .negativeText(R.string.label_confirm_no)
                .typeface(getString(R.string.font_regular), getString(R.string.font_light))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (getListener() != null) {
                            getListener().onConfirm(getTag());
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        if (getListener() != null) {
                            getListener().onDeny(getTag());
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                    }
                });
        return builder.build();
    }

    private int getMessageId() {
        return getArguments().getInt(ARG_MSG);
    }

    protected ConfirmDialogFragmentListener getListener() {
        if (getTargetFragment() instanceof ConfirmDialogFragmentListener) {
            return (ConfirmDialogFragmentListener) getTargetFragment();
        } else if (getActivity() instanceof ConfirmDialogFragmentListener) {
            return (ConfirmDialogFragmentListener) getActivity();
        }
        return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        final ConfirmDialogFragmentListener listener = getListener();
        if (listener != null) {
            listener.onDeny(getTag());
        }
    }

    public interface ConfirmDialogFragmentListener {
        void onConfirm(String tag);
        void onDeny(String tag);
    }
}
