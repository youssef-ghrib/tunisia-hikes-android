package com.esprit.randonnetunisie.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.activities.GalleryActivity;
import com.esprit.randonnetunisie.entities.Media;
import com.esprit.randonnetunisie.entities.Post;
import com.esprit.randonnetunisie.entities.User;
import com.squareup.picasso.Picasso;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by youss on 29/11/2016.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";

    List<Post> posts;
    int resource;
    static Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        ImageView post_user_img;
        TextView post_user_name;
        TextView post_text;
        TextView post_nbr;
        ImageView post_media;

        public ViewHolder(View itemView) {
            super(itemView);

            post_user_img = (ImageView) itemView.findViewById(R.id.post_user_img);
            post_user_name = (TextView) itemView.findViewById(R.id.post_user_name);
            post_text = (TextView) itemView.findViewById(R.id.post_text);
            post_media = (ImageView) itemView.findViewById(R.id.post_media);
            post_nbr = (TextView) itemView.findViewById(R.id.post_nbr);
        }
    }

    public PostAdapter(Context context, int resource, List<Post> posts) {
        System.out.println("azerty");
        this.context = context;
        this.resource = resource;
        this.posts = posts;
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

        final Post post = posts.get(position);

        String url2 = "https://randonnee-tunisie.000webhostapp.com/randonnee/getUserById.php?id=" + post.getUserId();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    ObjectMapper mapper = new ObjectMapper();
                    final User user = mapper.readValue(response.toString(), User.class);

                    String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getAllMediaByPost.php?post_id=" + post.getId();

                    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            final ArrayList<Media> medias = new ArrayList<>();
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

                            Picasso.with(context).load(user.getThumbnail()).placeholder(R.drawable.user).into(holder.post_user_img);
                            holder.post_user_name.setText(user.getName());
                            holder.post_text.setText(post.getText());

                           /* if(medias.size() == 0){

                                if(((ViewGroup) holder.post_media.getParent()) != null){

                                    ((ViewGroup) holder.post_media.getParent()).removeView(holder.post_media);
                                    ((ViewGroup) holder.post_nbr.getParent()).removeView(holder.post_nbr);
                                }
                            }*/

                            if (medias.size() > 0) {

                                Picasso.with(context).load(medias.get(0).getThumbnail()).into(holder.post_media);
                            }

                            if (medias.size() > 1) {

                                holder.post_nbr.setText("+" + (medias.size() - 1));
                            } else {
                                holder.post_nbr.setText("");
                            }

                            holder.post_media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent intent = new Intent(context, GalleryActivity.class);
                                    intent.putParcelableArrayListExtra("medias", medias);
                                    context.startActivity(intent);
                                }
                            });

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

                    jsonArrayRequest.setTag(TAG);

                    // Access the RequestQueue through your singleton class.
                    MySingleton.getInstance(context).addToRequestQueue(jsonArrayRequest);

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

        jsObjRequest.setTag(TAG);
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
