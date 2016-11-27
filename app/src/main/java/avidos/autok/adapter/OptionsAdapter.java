package avidos.autok.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import avidos.autok.R;
import avidos.autok.entity.CarsUnassigned;

/**
 * Created by Alan on 10/17/2016.
 */

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
    private List<String> mListOptions;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mOptionTextView;
        public ViewHolder(View v) {
            super(v);
            mOptionTextView = (TextView) v.findViewById(R.id.text_view_option_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OptionsAdapter(Context context) {
        mListOptions = new ArrayList<>();
        mListOptions.add(context.getResources().getStringArray(R.array.options_array)[0]);
        mListOptions.add(context.getResources().getStringArray(R.array.options_array)[1]);
        mListOptions.add(context.getResources().getStringArray(R.array.options_array)[2]);
        mListOptions.add(context.getResources().getStringArray(R.array.options_array)[3]);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_options, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mOptionTextView.setText(mListOptions.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mListOptions.size();
    }
}