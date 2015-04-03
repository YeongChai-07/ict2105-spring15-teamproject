package com.example.insite.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.insite.app.adapter.CustomListAdapter;
import com.example.insite.app.app.AppController;
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
    private static final String TAG = MainActivity.class.getSimpleName();

    // Authorisation token
    private static final String token = "51d3b1d3beb959685da8fa662de3948a";

    // Issue json url
    private static final String url = "http://www.metalvilletrading.com.sg/insite/v1/issue";
    private ProgressDialog pDialog;
    private List<Issue> issueList = new ArrayList<Issue>();
    private ListView listView;
    private CustomListAdapter adapter;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "Position";


    // TODO: Rename and change types of parameters
    private int mParam1;


    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static IssueListFragment newInstance(int param1) {
        IssueListFragment fragment = new IssueListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);

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

        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }

        // Set an Adapter to the ListView
        adapter = new CustomListAdapter(getActivity(), issueList);

        setListAdapter(adapter);


        pDialog = new ProgressDialog(getActivity());
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();


        // Creating volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        hidePDialog();

                        try {
                            JSONArray issueArray = response.getJSONArray("issue");
                            Log.d(TAG, issueArray.toString());
                        // Parsing json
                        for (int i = 0; i < issueArray.length(); i++) {

                                JSONObject obj = issueArray.getJSONObject(i);

                                Issue issue = new Issue();

                                issue.setTitle(obj.getString("issue_name"));

                                Log.d(TAG, obj.getString("image_path"));
                                issue.setThumbnailUrl(obj.getString("image_path"));

                                issue.setLocation(obj.getString("location_name"));

                                issue.setDate(obj.getString("date_reported"));

                                issue.setUrgency_level(obj.getString("urgency_level"));

                                // adding issue to issue array
                                issueList.add(issue);

                        }
                            } catch (JSONException e) {
                            e.printStackTrace();
                            }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

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
        AppController.getInstance().addToRequestQueue(movieReq);

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
            //mListener.onFragmentInteraction(IssueContent.ITEMS.get(position).id);

            mListener.onFragmentInteraction(String.valueOf(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

}
