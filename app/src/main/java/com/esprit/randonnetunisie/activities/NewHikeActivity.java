package com.esprit.randonnetunisie.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.esprit.randonnetunisie.Constants;
import com.esprit.randonnetunisie.FetchAddressIntentService;
import com.esprit.randonnetunisie.MySingleton;
import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class NewHikeActivity extends AppCompatActivity implements VerticalStepperForm, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "NewHikeActivity";

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    Double latitude = 33.7208328;
    Double longitude = 5.067108;

    User user;

    EditText title;
    EditText desc;
    EditText location;

    TextView date;
    TextView startTime;
    TextView endTime;

    CircleImageView image;

    Switch type;
    EditText availability;
    EditText cost;
    Switch validation;

    Bitmap bitmap;

    private VerticalStepperFormLayout verticalStepperForm;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;

    private boolean mAddressRequested;
    Calendar calendar = Calendar.getInstance();

    private static final int SELECT_IMAGE = 1;

    DatePickerDialog.OnDateSetListener dateDialog = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "yyyy-MM-dd"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

            date.setText(sdf.format(calendar.getTime()));
            checkSecondStep();
        }
    };

    TimePickerDialog.OnTimeSetListener startTimeDialog = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            String myFormat = "HH:mm:ss"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

            startTime.setText(sdf.format(calendar.getTime()));
            checkSecondStep();
        }
    };

    TimePickerDialog.OnTimeSetListener endTimeDialog = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            String myFormat = "HH:mm:ss"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

            endTime.setText(sdf.format(calendar.getTime()));
            checkSecondStep();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hike);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mAddressRequested = false;
        mResultReceiver = new AddressResultReceiver(null);
        getCurrentUser();

        String[] mySteps = {"Basic info", "Time & Date", "Photo", "Details"};
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.primary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.primary_dark);

        // Finding the view
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(R.id.vertical_stepper_form);

        // Setting up and initializing the form
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, mySteps, this, this)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .displayBottomNavigation(true) // It is true by default, so in this case this line is not necessary
                .init();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case 0:
                view = firstStep();
                break;
            case 1:
                view = secondStep();
                break;
            case 2:
                view = thirdStep();
                break;
            case 3:
                view = fourthStep();
                break;
        }
        return view;
    }

    private View firstStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout layoutContent = (LinearLayout) inflater.inflate(R.layout.hike_form_step_1, null, false);

        title = (EditText) layoutContent.findViewById(R.id.form_title);
        desc = (EditText) layoutContent.findViewById(R.id.form_desc);
        location = (EditText) layoutContent.findViewById(R.id.form_location);

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFirstStep();
            }
        });

        desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFirstStep();
            }
        });

        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFirstStep();
                // Only start the service to fetch the address if GoogleApiClient is
                // connected.
                if (mGoogleApiClient.isConnected()) {
                    startIntentService();
                }
                // If GoogleApiClient isn't connected, process the user's request by
                // setting mAddressRequested to true. Later, when GoogleApiClient connects,
                // launch the service to fetch the address. As far as the user is
                // concerned, pressing the Fetch Address button
                // immediately kicks off the process of getting the address.
                mAddressRequested = true;
            }
        });

        return layoutContent;
    }

    private View secondStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout layoutContent = (LinearLayout) inflater.inflate(R.layout.hike_form_step_2, null, false);

        date = (TextView) layoutContent.findViewById(R.id.form_date);
        startTime = (TextView) layoutContent.findViewById(R.id.form_start_time);
        endTime = (TextView) layoutContent.findViewById(R.id.form_end_time);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(NewHikeActivity.this, dateDialog, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new TimePickerDialog(NewHikeActivity.this, startTimeDialog, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new TimePickerDialog(NewHikeActivity.this, endTimeDialog, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        return layoutContent;
    }

    private View thirdStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout layoutContent = (LinearLayout) inflater.inflate(R.layout.hike_form_step_3, null, false);

        image = (CircleImageView) layoutContent.findViewById(R.id.form_img);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
            }
        });

        return layoutContent;
    }

    private View fourthStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout layoutContent = (LinearLayout) inflater.inflate(R.layout.hike_form_step_4, null, false);

        type = (Switch) layoutContent.findViewById(R.id.form_type);
        availability = (EditText) layoutContent.findViewById(R.id.form_availability);
        cost = (EditText) layoutContent.findViewById(R.id.form_cost);
        validation = (Switch) layoutContent.findViewById(R.id.form_validation);

        availability.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFourthStep();
            }
        });

        cost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFourthStep();
            }
        });

        return layoutContent;
    }

    @Override
    public void onStepOpening(int stepNumber) {

        switch (stepNumber) {
            case 0:
                checkFirstStep();
                break;
            case 1:
                checkSecondStep();
                break;
            case 2:
                checkThirdStep();
                break;
            case 3:
                checkFourthStep();
                break;
        }
    }

    private void checkFirstStep() {
        if(title.length() != 0 && desc.length() != 0 && location.length() != 0) {
            verticalStepperForm.setActiveStepAsCompleted();
        } else {
            // This error message is optional (use null if you don't want to display an error message)
            String errorMessage = "Please fill all fields";
            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
        }
    }

    private void checkSecondStep() {
        if(!date.getText().toString().equals("Select date") && !startTime.getText().toString().equals("Select start time") && !endTime.getText().toString().equals("Select end time")) {
            verticalStepperForm.setActiveStepAsCompleted();
        } else {
            // This error message is optional (use null if you don't want to display an error message)
            String errorMessage = "Please fill all fields";
            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
        }
    }

    private void checkThirdStep() {
        if(image.getDrawable() != null) {
            verticalStepperForm.setActiveStepAsCompleted();
        } else {
            // This error message is optional (use null if you don't want to display an error message)
            String errorMessage = "Please choose a picture";
            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
        }
    }

    private void checkFourthStep() {
        if(availability.length() != 0 && cost.length() != 0) {
            verticalStepperForm.setActiveStepAsCompleted();
        } else {
            // This error message is optional (use null if you don't want to display an error message)
            String errorMessage = "Please fill all fields";
            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
        }
    }

    @Override
    public void sendData() {

        final ProgressDialog progressDialog = new ProgressDialog(NewHikeActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing info...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String title = this.title.getText().toString().trim();
        final String location = this.location.getText().toString().trim();
        final String date = this.date.getText().toString();
        final String startTime = this.startTime.getText().toString();
        final String endTime = this.endTime.getText().toString();
        final String desc = this.desc.getText().toString().trim();
        final String type = this.type.isChecked()? "private" : "public";
        final String picture = getStringImage(bitmap);
        final String availability = this.availability.getText().toString().trim();
        final String cost = this.cost.getText().toString().trim();
        final String validation = this.validation.isChecked()? "1" : "0";

        String url = "https://randonnee-tunisie.000webhostapp.com/randonnee/newHike.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.hide();
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
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("title", title);
                params.put("location", location);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("date", date);
                params.put("start_time", startTime);
                params.put("end_time", endTime);
                params.put("description", desc);
                params.put("type", type);
                params.put("picture", picture);
                params.put("availability", availability);
                params.put("cost", cost);
                params.put("validation", validation);
                params.put("user_id", String.valueOf(user.getId()));

                //returning parameters
                return params;
            }
        };
        stringRequest.setTag(TAG);

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Determine whether a Geocoder is available.
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "No geocoder available", Toast.LENGTH_LONG).show();
            return;
        }

        if (mAddressRequested) {
            startIntentService();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location.getText().toString().trim());
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String message = resultData.getString(Constants.RESULT_DATA_KEY);
            final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                longitude = address.getLongitude();
                latitude = address.getLatitude();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    image.setImageBitmap(bitmap);
                    checkThirdStep();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void getCurrentUser(){

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        user = gson.fromJson(json, User.class);
    }
}
