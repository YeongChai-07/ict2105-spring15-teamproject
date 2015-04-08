package com.example.insite.app.adapter;

/**
 * Created by Lim Xing Yi on 4/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.insite.app.R;
import com.example.insite.app.app.AppController;
import com.example.insite.app.model.Issue;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Issue> issueItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        TextView title;
        TextView location;
        TextView urgency;
        TextView date;
        TextView time;
    }

    public CustomListAdapter(Activity activity, List<Issue> issueItems) {
        this.activity = activity;
        this.issueItems = issueItems;
    }

    @Override
    public int getCount() {
        return issueItems.size();
    }

    @Override
    public Object getItem(int location) {
        return issueItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // reuse views
        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_row, null);

            //set up the ViewHolder
            viewHolder = new ViewHolderItem();

            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.location = (TextView) convertView.findViewById(R.id.location);
            viewHolder.urgency = (TextView) convertView.findViewById(R.id.urgency);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);

            //store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else{
            // View recycled, no need to inflate
            // avoid calling findViewById() on resource every time
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }


        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);



        // getting issue data for the row
        Issue m = issueItems.get(position);

        // thumbnail image
        if(m.getImage_url().length() > 0 && !m.getImage_url().equals("null") ) {
            thumbNail.setImageUrl(m.getImage_url(), imageLoader);
            // set an error thumbnail image in case if the image URL is invalid or inaccessible
            thumbNail.setErrorImageResId(R.drawable.sit_logo_black);
        }
        else{
            thumbNail.setDefaultImageResId(R.drawable.sit_logo);
        }

        // title
        viewHolder.title.setText(m.getTitle());

        // location
        viewHolder.location.setText("@ " + m.getLocation());

        // urgency level
        viewHolder.urgency.setText("Urgency: " + m.getUrgency_level());

        switch(m.getUrgency_level()) {
            case "Very high":
                viewHolder.urgency.setTextColor( activity.getResources().getColor(R.color.urgency_very_high) );
                break;
            case "High":
                viewHolder.urgency.setTextColor( activity.getResources().getColor(R.color.urgency_high) );
                break;
            case "Normal":
                viewHolder.urgency.setTextColor( activity.getResources().getColor(R.color.urgency_normal) );
                break;
            case "Low":
                viewHolder.urgency.setTextColor( activity.getResources().getColor(R.color.urgency_low) );
                break;
            case "Very low":
                viewHolder.urgency.setTextColor( activity.getResources().getColor(R.color.urgency_very_low) );
                break;
        }

        // date reported
        viewHolder.date.setText(m.getDate());

        // time reported
        viewHolder.time.setText(m.getTime());

        return convertView;
    }

}
