package com.example.insite.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.insite.app.model.IssueContent;

import com.example.insite.app.app.AppController;
import com.example.insite.app.util.imagePOST_RequestHelper;

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

/**
 * Created by Tan Yeong Chai on 25/3/2015.
 */
public class reportIssueActivity extends ActionBarActivity {

    private EditText m_IssueName = null;
    private EditText m_Location = null;
    private Button m_Camera = null;
    private Spinner m_Urgency = null;
    private ImageView m_IssueImage = null;
    private EditText m_Desc = null;
    private Button m_submitIssue = null;

    private static final String CAMERA_TAG = "Camera_Test";

    final String NAME_ERROR_MSG = "Please provide us with your NAME";
    final String LOCATION_ERROR_MSG = "Please provide us with the LOCATION where the fault occurs";
    final String ISSUE_DESC_ERROR_MSG = "Please provide us with the DESCRIPTION of where the fault occurs";
    final String NO_IMAGE_LOADED = "null";

    final String KEY_ISSUENAME = "RESTORE_ISSUENAME";
    final String KEY_LOCATION = "RESTORE_ISSUE_LOCATION";
    final String KEY_URGENCY = "RESTORE_ISSUE_URGENCY";
    final String KEY_IMAGEPATH = "RESTORE_IMAGE_PATH";
    final String KEY_DESC = "RESTORE_ISSUE_DESCRIPTION";
    final String ACTIVITY_TAG = "REPORT ISSUE_SCREEN";

    final String SUBMIT_ISSUE_URL = "http://www.metalvilletrading.com.sg/insite/v1/issue";
    final String UPLOAD_IMAGE_URL = "http://www.metalvilletrading.com.sg/insite/v1/image";

    // Authorisation token
    private static final String token = "51d3b1d3beb959685da8fa662de3948a";

    //Camera Attributes
    String currentImagePath = NO_IMAGE_LOADED;
    static File imagePath;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001;
    private Uri currentImageUri;
    String fileName = "";
    public static final int MEDIA_TYPE_IMAGE = 1;
    Bitmap bMapScaled;


    private String urgencyType = "";
    private String issueName = "";
    private String issueLocation = "";
    private String issueDesc = "";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_issue_layout);

        m_IssueName = (EditText) findViewById(R.id.enter_IssueName);
        m_Location = (EditText) findViewById(R.id.enter_IssueLocation);
        m_Camera = (Button) findViewById(R.id.button_captureImage);
        m_Urgency = (Spinner) findViewById (R.id.spinner_urgencyPicker);
        m_IssueImage = (ImageView)findViewById(R.id.issue_Image);
        m_Desc = (EditText) findViewById(R.id.multiLine_Description);
        m_submitIssue = (Button) findViewById(R.id.button_submitIssue);

        handleImageFrom_Resources();

        if(savedInstanceState != null)
        {
            m_IssueName.setText(savedInstanceState.getString(KEY_ISSUENAME));
            m_Location.setText(savedInstanceState.getString(KEY_LOCATION));
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
               String validateString = m_Desc.getText().toString();
               validateString.trim();
               //validateString = validateString.replaceAll("\n", "");
               System.out.println("The content is : " + validateString);

               if(validateInput_Failed (m_IssueName.getText().toString()) )
               {
                   m_IssueName.requestFocus();
                   m_IssueName.setError(NAME_ERROR_MSG);    //Shows a error message label
                   return;

               }

               if(validateInput_Failed(m_Location.getText().toString()) )
               {
                   m_Location.requestFocus();
                   m_Location.setError(LOCATION_ERROR_MSG);    //Shows a error message label
                   return;
               }

               if(validateInput_Failed(m_Desc.getText().toString()) )
               {
                   m_Desc.requestFocus();
                   m_Desc.setError(ISSUE_DESC_ERROR_MSG);    //Shows a error message label

                   return;

               }

               //Insert to database

                issueName = m_IssueName.getText().toString().trim();
                issueLocation = m_Location.getText().toString().trim();
                issueDesc = m_Desc.getText().toString().trim();

                urgencyType = m_Urgency.getSelectedItem().toString();

                //Save the record
                submitIssue();

                finish();

            }

        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
       savedInstanceState.putString(KEY_ISSUENAME, m_IssueName.getText().toString());
       savedInstanceState.putString(KEY_LOCATION, m_Location.getText().toString());
       savedInstanceState.putString(KEY_URGENCY, m_Urgency.getSelectedItem().toString());

       savedInstanceState.putString(KEY_IMAGEPATH, currentImagePath );
       savedInstanceState.putString(KEY_DESC, m_Desc.getText().toString());

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


    private boolean validateInput_Failed(String stringToCheck)
    {
       //stringToCheck.trim();
       stringToCheck = stringToCheck.replaceAll(" ", "");
       stringToCheck = stringToCheck.replaceAll("\n", "");
       return (stringToCheck.isEmpty());
    }

    private void submitIssue()
    {
        IssueContent newIssue = new IssueContent();
        newIssue.setIssueName(issueName);
        newIssue.setIssueLocation(issueLocation);
        newIssue.setDescription(issueDesc);
        newIssue.setUrgency(urgencyType);

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
                Map<String, String> myMap  = new HashMap<String, String>();
                myMap.put(IssueContent.ISSUE_FIELD, issueName);
                myMap.put(IssueContent.DESC_FIELD, issueDesc);
                myMap.put(IssueContent.LOCATION_FIELD, issueLocation);
                myMap.put(IssueContent.LATITUDE_FIELD, "0");
                myMap.put(IssueContent.LONGITUDE_FIELD, "0");

                if (!currentImagePath.equals(NO_IMAGE_LOADED)) {

                    String fileName = "";
                    StringTokenizer st = new StringTokenizer(currentImagePath, "/");

                    while (st.hasMoreTokens()) {
                        fileName = st.nextToken();

                    }

                    System.out.println("File : " + fileName);
                    myMap.put(IssueContent.IMAGEPATH_FIELD, fileName);
                }
                else {
                    myMap.put(IssueContent.IMAGEPATH_FIELD, currentImagePath);
                }
                myMap.put(IssueContent.DATE_REPORTED_FIELD, getTodayDate());
                myMap.put(IssueContent.TIME_REPORTED_FIELD, getCurrentTime());
                myMap.put(IssueContent.URGENCY_FIELD, urgencyType);
                myMap.put(IssueContent.REPORTER_FIELD, "TAN YEONG CHAI (AH CHAI)");
                myMap.put(IssueContent.EMAIL_FIELD, "butchercai@gmail.com");
                myMap.put(IssueContent.MOBILE_FIELD, "97239416");

                return myMap;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(submitIssue_Req);

        if(!currentImagePath.equals(NO_IMAGE_LOADED))
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

        }


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


}
