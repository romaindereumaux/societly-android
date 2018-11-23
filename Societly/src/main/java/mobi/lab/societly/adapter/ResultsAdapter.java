package mobi.lab.societly.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.lab.societly.R;
import mobi.lab.societly.dto.Candidate;
import mobi.lab.societly.dto.CandidateCompatibility;
import mobi.lab.societly.ui.RoundedImageTransformation;
import mobi.lab.societly.util.ViewUtil;

public class ResultsAdapter extends BaseAdapter {

    private List<CandidateCompatibility> results;
    private Listener clickListener;

    public ResultsAdapter(List<CandidateCompatibility> newResults) {
        this(newResults, null);
    }

    public ResultsAdapter(List<CandidateCompatibility> newResults, Listener listener) {
        setResults(newResults);
        setOnItemClickListener(listener);
    }

    private void setResults(List<CandidateCompatibility> newResults) {
        results = newResults == null ? new ArrayList<>() : newResults;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(Listener listener) {
        clickListener = listener;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public CandidateCompatibility getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = onCreateViewHolder(parent);
        onBindViewHolder(holder, position);
        return holder.root;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result_list, parent, false);
        return new ViewHolder(root);
    }

    private void onBindViewHolder(ViewHolder holder, int position) {
        final Context context = holder.root.getContext();
        final CandidateCompatibility result = results.get(position);
        Candidate candidate = result.getCandidate();
        if (clickListener != null) {
            holder.root.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(result, position);
                }
            });
        }
        holder.name.setText(candidate.getName());
        // TODO add description here?
        holder.description.setText(candidate.getParty());
        holder.compatibility.setText(String.format(Locale.getDefault(), "%d%%", result.getCompatibility()));
        holder.progress.setProgress(result.getCompatibility());
        Picasso.with(context)
                .load(candidate.getImageUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.placeholder_rounded_image)
                .transform(new RoundedImageTransformation(context.getResources().getDimensionPixelSize(R.dimen.height_result_avatar) / 2))
                .into(holder.image);

        // Add a top margin to the first item
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int top = position == 0 ? ViewUtil.getDimensionInPx(context, R.dimen.margin_top_first_result_item) : 0;
        params.setMargins(0, top, 0, 0);
        holder.root.setLayoutParams(params);
    }

    public static class ViewHolder {

        View root;
        TextView name;
        TextView description;
        TextView compatibility;
        ProgressBar progress;
        ImageView image;

        public ViewHolder(View v) {
            root = v;
            name = (TextView) v.findViewById(R.id.text_result_main);
            description = (TextView) v.findViewById(R.id.text_result_secondary);
            compatibility = (TextView) v.findViewById(R.id.text_result_compatibility);
            progress = (ProgressBar) v.findViewById(R.id.progress_result_compatibility);
            image = (ImageView) v.findViewById(R.id.img_rounded);
        }
    }

    public interface Listener {
        void onItemClick(CandidateCompatibility item, int position);
    }
}
