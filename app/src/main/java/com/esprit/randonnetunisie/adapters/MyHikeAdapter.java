package com.esprit.randonnetunisie.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.activities.DetailActivity;
import com.esprit.randonnetunisie.entities.Participation;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by youss on 03/01/2017.
 */

public class MyHikeAdapter extends RecyclerView.Adapter<MyHikeAdapter.ViewHolder> {

    private static final String TAG = "RandonneeAdapter";

    List<Randonnee> randonnees;
    Map<Integer, Participation> map;
    int resource;
    static Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        View itemView;
        ImageView img;
        TextView date;
        TextView title;
        TextView timeLocation;
        Button btnDelete;


        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            img = (ImageView) itemView.findViewById(R.id.my_list_img);
            date = (TextView) itemView.findViewById(R.id.my_list_date);
            title = (TextView) itemView.findViewById(R.id.my_list_title);
            timeLocation = (TextView) itemView.findViewById(R.id.my_list_time);
            btnDelete = (Button) itemView.findViewById(R.id.my_list_btn);
        }
    }

    public MyHikeAdapter(Context context, int resource, List<Randonnee> randonnees, Map<Integer, Participation> map) {

        this.context = context;
        this.resource = resource;
        this.randonnees = randonnees;
        this.map = map;
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
        holder.date.setText(randonnee.getDate().getDay() + "/" + randonnee.getDate().getMonth() + "/" + randonnee.getDate().getYear());
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

        if(randonnee.getStatus().equals("canceled")){

            holder.btnDelete.setText("Canceled");
            holder.btnDelete.setEnabled(false);
            holder.btnDelete.setBackgroundColor(Color.RED);
            holder.btnDelete.setTextColor(Color.WHITE);
        }

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/cancelHike.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        holder.btnDelete.setText("Canceled");
                        holder.btnDelete.setEnabled(false);
                        holder.btnDelete.setBackgroundColor(Color.RED);
                        holder.btnDelete.setTextColor(Color.WHITE);
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
                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("id", String.valueOf(randonnee.getId()));

                        //returning parameters
                        return params;
                    }
                };
                stringRequest.setTag(TAG);

                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(context).addToRequestQueue(stringRequest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return randonnees.size();
    }
}
