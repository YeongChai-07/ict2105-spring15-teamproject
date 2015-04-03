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

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView location = (TextView) convertView.findViewById(R.id.location);
        TextView urgency = (TextView) convertView.findViewById(R.id.urgency);
        TextView date = (TextView) convertView.findViewById(R.id.date);

        // getting issue data for the row
        Issue m = issueItems.get(position);

        // thumbnail image
        if(m.getThumbnailUrl().length() > 0 && !m.getThumbnailUrl().equals("null") ) {
            thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);
        }
        else{
            thumbNail.setDefaultImageResId(R.drawable.sit_logo);
            //thumbNail.setErrorImageResId(R.drawable.sit_logo);
        }

        // title
        title.setText(m.getTitle());

        // location
        location.setText("Location: " + m.getLocation());

        // urgency level
        urgency.setText("Urgency: " + m.getUrgency_level());

        // date reported
        date.setText(m.getDate());

        return convertView;
    }

}
