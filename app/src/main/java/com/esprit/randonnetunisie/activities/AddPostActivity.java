package com.esprit.randonnetunisie.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.Randonnee;
import com.esprit.randonnetunisie.entities.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.darsh.multipleimageselect.R.id.image;

public class AddPostActivity extends AppCompatActivity {

    @BindView(R.id.add_post_user_img)
    ImageView add_post_user_img;
    @BindView(R.id.add_post_user_name)
    TextView add_post_user_name;
    @BindView(R.id.add_post_text)
    EditText add_post_text;
    @BindView(R.id.add_post_media)
    LinearLayout add_post_media;

    List<Image> images;

    // Get a RequestQueue
    RequestQueue queue;

    private static final String TAG = "AddPostActivity";
    public static int ADD_POST_Code = 1;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    User user;
    Randonnee randonnee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
        randonnee = (Randonnee) getIntent().getExtras().get("randonnee");

        setContentView(R.layout.activity_add_post);
        ButterKnife.bind(this);

        Picasso.with(this).load(user.getPhoto()).into(add_post_user_img);
        add_post_user_name.setText(user.getName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("In " + randonnee.getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_post_gallery);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AddPostActivity.this, AlbumSelectActivity.class);
                //set limit on number of images that can be selected, default is 10
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 5);
                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });
    }

    public ImageView newImage(Image image, int width, int height, int weight) {

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);

        File file = new File(image.path);
        Picasso.with(this).load(file).resize(300, 300).centerCrop().into(imageView);

        return imageView;
    }

    public LinearLayout newHorizontalLinearLayout(int width, int height, int weight, List<Image> images, boolean last) {

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (Image image : images) {

            ImageView imageView = newImage(image, 0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            linearLayout.addView(imageView);
        }

        return linearLayout;
    }

    public LinearLayout newVerticalLinearLayout(int width, int height, int weight, List<Image> images) {

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < images.size(); i++) {

            linearLayout.addView(newImage(images.get(i), LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        }

        return linearLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            add_post_media.removeAllViews();

            //The array list has the image paths of the selected images
            images = new ArrayList<>();
            images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);

            if (images.size() > 3) {

                add_post_media.setOrientation(LinearLayout.VERTICAL);
            } else {

                add_post_media.setOrientation(LinearLayout.HORIZONTAL);
            }

            switch (images.size()) {

                case 1:
                    add_post_media.addView(newImage(images.get(0), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    break;
                case 2:
                    add_post_media.addView(newImage(images.get(0), 0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    add_post_media.addView(newImage(images.get(1), 0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    break;
                case 3:
                    add_post_media.addView(newImage(images.get(0), 0, LinearLayout.LayoutParams.MATCH_PARENT, 2));
                    List<Image> data1 = images.subList(1, 3);
                    add_post_media.addView(newVerticalLinearLayout(0, LinearLayout.LayoutParams.MATCH_PARENT, 1, data1));
                    break;
                case 4:
                    add_post_media.addView(newImage(images.get(0), LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
                    List<Image> data2 = images.subList(1, 4);
                    add_post_media.addView(newHorizontalLinearLayout(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1, data2, false));
                    break;
                case 5:
                    List<Image> data3 = images.subList(0, 2);
                    add_post_media.addView(newHorizontalLinearLayout(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1, data3, false));
                    List<Image> data4 = images.subList(2, 5);
                    add_post_media.addView(newHorizontalLinearLayout(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1, data4, false));
                    break;
                default:
                    List<Image> data5 = images.subList(0, 2);
                    add_post_media.addView(newHorizontalLinearLayout(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1, data5, false));
                    List<Image> data6 = images.subList(2, 5);
                    add_post_media.addView(newHorizontalLinearLayout(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1, data6, true));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_post) {

            addPost();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addPost() {

        final ProgressDialog progressDialog = new ProgressDialog(AddPostActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing info...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/addPost.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String postId) {

                if(images != null){

                    for (int i=0; i<images.size(); i++) {

                        final Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).path);

                        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/addMedia.php";

                        final int finalI = i;
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                System.out.println(response);

                                if(finalI == (images.size() - 1)){

                                    progressDialog.hide();
                                    finish();
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
                                Map<String, String> params = new Hashtable<>();

                                //Adding parameters
                                params.put("media", getStringImage(bitmap));
                                params.put("post_id", postId);

                                //returning parameters
                                return params;
                            }
                        };

                        stringRequest.setTag(TAG);

                        stringRequest.setRetryPolicy(new RetryPolicy() {
                            @Override
                            public int getCurrentTimeout() {
                                return 60000;
                            }

                            @Override
                            public int getCurrentRetryCount() {
                                return 0;
                            }

                            @Override
                            public void retry(VolleyError error) throws VolleyError {
                                error.printStackTrace();
                            }
                        });

                        // Access the RequestQueue through your singleton class.
                        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    }
                } else {
                    progressDialog.hide();
                    finish();
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
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("text", add_post_text.getText().toString());
                params.put("randonnee_id", String.valueOf(randonnee.getId()));
                params.put("user_id", String.valueOf(user.getId()));

                //returning parameters
                return params;
            }
        };

        stringRequest.setTag(TAG);

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
