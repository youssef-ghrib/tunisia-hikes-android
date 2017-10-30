package com.esprit.randonnetunisie.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.Media;
import com.esprit.randonnetunisie.entities.Participation;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.esprit.randonnetunisie.fragments.MapFragment;
import com.esprit.randonnetunisie.fragments.RandonneeFragment;
import com.google.gson.Gson;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    private static final String TAG = "DetailActivity";
    public static int ADD_POST_Code = 1;

    public Randonnee randonnee;
    public Participation participation;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        randonnee = (Randonnee) getIntent().getExtras().get("randonnee");
        participation = (Participation) getIntent().getExtras().get("participation");

        getCurrentUser();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.action_posts:

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new RandonneeFragment())
                                        .commit();
                                return true;

                            case R.id.action_gallery:

                                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getAllMediaByHike.php?randonnee_id=" + randonnee.getId();

                                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        ArrayList<Media> medias = new ArrayList<>();
                                        for (int i = 0; i < response.length(); i++) {
                                            try {

                                                JSONObject jsonObject = response.getJSONObject(i);
                                                ObjectMapper mapper = new ObjectMapper();
                                                Media media = mapper.readValue(jsonObject.toString(), Media.class);
                                                medias.add(media);

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

                                        Intent intent = new Intent(DetailActivity.this, GalleryActivity.class);
                                        intent.putParcelableArrayListExtra("medias", medias);
                                        startActivity(intent);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });
                                jsonArrayRequest.setTag(TAG);
                                MySingleton.getInstance(DetailActivity.this).addToRequestQueue(jsonArrayRequest);

                                return true;

                            case R.id.action_chat:

                                Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
                                intent.putExtra("randonnee", randonnee);
                                startActivity(intent);
                                return true;

                            case R.id.action_map:

                                MapFragment mapFragment = new MapFragment();
                                Bundle args = new Bundle();
                                args.putSerializable("randonnee", randonnee);
                                mapFragment.setArguments(args);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, mapFragment)
                                        .commit();
                                return true;
                        }
                        return false;
                    }
                });
        View view = bottomNavigationView.findViewById(R.id.action_posts);
        view.performClick();
    }

    public void addPost(View view) {

        Intent intent = new Intent(this, AddPostActivity.class);
        intent.putExtra("randonnee", randonnee);
        startActivityForResult(intent, ADD_POST_Code);
    }

    private void getCurrentUser() {

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
    }
}
