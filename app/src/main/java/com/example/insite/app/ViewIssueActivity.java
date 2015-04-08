package com.example.insite.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.insite.app.app.AppController;
import com.example.insite.app.model.Issue;

/**
 * Created by Xing Yi on 7/4/2015.
 */
public class ViewIssueActivity extends ActionBarActivity {

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private Issue issue;
    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvUrgency;
    private TextView tvReporter;
    private TextView tvEmail;
    private TextView tvContact;
    private TextView tvStatus;
    private TextView tvStatusComment;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_issue);


        Bundle bundle = this.getIntent().getExtras();
        issue = bundle.getParcelable("issue_obj");

        tvTitle = (TextView)findViewById(R.id.tv_title);
        tvDate = (TextView)findViewById(R.id.tv_date);
        tvTime = (TextView)findViewById(R.id.tv_time);
        tvLocation = (TextView)findViewById(R.id.tv_location);
        tvDescription = (TextView)findViewById(R.id.tv_description);
        tvUrgency = (TextView)findViewById(R.id.tv_urgency);
        tvReporter = (TextView)findViewById(R.id.tv_reporter);
        tvEmail = (TextView)findViewById(R.id.tv_email);
        tvContact = (TextView)findViewById(R.id.tv_contact);
        tvStatus = (TextView)findViewById(R.id.tv_status);
        tvStatusComment = (TextView)findViewById(R.id.tv_status_comment);

        tvTitle.setText(issue.getTitle());
        tvDate.setText(issue.getDate());
        tvTime.setText(issue.getTime());
        tvLocation.setText(issue.getLocation());
        tvDescription.setText(issue.getDescription());
        tvUrgency.setText(issue.getUrgency_level());
        tvReporter.setText(issue.getReporter());
        tvEmail.setText(issue.getEmail());
        tvContact.setText(issue.getContact());
        tvStatus.setText(issue.getStatus());
        tvStatusComment.setText(issue.getStatus_comment());

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView imageIssue = (NetworkImageView) this
                .findViewById(R.id.img_issue);

        // load image
        if(issue.getImage_url().length() > 0 && !issue.getImage_url().equals("null") ) {
            imageIssue.setImageUrl(issue.getImage_url(), imageLoader);
            // set an error thumbnail image in case if the image URL is invalid or inaccessible
            imageIssue.setErrorImageResId(R.drawable.sit_logo_black);
        }
        else{
            imageIssue.setDefaultImageResId(R.drawable.sit_logo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                Intent settingIntent = new Intent();
                settingIntent.setClass(this, SetPreferenceActivity.class);
                startActivity(settingIntent);
                return true;

            case R.id.action_add:
                Intent newIssueIntent = new Intent(this, ReportIssueActivity.class);
                this.startActivity(newIssueIntent);
                //overridePendingTransition(R.anim.right_enter, R.anim.left_exit);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
