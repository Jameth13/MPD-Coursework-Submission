//James Paton S1111175

package com.jamespaton.MPDCoursework;


import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//Code adapted from https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private Context context;
    private OnItemListener onItemListener;
    private List<RSS> mData;
    private LayoutInflater mInflater;

    RecyclerAdapter(Context context, List<RSS> data, OnItemListener onItemListener) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.onItemListener = onItemListener;
    }

    //Inflate the row layout
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerviewlayout, parent, false);
        return new ViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Another fluke.
        if (mData.size() <= 0) {
            Log.e("ERROR!", "No mData for recycler");
            return;
        }

        RSS rss = mData.get(position);

        if (rss.title != null)
            holder.textViewTitle.setText(Html.fromHtml(rss.title, Html.FROM_HTML_MODE_COMPACT));
        if (rss.description != null)
            holder.textViewDescription.setText(Html.fromHtml(rss.description, Html.FROM_HTML_MODE_COMPACT));

        int days = rss.lengthInDays;
        float gradient = days / 31.0f;
        if (days >= 0) {
            float r = 0.0f;
            float g = 0.0f;
            float b = 0.0f;

            if (gradient >= 0.0f && gradient <= 0.5f)
                r = gradient / 0.5f;
            else
                r = 1.0f;

            if (gradient > 0.5)
                g = 1.0f - Math.min(gradient, 1.0f);
            else
                g = 1.0f;

            //Workaround to avoid increasing the API to 26 just to use Color.rgb().
            int rInt = Math.max(0, Math.min(255, Math.round( r * 255)));
            int gInt = Math.max(0, Math.min(255, Math.round( g * 255)));
            int bInt = Math.max(0, Math.min(255, Math.round( b * 255)));
            String hex = String.format("#%02x%02x%02x", rInt, gInt, bInt);

            //Expand color indicator.
            holder.imageView.setColorFilter(Color.parseColor(hex));
            ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
            layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources().getDisplayMetrics());;
            holder.imageView.setLayoutParams(layoutParams);
            layoutParams = holder.space.getLayoutParams();
            layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, context.getResources().getDisplayMetrics());;
            holder.space.setLayoutParams(layoutParams);
        } else {
            //Collapse color indicator.
            holder.imageView.setColorFilter(Color.parseColor("#FFFFFF"));
            ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
            layoutParams.width = 0;
            holder.imageView.setLayoutParams(layoutParams);
            layoutParams = holder.space.getLayoutParams();
            layoutParams.width = 0;
            holder.space.setLayoutParams(layoutParams);
        }

        //Set time ago
        if (rss.timeAgo > 0) {
            //Expand time ago.
            ViewGroup.LayoutParams layoutParams = holder.textViewTime.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.textViewTime.setLayoutParams(layoutParams);

            holder.textViewTime.setText(rss.timeAgoString);
        } else {
            //Collapse time ago.
            ViewGroup.LayoutParams layoutParams = holder.textViewTime.getLayoutParams();
            layoutParams.height = 0;
            holder.textViewTime.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        OnItemListener onItemListener;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewTime;
        ImageView imageView;
        Space space;

        ViewHolder(View itemView, OnItemListener onItemListener) {
            super(itemView);
            this.onItemListener = onItemListener;
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageView = itemView.findViewById(R.id.imageView);
            space = itemView.findViewById(R.id.space);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }
}
