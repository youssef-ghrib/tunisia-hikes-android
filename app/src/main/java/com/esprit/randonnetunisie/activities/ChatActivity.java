package com.esprit.randonnetunisie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MobilePushReceiver;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.adapters.MessageAdapter;
import com.esprit.randonnetunisie.entities.Message;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    @BindView(R.id.chat_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.chat_text)
    EditText chatText;

    // Get a RequestQueue
    RequestQueue queue;

    Randonnee randonnee;
    List<Message> messages;
    User user;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    MobilePushReceiver mobilePushReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        randonnee = (Randonnee) getIntent().getExtras().get("randonnee");
        initDataset();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MessageAdapter(this, messages);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mobilePushReceiver = new MobilePushReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);

                Bundle bundle = intent.getExtras();

                Message message = new Message(bundle.getString("text"), Integer.parseInt(bundle.getString("randonnee_id")), Integer.parseInt(bundle.getString("user_id")));

                messages.add(message);
                mAdapter.notifyDataSetChanged();
            }
        };

        registerReceiver(mobilePushReceiver, new IntentFilter("broadCastName"));
    }

    /**
     * Generates data for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {

        messages = new ArrayList<>();

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getAllMessages.php?randonnee=" + randonnee.getId();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {

                        JSONObject jsonObject = response.getJSONObject(i);
                        ObjectMapper mapper = new ObjectMapper();
                        Message message = mapper.readValue(jsonObject.toString(), Message.class);
                        messages.add(message);

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
        MySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    public void send(View view) {

        FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(randonnee.getId()));
        final String text =chatText.getText().toString().trim();
        chatText.setText("");

        String url = "https://fcm.googleapis.com/fcm/send";

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "    \"data\": {\n" +
                    "        \"text\": \"" + text + "\",\n" +
                    "        \"randonnee_id\": " + randonnee.getId() + ",\n" +
                    "        \"user_id\": " + user.getId() + "\n" +
                    "    },\n" +
                    "    \"to\": \"/topics/" + randonnee.getId() + "\"\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/sendMessage.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();
                    }
                }) {
                    @Override
                    public Map<String, String> getParams() {

                        //Creating parameters
                        Map<String, String> params = new Hashtable<String, String>();

                        //Adding parameters
                        params.put("text", text);
                        params.put("randonnee_id", String.valueOf(randonnee.getId()));
                        params.put("user_id", String.valueOf(user.getId()));

                        //returning parameters
                        return params;
                    }
                };

                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> mHeaders = new HashMap<>();
                mHeaders.put("Content-Type", "application/json");
                mHeaders.put("Authorization", "key=AAAAgRvkgk0:APA91bH00BJXuZ_1_UW7MdS6YzR1efyYsIyXvH7mSAAZdAynpzXjUduGCpYk44jTkt2YFNkc14Vzl2OVTYF_KVyawYY_-SSh44BMu2wizNgi4TzYXBp4f8lJADATeEhW_C73eDPOBlBJPMSxOFqFqrrjg46gllOWQg");

                return mHeaders;
            }
        };

        jsObjRequest.setTag(TAG);
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mobilePushReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
