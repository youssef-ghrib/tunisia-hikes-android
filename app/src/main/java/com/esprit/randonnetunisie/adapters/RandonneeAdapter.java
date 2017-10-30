package com.esprit.randonnetunisie.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.activities.DetailActivity;
import com.esprit.randonnetunisie.activities.MainActivity;
import com.esprit.randonnetunisie.entities.Participation;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by youss on 14/11/2016.
 */

public class RandonneeAdapter extends RecyclerView.Adapter<RandonneeAdapter.ViewHolder> {

    private static final String TAG = "RandonneeAdapter";

    List<Randonnee> randonnees;
    Map<Integer, Participation> map;
    int resource;
    static Context context;
    User user;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        View itemView;
        ImageView img;
        TextView day;
        TextView month;
        TextView title;
        TextView timeLocation;
        Button btnWishlist;
        Button btnShare;

        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            img = (ImageView) itemView.findViewById(R.id.randonnee_img);
            day = (TextView) itemView.findViewById(R.id.randonnee_day);
            month = (TextView) itemView.findViewById(R.id.randonnee_month);
            title = (TextView) itemView.findViewById(R.id.randonnee_title);
            timeLocation = (TextView) itemView.findViewById(R.id.randonnee_time_location);
            btnWishlist = (Button) itemView.findViewById(R.id.btn_wishlist);
            btnShare = (Button) itemView.findViewById(R.id.btn_share);
        }
    }

    public RandonneeAdapter(Context context, int resource, List<Randonnee> randonnees, Map<Integer, Participation> map) {

        this.context = context;
        this.resource = resource;
        this.randonnees = randonnees;
        this.map = map;

        shared = context.getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Randonnee randonnee = randonnees.get(position);

        Picasso.with(context).load(randonnee.getPhoto()).into(holder.img);
        holder.day.setText(new SimpleDateFormat("dd").format(randonnee.getDate().getDay()));
        holder.month.setText(new SimpleDateFormat("MMM").format(randonnee.getDate().getMonth()));
        holder.title.setText(randonnee.getTitle());
        holder.timeLocation.setText(new SimpleDateFormat("h a").format(randonnee.getStartTime()) + " - " + new SimpleDateFormat("h a").format(randonnee.getEndTime()) + " @" + randonnee.getLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Participation participation = map.get(randonnee.getId());

                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("randonnee", randonnee);
                intent.putExtra("participation", participation);
                context.startActivity(intent);
            }
        });

        if (map.containsKey(randonnee.getId())) {

            Participation participation = map.get(randonnee.getId());

            if (participation.getStatus().equals("wishlist")) {

                holder.btnWishlist.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.heart), null, null, null);
            }
        }

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://play.google.com/store/search?q=Tunisia%20Hikes"))
                        .setContentDescription(randonnee.getDescription())
                        .setContentTitle(randonnee.getTitle())
                        .setImageUrl(Uri.parse(randonnee.getPhoto()))
                        .build();

                ShareDialog shareDialog = new ShareDialog((MainActivity)context);
                shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            }
        });

        holder.btnWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

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

                                                holder.btnWishlist.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.heart), null, null, null);
                                                FirebaseMessaging.getInstance().unsubscribeFromTopic(String.valueOf(randonnee.getId()));
                                                Participation participation = new Participation(randonnee.getId(), "wishlist", user.getId());
                                                map.put(participation.getRandonneeId(), participation);

                                            } else {

                                                holder.btnWishlist.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.heart_empty), null, null, null);
                                                map.remove(randonnee.getId());
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
                                    MySingleton.getInstance(context).addToRequestQueue(stringRequest);

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

                                        holder.btnWishlist.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.heart), null, null, null);
                                        Participation participation = new Participation(randonnee.getId(), "wishlist", user.getId());
                                        map.put(participation.getRandonneeId(), participation);
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
                                MySingleton.getInstance(context).addToRequestQueue(stringRequest);
                            }
                        });

                jsObjRequest.setTag(TAG);
                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return randonnees.size();
    }
}
