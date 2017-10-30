package com.esprit.randonnetunisie.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.activities.DetailActivity;
import com.esprit.randonnetunisie.adapters.PostAdapter;
import com.esprit.randonnetunisie.adapters.UserAdapter;
import com.esprit.randonnetunisie.entities.Media;
import com.esprit.randonnetunisie.entities.Participation;
import com.esprit.randonnetunisie.entities.Post;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

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
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class RandonneeFragment extends Fragment {

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    private static final String TAG = "RandonneeFragment";

    // Get a RequestQueue
    RequestQueue queue;

    @BindView(R.id.hike_img)
    ImageView hike_img;
    @BindView(R.id.hike_title)
    TextView hike_title;
    @BindView(R.id.hike_type)
    TextView hike_type;
    @BindView(R.id.hike_nbr_participants)
    TextView hike_nbr_participants;
    @BindView(R.id.hike_participants)
    RecyclerView hike_participants;
    @BindView(R.id.hike_posts)
    RecyclerView hike_posts;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.toolbar)
    CollapsingToolbarLayout toolbar;
    @BindView(R.id.hike_wishlist)
    FloatingActionButton btnWishlist;
    @BindView(R.id.hike_going)
    FloatingActionButton btnGoing;
    @BindView(R.id.item_img_user)
    CircleImageView invite;

    RecyclerView.Adapter userAdapter;
    RecyclerView.Adapter postAdapter;

    List<Post> posts;

    List<User> participants;
    Randonnee randonnee;
    Participation participation;
    User user;

    public RandonneeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataset();
        getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_randonnee, container, false);
        ButterKnife.bind(this, view);

        toolbar.setTitle(randonnee.getLocation());
        Picasso.with(getActivity()).load(randonnee.getPhoto()).into(hike_img);
        hike_title.setText(randonnee.getTitle());
        hike_type.setText(randonnee.getType() + " hike");

        if(participation != null){

            if (participation.getStatus().equals("going")){
                btnGoing.setImageResource(R.drawable.checked_filled_24);
            } else if (participation.getStatus().equals("wishlist")){
                btnWishlist.setImageResource(R.drawable.heart);
            }
        }

        hike_participants.setHasFixedSize(true);
        RecyclerView.LayoutManager horizontal = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        hike_participants.setLayoutManager(horizontal);
        userAdapter = new UserAdapter(getActivity(), R.layout.item_user_img, participants);
        hike_participants.setAdapter(userAdapter);

        hike_posts.setHasFixedSize(false);
        RecyclerView.LayoutManager vertical = new LinearLayoutManager(getActivity());
        hike_posts.setLayoutManager(vertical);
        postAdapter = new PostAdapter(getActivity(), R.layout.item_post, posts);
        hike_posts.setAdapter(postAdapter);

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String appLinkUrl, previewImageUrl;

                appLinkUrl = "https://fb.me/1821883938054822";
                previewImageUrl = randonnee.getPhoto();

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(previewImageUrl)
                            .build();
                    AppInviteDialog.show(getActivity(), content);
                }
            }
        });

        btnWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/checkParticipation.php?randonnee=" + randonnee.getId() + "&user=" + user.getId();

                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    final String status = response.getString("status");

                                    String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/updateWishlist.php";

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            if (status.equals("going")) {

                                                btnWishlist.setImageResource(R.drawable.heart);
                                                btnGoing.setImageResource(R.drawable.checked_24);
                                            } else {
                                                btnWishlist.setImageResource(R.drawable.heart_empty);
                                            }
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
                                            params.put("randonnee", String.valueOf(randonnee.getId()));
                                            params.put("user", String.valueOf(user.getId()));
                                            params.put("status", status);

                                            //returning parameters
                                            return params;
                                        }
                                    };
                                    ;

                                    // Access the RequestQueue through your singleton class.
                                    MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/addParticipation.php";

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        btnWishlist.setImageResource(R.drawable.heart);
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
                                        params.put("randonnee", String.valueOf(randonnee.getId()));
                                        params.put("user", String.valueOf(user.getId()));
                                        params.put("status", "wishlist");

                                        //returning parameters
                                        return params;
                                    }
                                };
                                ;

                                // Access the RequestQueue through your singleton class.
                                MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
                            }
                        });

                jsObjRequest.setTag(TAG);
                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
            }
        });

        btnGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/checkParticipation.php?randonnee=" + randonnee.getId() + "&user=" + user.getId();

                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    final String status = response.getString("status");

                                    String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/updateGoing.php";

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            if (status.equals("wishlist")) {

                                                btnGoing.setImageResource(R.drawable.checked_filled_24);
                                                btnWishlist.setImageResource(R.drawable.heart_empty);
                                                FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(randonnee.getId()));
                                            } else {
                                                btnGoing.setImageResource(R.drawable.checked_24);
                                                FirebaseMessaging.getInstance().unsubscribeFromTopic(String.valueOf(randonnee.getId()));
                                            }
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
                                            params.put("randonnee", String.valueOf(randonnee.getId()));
                                            params.put("user", String.valueOf(user.getId()));
                                            params.put("status", status);

                                            //returning parameters
                                            return params;
                                        }
                                    };
                                    ;

                                    // Access the RequestQueue through your singleton class.
                                    MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/addParticipation.php";

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        btnGoing.setImageResource(R.drawable.checked_filled_24);
                                        FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(randonnee.getId()));
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
                                        params.put("randonnee", String.valueOf(randonnee.getId()));
                                        params.put("user", String.valueOf(user.getId()));
                                        params.put("status", "going");

                                        //returning parameters
                                        return params;
                                    }
                                };
                                ;

                                // Access the RequestQueue through your singleton class.
                                MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
                            }
                        });

                jsObjRequest.setTag(TAG);
                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
            }
        });

        return view;
    }

    private void initDataset() {

        randonnee = ((DetailActivity) getActivity()).randonnee;
        participation = ((DetailActivity) getActivity()).participation;
        getAllGoing();
        getAllPosts();
    }

    public void getAllGoing() {

        participants = new ArrayList<>();
        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getAllGoing.php?randonnee_id=" + randonnee.getId();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        ObjectMapper mapper = new ObjectMapper();
                        User user = mapper.readValue(jsonObject.toString(), User.class);
                        participants.add(user);

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

                hike_nbr_participants.setText(participants.size() + " participants");
                userAdapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsonArrayRequest.setTag(TAG);

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }

    public void getAllPosts() {

        posts = new ArrayList<>();
        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getAllPosts.php?randonnee_id=" + randonnee.getId();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        ObjectMapper mapper = new ObjectMapper();
                        Post post = mapper.readValue(jsonObject.toString(), Post.class);

                        posts.add(post);

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

                if (posts.isEmpty()) {
                    hike_posts.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    postAdapter.notifyDataSetChanged();
                    hike_posts.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
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
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }

    private void getCurrentUser(){

        shared = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
