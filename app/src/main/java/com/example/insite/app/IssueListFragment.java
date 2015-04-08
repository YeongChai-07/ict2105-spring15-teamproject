package com.example.insite.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.insite.app.adapter.CustomListAdapter;
import com.example.insite.app.app.AppController;
import com.example.insite.app.helper.Common;
import com.example.insite.app.model.AppSetting;
import com.example.insite.app.model.Issue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class IssueListFragment extends ListFragment {

    // Log tag
    private static final String TAG = IssueListFragment.class.getSimpleName();

    // Authorisation token
    private static final String token = AppSetting.APItoken;

    // Prepare the Issue json url stucture
    private static String baseUrl = AppSetting.baseUrl;
    private static String url = null;

    private ProgressDialog pDialog;

    private List<Issue> issueList = new ArrayList<Issue>();

    private ListView listView;
    private CustomListAdapter adapter;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TAB = "Position";

    // Initialise an error variable if mTabPosition is not set
    private int mTabPosition = -1;

    private OnFragmentInteractionListener mListener;

    private LayoutInflater inflater;

    public static IssueListFragment newInstance(int mTabPosition) {
        IssueListFragment fragment = new IssueListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB, mTabPosition);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssueListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate called");

        if (getArguments() != null) {
            mTabPosition = getArguments().getInt(ARG_TAB);
        }

        // Get the listview loaded with JSON from network
        makeJsonObjectRequest();

    }

    /**
     * Method to make json object request where json response starts wtih {
     * */
    public void makeJsonObjectRequest() {

        // Issue json url
        switch(mTabPosition){
            case 0: //Should not exist
                Log.d(TAG, "Tab Position 0 is passed in");
                break;

            case 1: //Get all Pending issue
                url = baseUrl + "/status/pending";
                break;

            case 2: //Get all Resolved issue
                url = baseUrl + "/status/progress";
                break;

            case 3: //Get all Resolved issue
                url = baseUrl + "/status/resolved";
                break;

            default: //Get all issue
                //do nothing
                break;
        }

        // reinitialise a new ArrayList to remove existing items.
        issueList = new ArrayList<Issue>();

        // Set an Adapter to the ListView
        adapter = new CustomListAdapter(getActivity(), issueList);

        setListAdapter(adapter);

        // Set the emptyView to the ListView
        //View emptyView = inflater.inflate(R.layout.misc_layout, null, false);
        //getListView().setEmptyView(emptyView);
        //setEmptyText("No Data Here");

        pDialog = new ProgressDialog(getActivity());
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();


        // Creating volley request obj
        JsonObjectRequest issueReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        Common.hidePDialog(pDialog);

                        try {
                            JSONArray issueArray = response.getJSONArray("issue");
                            Log.d(TAG, issueArray.toString());
                            // Parsing json
                            for (int i = 0; i < issueArray.length(); i++) {

                                JSONObject obj = issueArray.getJSONObject(i);

                                Issue issue = new Issue();

                                issue.setIssue_id(obj.getInt("id"));
                                issue.setTitle(obj.getString("issue_name"));
                                issue.setImage_url(obj.getString("image_path"));
                                issue.setLocation(obj.getString("location_name"));
                                issue.setDate(obj.getString("date_reported"));
                                issue.setTime(obj.getString("time_reported"));
                                issue.setUrgency_level(obj.getString("urgency_level"));

                                issue.setDescription(obj.getString("description"));
                                issue.setReporter(obj.getString("reporter_name"));
                                issue.setEmail(obj.getString("email"));
                                issue.setContact(obj.getString("contact"));
                                issue.setStatus(obj.getString("status"));
                                issue.setStatus_comment(obj.getString("status_comment"));

                                // adding issue to issue array
                                issueList.add(issue);

                                Log.d(TAG, "Title: " + obj.getString("issue_name") );

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d("TAG", "Reaching notifyDataSetChange method");
                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Common.hidePDialog(pDialog);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(issueReq);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(String.valueOf(position));

            // To get the issue id (primary key) from the database instead of the order id from listview
            Issue issueObj = issueList.get(position);
            int issue_id = issueObj.getIssue_id();

            mListener.onFragmentInteraction(issue_id, issueObj);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onFragmentInteraction(int id, Parcelable issueObj);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.hidePDialog(pDialog);
    }

}
