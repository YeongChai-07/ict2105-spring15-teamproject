package com.example.insite.app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by SITUSER on 25/3/2015.
 */
public class reportIssueActivity extends ActionBarActivity {

    private EditText m_IssueName = null;
    private EditText m_Location = null;
    private Button m_Camera = null;
    private Spinner m_Urgency = null;
    private ImageView m_Photo = null;
    private EditText m_Desc = null;
    private Button m_submitIssue = null;

    final String NAME_ERROR_MSG = "Please provide us with your NAME";
    final String LOCATION_ERROR_MSG = "Please provide us with the LOCATION where the fault occurs";
    final String ISSUE_DESC_ERROR_MSG = "Please provide us with the DESCRIPTION of where the fault occurs";

    final String KEY_ISSUENAME = "RESTORE_ISSUENAME";
    final String KEY_LOCATION = "RESTORE_ISSUE_LOCATION";
    final String KEY_URGENCY = "RESTORE_ISSUE_URGENCY";
    final String KEY_IMAGEPATH = "RESTORE_IMAGE_PATH";
    final String KEY_DESC = "RESTORE_ISSUE_DESCRIPTION";

    final String IMAGE_PATH = "/storage/sdcard0/APP_PICTURES/com.example.insite.app/";
    final String ACTIVITY_TAG = "REPORT ISSUE_SCREEN";

    final String SUBMIT_ISSUE_URL = "http://www.metalvilletrading.com.sg/insite/v1/issue";

    // Authorisation token
    private static final String token = "51d3b1d3beb959685da8fa662de3948a";


    private String urgencyType = "";
    private String issueName = "";
    private String issueLocation = "";
    private String issueDesc = "";
    private String imageFileName = "test.jpg";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_issue_layout);

        m_IssueName = (EditText) findViewById(R.id.enter_IssueName);
        m_Location = (EditText) findViewById(R.id.enter_IssueLocation);
        m_Camera = (Button) findViewById(R.id.button_captureImage);
        m_Urgency = (Spinner) findViewById (R.id.spinner_urgencyPicker);
        m_Photo = (ImageView)findViewById(R.id.issue_Image);
        m_Desc = (EditText) findViewById(R.id.multiLine_Description);
        m_submitIssue = (Button) findViewById(R.id.button_submitIssue);

        m_Photo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
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

//                m_Urgency.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                         parent.getItemAtPosition(position).toString();
//
//                    }
//                });

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
       //savedInstanceState.putString(KEY_IMAGEPATH, )
       savedInstanceState.putString(KEY_ISSUENAME, m_IssueName.getText().toString());

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        m_IssueName.setText(savedInstanceState.getString(KEY_ISSUENAME));
        m_Location.setText(savedInstanceState.getString(KEY_LOCATION));
        String uriPath = "BLANK";
        try
        {
            URI myUri = new URI("file",null,IMAGE_PATH + imageFileName,null);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(myUri) ) );
            Drawable myDraw = Drawable.createFromStream(bis, "BLAH");
            m_Photo.setImageDrawable(myDraw);
            uriPath = myUri.getRawPath();

        }
        catch(URISyntaxException use)
        {
            Log.e(ACTIVITY_TAG, "PROBLEM with the URI !\nMaybe you provides a invalid URI!");
        }
        catch(FileNotFoundException fne)
        {
            Log.e(ACTIVITY_TAG, "PROBLEM looking up the file - "+ imageFileName + "\n Perhaps " + imageFileName + " is not found or the provided partition/drive doesn't have the file");
            Log.e(ACTIVITY_TAG, "URI is : " + uriPath);

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
                myMap.put(IssueContent.IMAGEPATH_FIELD, "null");
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

        Toast.makeText(this, "Today's date is: " + getTodayDate() + "\nCurrent Time is : " + getCurrentTime(),Toast.LENGTH_LONG ).show();

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

}
