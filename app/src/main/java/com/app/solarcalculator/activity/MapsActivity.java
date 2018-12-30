package com.app.solarcalculator.activity;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.solarcalculator.R;
import com.app.solarcalculator.callback.AlertLocationSelectedCallback;
import com.app.solarcalculator.models.Pins;
import com.app.solarcalculator.suncalc.MoonTimes;
import com.app.solarcalculator.suncalc.SunTimes;
import com.app.solarcalculator.utils.PinsRepo;
import com.app.solarcalculator.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    @BindView(R.id.pin_image_button)
    ImageButton pinImageButton;
    @BindView(R.id.saved_pin_image_button)
    ImageButton savedPinImageButton;
    @BindView(R.id.choose_location_text_view)
    AppCompatTextView chooseLocationTextView;
    @BindView(R.id.sun_image_view)
    ImageView sunImageView;
    @BindView(R.id.up_image_view)
    ImageView upImageView;
    @BindView(R.id.up_time_text_view)
    AppCompatTextView upTimeTextView;
    @BindView(R.id.down_time_layout)
    LinearLayout downTimeLayout;
    @BindView(R.id.down_image_view)
    ImageView downImageView;
    @BindView(R.id.down_time_text_view)
    AppCompatTextView downTimeTextView;
    @BindView(R.id.moon_image_view)
    ImageView moonImageView;
    @BindView(R.id.moon_up_image_view)
    ImageView moonUpImageView;
    @BindView(R.id.moon_up_time_text_view)
    AppCompatTextView moonUpTimeTextView;
    @BindView(R.id.moon_down_time_layout)
    LinearLayout moonDownTimeLayout;
    @BindView(R.id.moon_down_image_view)
    ImageView moonDownImageView;
    @BindView(R.id.moon_down_time_text_view)
    AppCompatTextView moonDownTimeTextView;
    @BindView(R.id.previous_image_button)
    ImageButton previousImageButton;
    @BindView(R.id.current_image_button)
    ImageButton currentImageButton;
    @BindView(R.id.next_image_button)
    ImageButton nextImageButton;
    @BindView(R.id.bottom_bar_layout)
    LinearLayout bottomBarLayout;
    @BindView(R.id.my_location_imageview)
    ImageButton myLocationImageView;
    @BindView(R.id.date_text_view)
    AppCompatTextView dateTextView;
    @BindView(R.id.close_image_button)
    ImageButton closeImageButton;

    private String TAG = MapsActivity.class.getSimpleName();
    private String TIME_FORMAT = "hh:MM a";
    private int AUTOCOMPLETE_REQUEST_CODE = 101;
    private int LOCATION_REQUEST_CODE = 200;
    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 2000;

    private long currentTimeInMilliseconds;
    private GoogleMap googleMap;
    private Marker locationmarker;
    private LatLng selectedLatLng;
    private GoogleApiClient googleApiClient;
    private String[] permissions = {"android.permission.ACCESS_FINE_LOCATION"};
    private Calendar calendar;
    private MutableLiveData<SunTimes> sunTimesMutableLiveData;
    private MutableLiveData<MoonTimes> moonTimesMutableLiveData;
    private PinsRepo pinsRepo;
    private ArrayList<Pins> pinsArrayList;
    private LocationRequest locationRequest;

    LifecycleOwner lifecycleOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        lifecycleOwner = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Utils.hasPermission(this, permissions)) {
                requestPermissions(permissions, LOCATION_REQUEST_CODE);
            }
        }

        pinsRepo = new PinsRepo(getApplication());
        calendar = Calendar.getInstance();
        sunTimesMutableLiveData = new MutableLiveData<>();
        moonTimesMutableLiveData = new MutableLiveData<>();
        pinsArrayList = new ArrayList<>();

        currentTimeInMilliseconds = Utils.getCurrentTimeInMillis();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        chooseLocationTextView.setOnClickListener(v -> launchAutocompleteActivity());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (selectedLatLng != null) {
            locationmarker = Utils.addOrMoveMarker(googleMap, selectedLatLng, locationmarker);
        }

        setDateToView(currentTimeInMilliseconds);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Utils.hasPermission(this, permissions)) {
                connectClient();
            }
        } else {
            connectClient();
        }
    }

    @SuppressLint("MissingPermission")
    @OnClick({R.id.pin_image_button, R.id.saved_pin_image_button, R.id.previous_image_button,
            R.id.current_image_button, R.id.next_image_button, R.id.my_location_imageview, R.id.close_image_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pin_image_button:
                savePinToDb();
                break;
            case R.id.saved_pin_image_button:
                openSavedPinsDialog();
                break;
            case R.id.previous_image_button:
                setDateToView(Utils.getPreviousDay(currentTimeInMilliseconds, calendar));
                break;
            case R.id.current_image_button:
                setDateToView(Utils.getCurrentTimeInMillis());
                break;
            case R.id.next_image_button:
                setDateToView(Utils.getNextDay(currentTimeInMilliseconds, calendar));
                break;
            case R.id.my_location_imageview:
                currentLocationClicked();
                break;
            case R.id.close_image_button:
                finish();
                break;
        }
    }

    private void currentLocationClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Utils.hasPermission(this, permissions)) {
                if (googleApiClient != null && googleApiClient.isConnected()) {
                    startLocationUpdates();
                } else {
                    connectClient();
                }
            }
        } else {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                startLocationUpdates();
            } else {
                connectClient();
            }
        }
    }

    private void launchAutocompleteActivity() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                if (googleMap != null) {
                    selectedLatLng = place.getLatLng();
                    if (selectedLatLng != null) {
                        locationmarker = Utils.addOrMoveMarker(googleMap, selectedLatLng, locationmarker);
                        changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (LOCATION_REQUEST_CODE) {
            case 200:
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (locationAccepted) {
                    connectClient();
                }
                break;
        }
    }

    public void connectClient() {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        } else {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            connectClient();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Utils.checkPlayServices(this)) {
            Utils.showShortToast(this, "Please install google play services");
        }
    }

    private void openSavedPinsDialog() {
        Utils.customPinsDialog(lifecycleOwner, MapsActivity.this, pinsRepo.getAllPins(), new AlertLocationSelectedCallback() {
            @Override
            public void locationSelected(LatLng latLng) {
                selectedLatLng = latLng;
                locationmarker = Utils.addOrMoveMarker(googleMap, selectedLatLng, locationmarker);
                changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
            }
        });
    }

    private void savePinToDb() {
        if (selectedLatLng != null) {
            Pins pins = new Pins();
            pins.latitude = selectedLatLng.latitude;
            pins.longitude = selectedLatLng.longitude;
            pinsRepo.insertPin(pins);
        } else {
            Utils.showShortToast(this, "Please select a location first to continue.");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Utils.hasPermission(this, permissions)) {
                startLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (Utils.hasPermission(this, permissions)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private void setDateToView(long currentTimeInMilliseconds) {
        this.currentTimeInMilliseconds = currentTimeInMilliseconds;
        dateTextView.setText(Utils.getCurrentDate(currentTimeInMilliseconds, calendar));

        if (selectedLatLng != null) {
            changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        locationmarker = Utils.addOrMoveMarker(googleMap, myLatLng, locationmarker);
        selectedLatLng = myLatLng;
        changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
    }

    private void changeRiseSetTime(LatLng latLng, long millis) {
        Date date = new Date(millis);
        SunTimes sunTimes = SunTimes.compute().on(date).at(latLng.latitude, latLng.longitude).execute();
        MoonTimes moonTimes = MoonTimes.compute().on(date).at(latLng.latitude, latLng.longitude).execute();

        sunTimesMutableLiveData.setValue(sunTimes);
        moonTimesMutableLiveData.setValue(moonTimes);

        MutableLiveData<SunTimes> goldenHourSunTime = new MutableLiveData<>();
        SunTimes goldenHourTime = SunTimes.compute().twilight(SunTimes.Twilight.GOLDEN_HOUR)
                .on(date).at(latLng.latitude, latLng.longitude).execute();
        goldenHourSunTime.setValue(goldenHourTime);

        goldenHourSunTime.observe(this, new Observer<SunTimes>() {
            @Override
            public void onChanged(@Nullable SunTimes sunTimes) {
                if (sunTimes != null && sunTimes.getRise() != null) {
                    Utils.scheduleAlarm(MapsActivity.this, sunTimes.getRise().getTime());
                }
                if (sunTimes != null && sunTimes.getSet() != null) {
                    Utils.scheduleAlarm(MapsActivity.this, sunTimes.getSet().getTime());
                }
            }
        });

        sunTimesMutableLiveData.observe(this, new Observer<SunTimes>() {
            @Override
            public void onChanged(@Nullable SunTimes sunTimes) {
                if (sunTimes != null && sunTimes.getRise() != null && sunTimes.getSet() != null) {
                    String sunRiseTime = Utils.getFormattedDate(sunTimes.getRise(), TIME_FORMAT, calendar);
                    String sunSetTime = Utils.getFormattedDate(sunTimes.getSet(), TIME_FORMAT, calendar);
                    upTimeTextView.setText(sunRiseTime);
                    downTimeTextView.setText(sunSetTime);
                }
            }
        });

        moonTimesMutableLiveData.observe(this, new Observer<MoonTimes>() {
            @Override
            public void onChanged(@Nullable MoonTimes moonTimes) {
                if (moonTimes != null && moonTimes.getRise() != null && moonTimes.getSet() != null) {
                    String moonRiseTime = Utils.getFormattedDate(moonTimes.getRise(), TIME_FORMAT, calendar);
                    String moonSetTime = Utils.getFormattedDate(moonTimes.getSet(), TIME_FORMAT, calendar);
                    moonUpTimeTextView.setText(moonRiseTime);
                    moonDownTimeTextView.setText(moonSetTime);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}