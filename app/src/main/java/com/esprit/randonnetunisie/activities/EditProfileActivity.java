package com.esprit.randonnetunisie.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditProfileActivity extends AppCompatActivity {

    // Get a RequestQueue
    RequestQueue queue;

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE = 2;

    public User user;
    Bitmap bitmap;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    @BindView(R.id.edit_img)
    ImageView img;
    @BindView(R.id.edit_name)
    EditText name;
    @BindView(R.id.edit_email)
    EditText email;
    @BindView(R.id.edit_current_password)
    EditText currentPassword;
    @BindView(R.id.edit_new_password)
    EditText newPassword;
    @BindView(R.id.edit_reenter_password)
    EditText reenterPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);

        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        Picasso.with(this).load(user.getPhoto()).placeholder(R.drawable.user).into(img);
        name.setText(user.getName());
        email.setText(user.getEmail());

        currentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.length() != 0){

                    newPassword.setEnabled(true);
                    reenterPassword.setEnabled(true);

                } else {

                    newPassword.setEnabled(false);
                    reenterPassword.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(bitmap);
            user.setPhoto(getStringImage(bitmap));
        } else if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    img.setImageBitmap(bitmap);
                    user.setPhoto(getStringImage(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_confirm) {

            if (!validate()) {
                Toast.makeText(this, "Please check your info and try again.", Toast.LENGTH_SHORT).show();
                return true;
            }

            user.setName(name.getText().toString().trim());
            user.setEmail(email.getText().toString().trim());

            if (!currentPassword.getText().toString().trim().isEmpty()) {

                if (!validatePasswords()) {
                    Toast.makeText(this, "Please check your passwords and try again.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                user.setPassword(newPassword.getText().toString().trim());
            }

            updateUser(user);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean validate() {
        boolean valid = true;

        String username = name.getText().toString();
        String mail = email.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            name.setError("at least 3 characters");
            valid = false;
        } else {
            name.setError(null);
        }

        if (mail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("enter a valid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        return valid;
    }

    public boolean validatePasswords() {
        boolean valid = true;

        String pwd = newPassword.getText().toString();
        String reEnterPassword = reenterPassword.getText().toString();

        if (pwd.isEmpty() || pwd.length() < 4 || pwd.length() > 10) {
            newPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            newPassword.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(pwd))) {
            reenterPassword.setError("Password Do not match");
            valid = false;
        } else {
            reenterPassword.setError(null);
        }

        return valid;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void editPicture(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void updateUser(final User user) {

        final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/getUserByEmail.php?email=" + email;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Toast.makeText(EditProfileActivity.this, "The email you entered appears to belong to an existing account.\nPlease check your email and try again.", Toast.LENGTH_LONG).show();

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/updateAccount.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();

                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        editor.putString("user", json);
                        editor.commit();
                        finish();
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
                        params.put("id", String.valueOf(user.getId()));
                        params.put("name", user.getName());
                        params.put("email", user.getEmail());
                        params.put("password", user.getPassword());
                        params.put("photo", user.getPhoto());

                        //returning parameters
                        return params;
                    }
                };

                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            }
        });

        jsObjRequest.setTag(TAG);
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
}