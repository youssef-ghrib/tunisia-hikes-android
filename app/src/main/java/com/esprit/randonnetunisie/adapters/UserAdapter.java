package com.esprit.randonnetunisie.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by youss on 29/11/2016.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "UserAdapter";

    List<User> users;
    int resource;
    static Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        CircleImageView item_img_user;

        public ViewHolder(View itemView) {
            super(itemView);

            item_img_user = (CircleImageView) itemView.findViewById(R.id.item_img_user);
        }
    }

    public UserAdapter(Context context, int resource, List<User> users) {

        this.context = context;
        this.resource = resource;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        User user = users.get(position);

        Picasso.with(context).load(user.getPhoto()).into(holder.item_img_user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
