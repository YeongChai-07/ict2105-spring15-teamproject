package com.example.insite.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.insite.app.app.AppController;
import com.example.insite.app.model.AppSetting;
import com.example.insite.app.model.Issue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

//import com.example.insite.app.util.imagePOST_RequestHelper;

/**
 * Created by Tan Yeong Chai on 25/3/2015.
 * Refactored by Lim Xing Yi
 */
public class ReportIssueActivity extends ActionBarActivity {

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CAMERA_TAG = "Camera_Test";

    private EditText editTitle;
    private EditText editLocation;
    private Button m_Camera;
    private Spinner editUrgency;
    private ImageView m_IssueImage;
    private EditText editDescription;
    private EditText editReporter;
    private EditText editEmail;
    private EditText editContact;
    private Button m_submitIssue;


    final String KEY_ISSUENAME = "RESTORE_ISSUENAME";
    final String KEY_LOCATION = "RESTORE_ISSUE_LOCATION";
    final String KEY_URGENCY = "RESTORE_ISSUE_URGENCY";
    final String KEY_IMAGEPATH = "RESTORE_IMAGE_PATH";
    final String KEY_DESC = "RESTORE_ISSUE_DESCRIPTION";
    final String ACTIVITY_TAG = "REPORT ISSUE_SCREEN";

    final String SUBMIT_ISSUE_URL = AppSetting.baseUrl;
    final String UPLOAD_IMAGE_URL = AppSetting.imagePostUrl;

    // Authorisation token
    private static final String token = "51d3b1d3beb959685da8fa662de3948a";

    //Camera Attributes
    String currentImagePath = null;
    static File imagePath;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001;
    private Uri currentImageUri;
    String fileName = "";
    public static final int MEDIA_TYPE_IMAGE = 1;
    Bitmap bMapScaled;

    final String NO_IMAGE_LOADED = "null";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);

        // to setup touch outside listener for soft keyboard dismissal
        setupUI(findViewById(R.id.scrollview_report_issue));


        final String TITLE_ERROR_MSG = getResources().getString(R.string.error_title);
        final String LOCATION_ERROR_MSG = getResources().getString(R.string.error_location);
        final String ISSUE_DESC_ERROR_MSG = getResources().getString(R.string.error_description);


        editTitle = (EditText) findViewById(R.id.edit_title);
        editLocation = (EditText) findViewById(R.id.edit_location);
        m_Camera = (Button) findViewById(R.id.button_captureImage);
        editUrgency = (Spinner) findViewById (R.id.spinner_urgencyPicker);
        m_IssueImage = (ImageView)findViewById(R.id.issue_Image);
        editDescription = (EditText) findViewById(R.id.multiLine_Description);
        editReporter = (EditText) findViewById(R.id.edit_reporter);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editContact = (EditText) findViewById(R.id.edit_contact);
        m_submitIssue = (Button) findViewById(R.id.button_submitIssue);


        editTitle.clearFocus();

        handleImageFrom_Resources();

        if(savedInstanceState != null)
        {
            editTitle.setText(savedInstanceState.getString(KEY_ISSUENAME));
            editLocation.setText(savedInstanceState.getString(KEY_LOCATION));
            currentImagePath = savedInstanceState.getString(KEY_IMAGEPATH);
            try
            {
                if(!currentImagePath.equals(NO_IMAGE_LOADED) )
                {
                    handleImageFrom_PictureFile(currentImagePath);
                }
           }
           catch(NullPointerException npe)
           {

           }

        }

        m_IssueImage.setImageBitmap(bMapScaled);

        m_Camera.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
//                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
//                startActivity(cameraIntent);
                if (currentImageUri == null) {
                    currentImageUri = getImageFileUri();
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri); // set the image file name
                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        });

        m_submitIssue.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
               String validateString = editDescription.getText().toString();
               validateString.trim();
               //validateString = validateString.replaceAll("\n", "");
               System.out.println("The content is : " + validateString);

               if(isInputEmpty(editTitle.getText().toString()) )
               {
                   editTitle.requestFocus();
                   editTitle.setError(TITLE_ERROR_MSG);    //Shows a error message label
                   return;

               }

               if(isInputEmpty(editLocation.getText().toString()) )
               {
                   editLocation.requestFocus();
                   editLocation.setError(LOCATION_ERROR_MSG);    //Shows a error message label
                   return;
               }

               if(isInputEmpty(editDescription.getText().toString()) )
               {
                   editDescription.requestFocus();
                   editDescription.setError(ISSUE_DESC_ERROR_MSG);    //Shows a error message label

                   return;

               }



                String issueName = editTitle.getText().toString().trim();
                String issueLocation = editLocation.getText().toString().trim();
                String issueDesc = editDescription.getText().toString().trim();
                String urgencyType = editUrgency.getSelectedItem().toString();
                String reporter = editReporter.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String contact = editEmail.getText().toString().trim();

                Issue newIssue = new Issue();
                newIssue.setTitle(issueName);
                newIssue.setDate(getTodayDate());
                newIssue.setTime(getCurrentTime());
                newIssue.setLocation(issueLocation);
                newIssue.setDescription(issueDesc);
                newIssue.setUrgency_level(urgencyType);
                newIssue.setReporter(reporter);
                newIssue.setEmail(email);
                newIssue.setContact(contact);

                //Save the record
                submitIssue(newIssue);

                //finish();

            }

        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
       savedInstanceState.putString(KEY_ISSUENAME, editTitle.getText().toString());
       savedInstanceState.putString(KEY_LOCATION, editLocation.getText().toString());
       savedInstanceState.putString(KEY_URGENCY, editUrgency.getSelectedItem().toString());

       savedInstanceState.putString(KEY_IMAGEPATH, currentImagePath );
       savedInstanceState.putString(KEY_DESC, editDescription.getText().toString());

    }

    //TODO: For use to return the result back from the camera Intent Service
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(CAMERA_TAG, "Entered onActivityResult method");

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(CAMERA_TAG, "Trying to save image!");
                galleryAddPic();

                //setPic();
                //if(currentImagePath == null){
                    currentImagePath= currentImageUri.getPath();
                //}
                //ImageView imageViewer = (ImageView) findViewById(R.id.imagePreviewThumb);
                handleImageFrom_PictureFile(currentImagePath);
                // Set image to viewer
                m_IssueImage.setImageBitmap(bMapScaled);

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }


    private boolean isInputEmpty(String stringToCheck)
    {
       //stringToCheck.trim();
       stringToCheck = stringToCheck.replaceAll(" ", "");
       stringToCheck = stringToCheck.replaceAll("\n", "");
       return stringToCheck.isEmpty();
    }

    private void submitIssue(final Issue issue)
    {
        Log.d(TAG, issue.getTitle());

        StringRequest submitIssue_Req = new StringRequest(Request.Method.POST, SUBMIT_ISSUE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(ACTIVITY_TAG, "This HTTP response is : " + response);
            }

        },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e(ACTIVITY_TAG, "OOPS... An error occurred. INFO: " + error);
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


                if (!currentImagePath.equals(NO_IMAGE_LOADED)) {

                    String fileName = "";
                    StringTokenizer st = new StringTokenizer(currentImagePath, "/");

                    while (st.hasMoreTokens()) {
                        fileName = st.nextToken();

                    }

                    System.out.println("File : " + fileName);
                     issueMap.put("image_path", fileName);
                }
                else {
                     issueMap.put("image_path", currentImagePath);
                }
                 issueMap.put("date_reported", issue.getDate());
                 issueMap.put("time_reported", issue.getTime());
                 issueMap.put("urgency_level", issue.getUrgency_level());
                 issueMap.put("reporter", issue.getReporter());
                 issueMap.put("email", issue.getEmail());
                 issueMap.put("mobile", issue.getContact());

                return  issueMap;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(submitIssue_Req);

/*        if(!currentImagePath.equals(NO_IMAGE_LOADED))
        {
            imagePOST_RequestHelper uploadImage = new imagePOST_RequestHelper(UPLOAD_IMAGE_URL, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e(ACTIVITY_TAG, "OOPS... An error occurred. INFO: " + error);
                }
            },new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(ACTIVITY_TAG, "This HTTP response is : " + response);
                }

            }, new File(currentImagePath)

            );

            AppController.getInstance().addToRequestQueue(uploadImage);
            Toast.makeText(this, "File Name : " + currentImagePath, Toast.LENGTH_LONG).show();

        }*/


        //Toast.makeText(this, "Today's date is: " + getTodayDate() + "\nCurrent Time is : " + getCurrentTime(),Toast.LENGTH_LONG ).show();

    }

    private String getTodayDate()
    {
        TimeZone currZone = TimeZone.getTimeZone("Asia/Singapore");
        Calendar myCal = Calendar.getInstance(currZone, new Locale("en"));
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");


        return ( sdf.format(myCal.getTime()) );
    }

    private String getCurrentTime()
    {
        TimeZone currZone = TimeZone.getTimeZone("Asia/Singapore");
        Calendar myCal = Calendar.getInstance(currZone, new Locale("en"));
        String timeNow = String.format("%02d", myCal.get(myCal.HOUR_OF_DAY) ) + ":" + String.format("%02d", myCal.get(myCal.MINUTE) )
                + ":" + String.format("%02d", myCal.get(myCal.SECOND));

        return timeNow;
    }

    // TODO: Added the Camera functions
    private static Uri getImageFileUri() {
        // Create a storage directory for the images
        // To be safe(er), you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this
        imagePath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "inSITe");
        Log.d(CAMERA_TAG, "Find " + imagePath.getAbsolutePath());
        if (!imagePath.exists()) {
            if (!imagePath.mkdirs()) {
                Log.d("CameraTestIntent", "failed to create directory");
                return null;
            } else {
                Log.d(CAMERA_TAG, "create new inSITe folder");
            }
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(imagePath, "inSITe" + timeStamp + ".jpg");

        if (!image.exists()) {
            try {
                image.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //return image;
        // Create an File Uri
        return Uri.fromFile(image);
    }

    private void galleryAddPic() {
        /**
         * copy current image to Gallery
         */
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(currentImageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void handleImageFrom_PictureFile(String path)
    {
        // Decode image url and retrieve the image
        Bitmap bMap = BitmapFactory.decodeFile(path);
        // Rotate image
        bMap = imageOrientationValidator(bMap,path);
        // Resize image
        bMapScaled = Bitmap.createScaledBitmap(bMap, 800, 600, true);
    }

    private void handleImageFrom_Resources()
    {
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.sit_logo);
        bMapScaled = Bitmap.createScaledBitmap(bMap,800,600, true);
    }

    private Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
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
