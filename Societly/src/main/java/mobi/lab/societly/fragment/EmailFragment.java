package mobi.lab.societly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import mobi.lab.societly.R;
import mobi.lab.societly.activity.ResultsListActivity;
import mobi.lab.societly.util.Util;
import mobi.lab.societly.util.ViewUtil;

public class EmailFragment extends BaseFragment {

    private EditText emailText;

    public static EmailFragment newInstance() {
        return new EmailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailText = (EditText) view.findViewById(R.id.edittext_email);
        View previousButton = view.findViewById(R.id.btn_toolbar_previous);
        previousButton.setVisibility(View.VISIBLE);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.onNavigateUp(getActivity());
            }
        });

        initSubmitButton(view);
        initSkipText(view);
    }

    private void submitResults(String email) {
        // If submit is successful then show actual results
        model.submitResults(email);
        Intent intent = new Intent(getContext(), ResultsListActivity.class);
        startActivity(intent);
        // We don't want to have the email screen in history..
        getActivity().finish();
    }

    private void initSubmitButton(View root) {
        root.findViewById(R.id.btn_submit_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = emailText.getText().toString();
                if (Util.isValidEmail(data)) {
                    submitResults(data);
                } else {
                    emailText.setError("Invalid email");
                }
            }
        });
    }

    private void initSkipText(View root) {
        TextView skipText = (TextView) root.findViewById(R.id.text_email_skip);
        String text = getString(R.string.text_skip_email);

        int start = text.indexOf("skip");
        int end = start + "skip".length();
        ForegroundColorSpan span = new ForegroundColorSpan(ViewUtil.getColor(getResources(), R.color.text_light_primary));
        SpannableString ss = new SpannableString(text);
        ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        skipText.setText(ss);
        skipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResults(null);
            }
        });
    }
}
