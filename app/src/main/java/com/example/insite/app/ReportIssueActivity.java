package com.example.insite.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.insite.app.app.AppController;
import com.example.insite.app.helper.CameraHelper;
import com.example.insite.app.helper.Common;
import com.example.insite.app.helper.MultipartRequest;
import com.example.insite.app.model.AppSetting;
import com.example.insite.app.model.Issue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//import com.example.insite.app.util.imagePOST_RequestHelper;

/**
 * Created by Tan Yeong Chai on 25/3/2015.
 * Refactored by Lim Xing Yi
 */
public class ReportIssueActivity extends ActionBarActivity {

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CAMERA_TAG = "Camera Test";
    private static final String UPLOAD_TAG = "Image Upload";

    private Context context;

    private EditText editTitle;
    private EditText editLocation;
    private Button btnCamera;
    private Spinner spinnerUrgency;
    private ImageView imgIssue;
    private EditText editDescription;
    private EditText editReporter;
    private EditText editEmail;
    private EditText editContact;
    private Button btnSubmit;


    final static String STATE_ISSUENAME = "title";
    final static String STATE_LOCATION = "location";
    final static String STATE_IMAGEPATH = "imagePath";
    final static String STATE_DESC = "description";
    final static String STATE_URGENCY = "urgency";
    final static String STATE_REPORTER = "reporter";
    final static String STATE_EMAIL = "email";
    final static String STATE_CONTACT = "contact";

    final String SUBMIT_ISSUE_URL = AppSetting.baseUrl;
    final String UPLOAD_IMAGE_URL = AppSetting.imagePostUrl;

    // Authorisation token
    private static final String token = AppSetting.APItoken;

    //Camera Attributes
    String currentImagePath = "";
    String mFileName = "";
    static File imageFile;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001;
    private Uri currentImageUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    Bitmap bMapScaled;

    String mtoastMsg = "";
    boolean mIsSubmitted = false;
    private ProgressDialog progress;

    Issue newIssue = new Issue();

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Always call the superclass first
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);

        Common.setContext(getApplicationContext());

        // to setup touch outside listener for soft keyboard dismissal
        setupUI(findViewById(R.id.scrollview_report_issue));


        final String TITLE_ERROR_MSG = getResources().getString(R.string.error_title);
        final String LOCATION_ERROR_MSG = getResources().getString(R.string.error_location);
        final String ISSUE_DESC_ERROR_MSG = getResources().getString(R.string.error_description);
        final String REPORTER_ERROR_MSG = getResources().getString(R.string.error_reporter);
        final String EMAIL_ERROR_MSG = getResources().getString(R.string.error_email);

        editTitle = (EditText) findViewById(R.id.edit_title);
        editLocation = (EditText) findViewById(R.id.edit_location);
        btnCamera = (Button) findViewById(R.id.button_captureImage);
        spinnerUrgency = (Spinner) findViewById (R.id.spinner_urgencyPicker);
        imgIssue = (ImageView)findViewById(R.id.issue_Image);
        editDescription = (EditText) findViewById(R.id.multiLine_Description);
        editReporter = (EditText) findViewById(R.id.edit_reporter);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editContact = (EditText) findViewById(R.id.edit_contact);
        btnSubmit = (Button) findViewById(R.id.button_submitIssue);

        editTitle.clearFocus();
        progress = new ProgressDialog(this);

        newIssue.setImage_url("");
        // get default image
        bMapScaled = Common.setDefaultImage();

        // Check whether we're recreating a previously destroyed instance
        if(savedInstanceState != null)
        {
            // Restore value from saved state
            editTitle.setText(savedInstanceState.getString(STATE_ISSUENAME));
            editLocation.setText(savedInstanceState.getString(STATE_LOCATION));
            currentImagePath = savedInstanceState.getString(STATE_IMAGEPATH);
            editDescription.setText(savedInstanceState.getString(STATE_DESC));
            spinnerUrgency.setSelection(savedInstanceState.getInt(STATE_URGENCY));
            editReporter.setText(savedInstanceState.getString(STATE_REPORTER));
            editEmail.setText(savedInstanceState.getString(STATE_EMAIL));
            editContact.setText(savedInstanceState.getString(STATE_CONTACT));

            // if no image is captured
                if(!currentImagePath.isEmpty() )
                {
                    bMapScaled = Common.handleImageFrom_PictureFile(currentImagePath);
                }

        } else {
            // Probably initialize members with default values for a new instance

        }


        // set image in the image view
        imgIssue.setImageBitmap(bMapScaled);

        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
//                startActivity(cameraIntent);
                if (currentImageUri == null) {
                    mFileName = CameraHelper.generateFileName();
                    newIssue.setImage_url(mFileName);
                    currentImageUri = CameraHelper.getImageFileUri(imageFile, mFileName);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri); // set the image file name
                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (Common.isInputEmpty(editTitle.getText().toString())) {
                    editTitle.requestFocus();
                    editTitle.setError(TITLE_ERROR_MSG);    //Shows a error message label
                    return;

                }

                if (Common.isInputEmpty(editLocation.getText().toString())) {
                    editLocation.requestFocus();
                    editLocation.setError(LOCATION_ERROR_MSG);    //Shows a error message label
                    return;
                }

                if (Common.isInputEmpty(editDescription.getText().toString())) {
                    editDescription.requestFocus();
                    editDescription.setError(ISSUE_DESC_ERROR_MSG);    //Shows a error message label

                    return;

                }


                String issueName = editTitle.getText().toString().trim();
                String issueLocation = editLocation.getText().toString().trim();
                String issueDesc = editDescription.getText().toString().trim();
                String urgencyType = spinnerUrgency.getSelectedItem().toString();
                String reporter = editReporter.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String contact = editContact.getText().toString().trim();


                newIssue.setTitle(issueName);
                newIssue.setDate(Common.getTodayDate());
                newIssue.setTime(Common.getCurrentTime());
                newIssue.setLocation(issueLocation);
                newIssue.setDescription(issueDesc);
                newIssue.setUrgency_level(urgencyType);
                newIssue.setReporter(reporter);
                newIssue.setEmail(email);
                newIssue.setContact(contact);


                submitIssue(newIssue);


            }

        });
    }


    //TODO: For use to return the result back from the camera Intent Service
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(CAMERA_TAG, "Entered onActivityResult method");

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(CAMERA_TAG, "Trying to save image!");
                CameraHelper.galleryAddPic(currentImageUri, this);

                currentImagePath= currentImageUri.getPath();

                bMapScaled = Common.handleImageFrom_PictureFile(currentImagePath);

                // Set image to viewer
                imgIssue.setImageBitmap(bMapScaled);

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }



    private void submitIssue(final Issue issue)
    {

        StringRequest submitIssue_Req = new StringRequest(Request.Method.POST, SUBMIT_ISSUE_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i(TAG, "This HTTP response is : " + response);

                mtoastMsg = "Thank you for reporting the issue!";
                Toast.makeText(ReportIssueActivity.this, mtoastMsg, Toast.LENGTH_LONG).show();
                finish();
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    //Toast.makeText(context,
                    //        context.getString(R.string.error_network_timeout),
                    //        Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    //
                } else if (error instanceof ServerError) {
                    //
                } else if (error instanceof NetworkError) {
                    //
                } else if (error instanceof ParseError) {
                    //
                }

                mtoastMsg = "Sorry, the form failed to submit.";
                Toast.makeText(ReportIssueActivity.this, mtoastMsg, Toast.LENGTH_LONG).show();
            }
        }
        ){
            @Override
            public Map<String,String> getParams()
            {
                Map<String, String> issueMap  = new HashMap<String, String>();
                issueMap.put("issue_name", issue.getTitle() );
                issueMap.put("description", issue.getDescription() );
                issueMap.put("location_name", issue.getLocation() );

                if( !issue.getImage_url().toString().isEmpty() )
                    issueMap.put("image_path", issue.getImage_url());

                issueMap.put("date_reported", issue.getDate());
                issueMap.put("time_reported", issue.getTime());
                issueMap.put("urgency_level", issue.getUrgency_level());
                issueMap.put("reporter", issue.getReporter());
                issueMap.put("email", issue.getEmail());
                issueMap.put("contact", issue.getContact());

                return issueMap;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(submitIssue_Req);

        if( !currentImagePath.isEmpty() )
        {


            progress.setMessage("Uploading Image :) ");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(true);
            progress.show();

            File sourceFile = new File(currentImagePath);
            Map<String, String> stringMap = new HashMap<String, String>();

            uploadFile("uploadTag", UPLOAD_IMAGE_URL, sourceFile, "image_file", stringMap, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Log.d(UPLOAD_TAG, "Success");
                    progress.dismiss();
                    mtoastMsg = "Image has been uploaded successfully.";
                    Toast.makeText(ReportIssueActivity.this, mtoastMsg, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //Log.d(UPLOAD_TAG, "Failed");
                    VolleyLog.d(UPLOAD_TAG, "Error: " + volleyError.getMessage());

                    mtoastMsg = "Image has failed to upload.";
                    Toast.makeText(ReportIssueActivity.this, mtoastMsg, Toast.LENGTH_LONG).show();
                }
            }, new MultipartRequest.MultipartProgressListener() {
                @Override
                public void transferred(long transfered, int progressTime) {
                    Log.d(UPLOAD_TAG, "In Progress - " + String.valueOf(progressTime) );
                    progress.setProgress(progressTime);
                }
            });
        }

    }

    protected <T> void uploadFile(final String tag, final String url,
                                  final File file, final String partName,
                                  final Map<String, String> headerParams,
                                  final Response.Listener<String> resultDelivery,
                                  final Response.ErrorListener errorListener,
                                  MultipartRequest.MultipartProgressListener progListener) {
        //AZNetworkRetryPolicy retryPolicy = new AZNetworkRetryPolicy();

        MultipartRequest mr = new MultipartRequest(url, errorListener,
                resultDelivery, file, file.length(), null, headerParams,
                partName, progListener);

        //mr.setRetryPolicy(retryPolicy);
        mr.setTag(tag);

        //Volley.newRequestQueue(this).add(mr);
        AppController.getInstance().addToRequestQueue(mr);
    }


    /**
     * Hides virtual keyboard
     *
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(ReportIssueActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putString(STATE_ISSUENAME, editTitle.getText().toString());
        savedInstanceState.putString(STATE_LOCATION, editLocation.getText().toString());
        savedInstanceState.putString(STATE_IMAGEPATH, currentImagePath.toString() );
        savedInstanceState.putString(STATE_DESC, editDescription.getText().toString());
        savedInstanceState.putInt(STATE_URGENCY, spinnerUrgency.getSelectedItemPosition());
        savedInstanceState.putString(STATE_REPORTER, editReporter.getText().toString());
        savedInstanceState.putString(STATE_EMAIL, editEmail.getText().toString());
        savedInstanceState.putString(STATE_CONTACT, editContact.getText().toString());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_issue, menu);
        return true;
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
