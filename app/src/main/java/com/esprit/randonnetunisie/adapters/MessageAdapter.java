package com.esprit.randonnetunisie.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.Message;
import com.esprit.randonnetunisie.entities.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by youss on 23/11/2016.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter";

    Context context;
    List<Message> messages;
    User user;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    public MessageAdapter(Context context, List<Message> messages) {

        this.context = context;
        this.messages = messages;

        shared = context.getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = null;
        if(viewType == 1){

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else if(viewType == 2){

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other, parent, false);
        }

        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Message message = messages.get(position);

        holder.text.setText(message.getText());

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getUserById.php?id=" + message.getUserId();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            ObjectMapper mapper = new ObjectMapper();
                            User user = mapper.readValue(response.toString(), User.class);

                            Picasso.with(context).load(user.getPhoto()).into(holder.img);

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

        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        Message message = messages.get(position);

        if(message != null){

            if(message.getUserId() == user.getId()){

                return 1;

            } else {

                return 2;
            }
        }

        return super.getItemViewType(position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        TextView text;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView.findViewById(R.id.chat_text);
            img = (ImageView) itemView.findViewById(R.id.chat_img);
        }
    }
}
