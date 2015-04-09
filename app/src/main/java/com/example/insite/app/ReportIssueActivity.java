package com.example.insite.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.insite.app.helper.DebouncedOnClickListener;
import com.example.insite.app.helper.MultipartRequest;
import com.example.insite.app.model.AppSetting;
import com.example.insite.app.model.Issue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tan Yeong Chai on 25/3/2015.
 * Refactored by Lim Xing Yi
 */
public class ReportIssueActivity extends ActionBarActivity {

    // Log tag
    private static final String TAG = ReportIssueActivity.class.getSimpleName();
    private static final String CAMERA_TAG = "Camera Test";
    private static final String UPLOAD_TAG = "Image Upload";

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
    final static String STATE_IMAGEFILE_URI = "imageURI";
    final static String STATE_FILENAME = "fileName";

    final String SUBMIT_ISSUE_URL = AppSetting.baseUrl;
    final String UPLOAD_IMAGE_URL = AppSetting.imagePostUrl;

    // Authorisation token
    private static final String token = AppSetting.APItoken;

    //Camera Attributes
    String mCurrentImagePath = "";
    String mFileName = "";
    static File fImageFile;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001;
    private Uri currentImageUri;
    Bitmap bMapScaled;

    String mToastMsg = "";
    private ProgressDialog pDialog;

    Issue newIssue = new Issue();

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Always call the superclass first
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_issue);

        Common.setContext(getApplicationContext());

        // to setup touch outside listener for soft keyboard dismissal
        Common.setupUI(findViewById(R.id.scrollview_report_issue), this);


        final String TITLE_ERROR_MSG = getResources().getString(R.string.error_title);
        final String LOCATION_ERROR_MSG = getResources().getString(R.string.error_location);
        final String ISSUE_DESC_ERROR_MSG = getResources().getString(R.string.error_description);
        final String REPORTER_ERROR_MSG = getResources().getString(R.string.error_reporter);
        final String EMAIL_ERROR_MSG = getResources().getString(R.string.error_email);
        final String EMAIL_ERROR_INVALID_MSG = getResources().getString(R.string.error_invald_email);

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Listen for any changes in the Settings Menu
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        Log.d(TAG, "SharedPreference Listener Invoked");
                        // listener implementation
                        if (key.equals("name_preference")) {
                            // Set summary to be the user-description for the selected value
                            editReporter.setText(prefs.getString("name_preference", ""));
                        }

                        if (key.equals("email_preference")) {
                            // Set summary to be the user-description for the selected value
                            editEmail.setText(prefs.getString("email_preference", ""));
                        }

                        if (key.equals("contact_preference")) {
                            // Set summary to be the user-description for the selected value
                            editContact.setText(prefs.getString("contact_preference", ""));
                        }
                    }
                };

        // Keep track of whenever there's changes in the setting menu
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        pDialog = new ProgressDialog(this);

        // if the activity is launch from the "Add" button,
        // then it will initialise an empty URL for image
        // otherwise it will set the file name after returning from the Camera App
        newIssue.setImage_url(mFileName);
        // get default image
        bMapScaled = Common.setDefaultImage();

        // Check whether we're recreating a previously destroyed instance
        if(savedInstanceState != null)
        {
            // Restore value from saved state
            editTitle.setText(savedInstanceState.getString(STATE_ISSUENAME));
            editLocation.setText(savedInstanceState.getString(STATE_LOCATION));
            mCurrentImagePath = savedInstanceState.getString(STATE_IMAGEPATH);
            editDescription.setText(savedInstanceState.getString(STATE_DESC));
            spinnerUrgency.setSelection(savedInstanceState.getInt(STATE_URGENCY));

            currentImageUri = savedInstanceState.getParcelable(STATE_IMAGEFILE_URI);
            mFileName = savedInstanceState.getString(STATE_FILENAME);
            // Have to comment out the code, otherwise the latest SharedPref setting won't take in effect
            //editReporter.setText(savedInstanceState.getString(STATE_REPORTER));
            //editEmail.setText(savedInstanceState.getString(STATE_EMAIL));
            //editContact.setText(savedInstanceState.getString(STATE_CONTACT));

            // if no image is captured
            if(!mCurrentImagePath.isEmpty() )
            {
                bMapScaled = Common.handleImageFrom_PictureFile(mCurrentImagePath);
            }

        } else {


            String namePref = sharedPref.getString("name_preference", "");
            String emailPref = sharedPref.getString("email_preference", "");
            String contactPref = sharedPref.getString("contact_preference", "");

            // Initialize members with default values from the SharedPreference
            editReporter.setText(namePref);
            editEmail.setText(emailPref);
            editContact.setText(contactPref);
        }


        // set image in the image view
        imgIssue.setImageBitmap(bMapScaled);

        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currentImageUri == null) {
                    mFileName = CameraHelper.generateFileName();
                    newIssue.setImage_url(mFileName);
                    currentImageUri = CameraHelper.getImageFileUri(fImageFile, mFileName);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // set the image file name
                intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        });

        // Use abstract class helper to prevent multiple click on the button within 1 sec limit.
        btnSubmit.setOnClickListener(new DebouncedOnClickListener(1000) {

            @Override
            public void onDebouncedClick(View v) {

                //*
                /* Form Validation - Start
                */
                editTitle.setError(null);
                editLocation.setError(null);
                editDescription.setError(null);
                editReporter.setError(null);
                editEmail.setError(null);

                if (Common.isInputEmpty(editTitle.getText().toString())) {
                    editTitle.requestFocus();
                    editTitle.setError(TITLE_ERROR_MSG);
                    return;
                }
                else if (Common.isInputEmpty(editLocation.getText().toString())) {
                    editLocation.requestFocus();
                    editLocation.setError(LOCATION_ERROR_MSG);
                    return;
                }
                else if (Common.isInputEmpty(editDescription.getText().toString())) {
                    editDescription.requestFocus();
                    editDescription.setError(ISSUE_DESC_ERROR_MSG);
                    return;
                }
                else if (Common.isInputEmpty(editReporter.getText().toString())) {
                    editReporter.requestFocus();
                    editReporter.setError(REPORTER_ERROR_MSG);
                    return;
                }
                else if (Common.isInputEmpty(editEmail.getText().toString())) {
                    editEmail.requestFocus();
                    editEmail.setError(EMAIL_ERROR_MSG);
                    return;
                }
                // if email is in invalid format
                else if (!Common.isValidEmail(editEmail.getText().toString())) {
                    editEmail.requestFocus();
                    editEmail.setError(EMAIL_ERROR_INVALID_MSG);
                    return;
                }
                //*
                /* Form Validation - End
                */

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

                // Submit the form and return to ListView if successful,
                // otherwise remain at the form
                submitIssue(newIssue);
            }

        });
    }


    // Return the result back from the camera Intent Service
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(CAMERA_TAG, "Entered onActivityResult method");

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(CAMERA_TAG, "Saving image...");
                CameraHelper.galleryAddPic(currentImageUri, this);

                mCurrentImagePath = currentImageUri.getPath();

                bMapScaled = Common.handleImageFrom_PictureFile(mCurrentImagePath);

                // Set image to viewer
                imgIssue.setImageBitmap(bMapScaled);

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Log.d(CAMERA_TAG, "Camera being cancelled");
                mFileName = "";

            } else {
                // Image capture failed, advise user
                mFileName = "";
            }
        }

        Log.d(TAG, "File Name: " + mFileName);
        //update the issue object if there should be an image path
        newIssue.setImage_url(mFileName);
    }

    /*
    * Submit request using Google Volley Library
    */
    private void submitIssue(final Issue issue)
    {

        StringRequest submitIssue_Req = new StringRequest(Request.Method.POST, SUBMIT_ISSUE_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i(TAG, "This HTTP response is : " + response);

                mToastMsg = "Thank you for reporting the issue!";
                Toast.makeText(ReportIssueActivity.this, mToastMsg, Toast.LENGTH_LONG).show();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "success");
                setResult(RESULT_OK, returnIntent);

                // Return to the listview after form submission
                finish();
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

                mToastMsg = "Sorry, the form failed to submit.";

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    mToastMsg = "There seems to be a connection error. Please try again later.";
                } else if (error instanceof AuthFailureError) {
                    // retain same error msg
                } else if (error instanceof ServerError) {
                    mToastMsg = "An error has occurred in the server.";
                } else if (error instanceof NetworkError) {
                    mToastMsg = "There seems to be a problem with the network. Please try again later.";
                } else if (error instanceof ParseError) {
                    // retain same error msg
                }

                Toast.makeText(ReportIssueActivity.this, mToastMsg, Toast.LENGTH_LONG).show();
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

                // if there is photo being captured
                if( !issue.getImage_url().isEmpty() )
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

        // Add the form post request to queue
        AppController.getInstance().addToRequestQueue(submitIssue_Req);

        // if there is photo being captured
        if( !mCurrentImagePath.isEmpty() )
        {
            // Show up progress dialog
            pDialog.setMessage("Uploading Image...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            // Set to false as we can measure "loading amount"
            pDialog.setIndeterminate(false);
            pDialog.show();

            File sourceFile = new File(mCurrentImagePath);

            // An empty hash map as there is no form parameters to post
            Map<String, String> stringMap = new HashMap<String, String>();

            // Starts to upload the image
            uploadFile("uploadTag", UPLOAD_IMAGE_URL, sourceFile, "image_file", stringMap, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Log.d(UPLOAD_TAG, "Success");
                    pDialog.dismiss();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    VolleyLog.d(UPLOAD_TAG, "Error: " + volleyError.getMessage());
                    pDialog.dismiss();
                    mToastMsg = "Image has failed to upload.";
                    Toast.makeText(ReportIssueActivity.this, mToastMsg, Toast.LENGTH_LONG).show();

                }
            }, new MultipartRequest.MultipartProgressListener() {
                @Override
                public void transferred(long transfered, int progressTime) {
                    Log.d(UPLOAD_TAG, "In Progress - " + String.valueOf(progressTime) );
                    pDialog.setProgress(progressTime);
                }
            });
        }

    }

    /**
     * Helper method for sending Multipart Request to upload file
     */
    protected <T> void uploadFile(final String tag, final String url,
                                  final File file, final String partName,
                                  final Map<String, String> headerParams,
                                  final Response.Listener<String> resultDelivery,
                                  final Response.ErrorListener errorListener,
                                  MultipartRequest.MultipartProgressListener progListener) {

        MultipartRequest mr = new MultipartRequest(url, errorListener,
                resultDelivery, file, file.length(), null, headerParams,
                partName, progListener);

        mr.setTag(tag);

        AppController.getInstance().addToRequestQueue(mr);
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putString(STATE_ISSUENAME, editTitle.getText().toString());
        savedInstanceState.putString(STATE_LOCATION, editLocation.getText().toString());
        savedInstanceState.putString(STATE_IMAGEPATH, mCurrentImagePath);
        savedInstanceState.putString(STATE_DESC, editDescription.getText().toString());
        savedInstanceState.putInt(STATE_URGENCY, spinnerUrgency.getSelectedItemPosition());
        savedInstanceState.putString(STATE_REPORTER, editReporter.getText().toString());
        savedInstanceState.putString(STATE_EMAIL, editEmail.getText().toString());
        savedInstanceState.putString(STATE_CONTACT, editContact.getText().toString());
        savedInstanceState.putParcelable(STATE_IMAGEFILE_URI, currentImageUri);
        savedInstanceState.putString(STATE_FILENAME, mFileName);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_issue, menu);
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
                Intent settingIntent = new Intent();
                settingIntent.setClass(this, SetPreferenceActivity.class);
                startActivity(settingIntent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // This will fix the file name when the camera app changes orientation
        newIssue.setImage_url(mFileName);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        Common.hidePDialog(pDialog);
    }

}