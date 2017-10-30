package com.esprit.randonnetunisie.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.adapters.ParticipationAdapter;
import com.esprit.randonnetunisie.adapters.RandonneeAdapter;
import com.esprit.randonnetunisie.entities.Participation;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListGoingActivity extends AppCompatActivity {

    private static final String TAG = "ListGoingActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<Randonnee> randonnees;
    Map<Integer, Participation> map;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    RequestQueue queue;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_going);

        getCurrentUser();
        initDataset();

        mRecyclerView = (RecyclerView) findViewById(R.id.list_going);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ParticipationAdapter(this, R.layout.item_randonnee, randonnees, map);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initDataset() {
        randonnees = new ArrayList<>();
        map = new HashMap<>();

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getParticipationsByUser.php?user_id=" + user.getId();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        ObjectMapper mapper = new ObjectMapper();
                        final Participation participation = mapper.readValue(jsonObject.toString(), Participation.class);

                        map.put(participation.getRandonneeId(), participation);

                        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getRandonneeById.php?id=" + participation.getRandonneeId();

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    Randonnee randonnee = mapper.readValue(response.toString(), Randonnee.class);
                                    if (participation.getStatus().equals("going")) {
                                        randonnees.add(randonnee);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                } catch (JsonParseException e) {
                                    e.printStackTrace();
                                } catch (JsonMappingException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

                        jsonObjectRequest.setTag(TAG);

                        // Access the RequestQueue through your singleton class.
                        MySingleton.getInstance(ListGoingActivity.this).addToRequestQueue(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsonArrayRequest.setTag(TAG);

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(ListGoingActivity.this).addToRequestQueue(jsonArrayRequest);
    }

    private void getCurrentUser() {

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
        user = new User();
        user.setId(1);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
