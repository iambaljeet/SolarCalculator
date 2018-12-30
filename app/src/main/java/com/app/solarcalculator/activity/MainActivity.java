package com.app.solarcalculator.activity;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.app.solarcalculator.R;
import com.app.solarcalculator.callback.AlertLocationSelectedCallback;
import com.app.solarcalculator.callback.LocationSettingsCallback;
import com.app.solarcalculator.models.Pins;
import com.app.solarcalculator.utils.PinsRepo;
import com.app.solarcalculator.utils.Utils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.solarcalculator.utils.Utils.REQUEST_CHECK_SETTINGS;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int AUTOCOMPLETE_REQUEST_CODE = 101;
    @BindView(R.id.pin_image_button)
    AppCompatImageButton pinImageButton;
    @BindView(R.id.saved_pin_image_button)
    AppCompatImageButton savedPinImageButton;
    @BindView(R.id.choose_location_text_view)
    AppCompatTextView chooseLocationTextView;
    @BindView(R.id.sun_image_view)
    AppCompatImageView sunAppCompatImageView;
    @BindView(R.id.up_image_view)
    AppCompatImageView upImageView;
    @BindView(R.id.up_time_text_view)
    AppCompatTextView upTimeTextView;
    @BindView(R.id.down_time_layout)
    LinearLayout downTimeLayout;
    @BindView(R.id.down_image_view)
    AppCompatImageView downImageView;
    @BindView(R.id.down_time_text_view)
    AppCompatTextView downTimeTextView;
    @BindView(R.id.moon_image_view)
    AppCompatImageView moonImageView;
    @BindView(R.id.moon_up_image_view)
    AppCompatImageView moonUpImageView;
    @BindView(R.id.moon_up_time_text_view)
    AppCompatTextView moonUpTimeTextView;
    @BindView(R.id.moon_down_time_layout)
    LinearLayout moonDownTimeLayout;
    @BindView(R.id.moon_down_image_view)
    AppCompatImageView moonDownImageView;
    @BindView(R.id.moon_down_time_text_view)
    AppCompatTextView moonDownTimeTextView;
    @BindView(R.id.previous_image_button)
    AppCompatImageButton previousImageButton;
    @BindView(R.id.current_image_button)
    AppCompatImageButton currentImageButton;
    @BindView(R.id.next_image_button)
    AppCompatImageButton nextImageButton;
    @BindView(R.id.bottom_bar_layout)
    LinearLayout bottomBarLayout;
    @BindView(R.id.my_location_imageview)
    AppCompatImageButton myLocationImageView;
    @BindView(R.id.date_text_view)
    AppCompatTextView dateTextView;
    @BindView(R.id.close_image_button)
    AppCompatImageButton closeAppCompatImageButton;
    LifecycleOwner lifecycleOwner;
    LocationCallback locationCallback;
    private String TAG = MainActivity.class.getSimpleName();
    private String TIME_FORMAT = "hh:MM a";
    private int LOCATION_REQUEST_CODE = 225;
    private long currentTimeInMilliseconds;
    private GoogleMap googleMap;
    private Marker locationmarker;
    private LatLng selectedLatLng;
    private String[] permissions = {"android.permission.ACCESS_FINE_LOCATION"};
    private Calendar calendar;
    private MutableLiveData<SunTimes> sunTimesMutableLiveData;
    private MutableLiveData<MoonTimes> moonTimesMutableLiveData;
    private PinsRepo pinsRepo;
    private ArrayList<Pins> pinsArrayList;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000; /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 2 sec */
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        lifecycleOwner = this;
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //Location callback called whenever user's location changes
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        pinsRepo = new PinsRepo(getApplication());
        calendar = Calendar.getInstance();
        sunTimesMutableLiveData = new MutableLiveData<>();
        moonTimesMutableLiveData = new MutableLiveData<>();
        pinsArrayList = new ArrayList<>();

        //Get current time initially
        currentTimeInMilliseconds = Utils.getCurrentTimeInMillis();

        //Setting up Maps along with Map's  Ready callback
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

        // Sets initial time to view
        setDateToView(currentTimeInMilliseconds);

        //Check and request permissions and GPS
        if (Utils.isRuntimePermissionRequired()) {
            if (Utils.hasPermission(this, permissions)) {
                checkGpsAndSubsribeToLocation();
            } else {
                requestPermissions(permissions, LOCATION_REQUEST_CODE);
            }
        } else {
            checkGpsAndSubsribeToLocation();
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
                //Goes a day before current/selected day
                setDateToView(Utils.getPreviousDay(currentTimeInMilliseconds, calendar));
                break;
            case R.id.current_image_button:
                //reset back to current day
                setDateToView(Utils.getCurrentTimeInMillis());
                break;
            case R.id.next_image_button:
                //Move a day forward from current day/selected day
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

    //Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    //Stop location updates when requested
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void onLocationChanged(Location location) {
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        locationmarker = Utils.addOrMoveMarker(googleMap, myLatLng, locationmarker);
        selectedLatLng = myLatLng;
        changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
    }

    //Get user's current location on demand
    private void currentLocationClicked() {
        if (Utils.isRuntimePermissionRequired()) {
            if (Utils.hasPermission(this, permissions)) {
                checkGpsAndSubsribeToLocation();
            }
        } else {
            checkGpsAndSubsribeToLocation();
        }
    }

    //Start's Google places autoComplete activity for getting user's location manually
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

    //Handles various results for various tasks like Places autocomplete address,
    //GPS settings
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTOCOMPLETE_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(this, data);

                        if (googleMap != null) {
                            selectedLatLng = place.getLatLng();
                            if (selectedLatLng != null) {
                                locationmarker = Utils.addOrMoveMarker(googleMap, selectedLatLng, locationmarker);
                                changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);

                                //As user has changed his location manually
                                stopLocationUpdates();
                            }
                        }
                        break;
                }
                break;
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        checkGpsAndSubsribeToLocation();
                        break;
                }
                break;
        }
    }

    //Checks runtime permissions result (accepted or not) and perform desired task
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (LOCATION_REQUEST_CODE) {
            case 225:
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (locationAccepted) {
                    checkGpsAndSubsribeToLocation();
                }
                break;
        }
    }

    //Show saved locations list in dialog
    private void openSavedPinsDialog() {
        Utils.customPinsDialog(lifecycleOwner, MainActivity.this, pinsRepo.getAllPins(), new AlertLocationSelectedCallback() {
            @Override
            public void locationSelected(LatLng latLng) {
                selectedLatLng = latLng;
                locationmarker = Utils.addOrMoveMarker(googleMap, selectedLatLng, locationmarker);
                changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
            }
        });
    }

    //Save user's selected location in Database
    private void savePinToDb() {
        if (selectedLatLng != null) {
            Pins pins = new Pins();
            pins.latitude = selectedLatLng.latitude;
            pins.longitude = selectedLatLng.longitude;
            pinsRepo.insertPin(pins, this);
        } else {
            Utils.showShortToast(this, "Please select a location first to continue.");
        }
    }

    private void setDateToView(long currentTimeInMilliseconds) {
        this.currentTimeInMilliseconds = currentTimeInMilliseconds;
        dateTextView.setText(Utils.getCurrentDate(currentTimeInMilliseconds, calendar));

        if (selectedLatLng != null) {
            changeRiseSetTime(selectedLatLng, currentTimeInMilliseconds);
        }
    }

    //Update UI with new SunRise/SunSet, MoonRise/MoonSet time for different lcoations and at different TimeStamps
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
                    Utils.scheduleAlarm(MainActivity.this, sunTimes.getRise().getTime());
                }
                if (sunTimes != null && sunTimes.getSet() != null) {
                    Utils.scheduleAlarm(MainActivity.this, sunTimes.getSet().getTime());
                }
            }
        });

        sunTimesMutableLiveData.observe(this, new Observer<SunTimes>() {
            @Override
            public void onChanged(@Nullable SunTimes sunTimes) {
                if (sunTimes != null) {
                    if (sunTimes.getRise() != null) {
                        String sunRiseTime = Utils.getFormattedDate(sunTimes.getRise(), TIME_FORMAT, calendar);
                        upTimeTextView.setText(sunRiseTime);
                    }
                    if (sunTimes.getSet() != null) {
                        String sunSetTime = Utils.getFormattedDate(sunTimes.getSet(), TIME_FORMAT, calendar);
                        downTimeTextView.setText(sunSetTime);
                    }
                }
            }
        });

        moonTimesMutableLiveData.observe(this, new Observer<MoonTimes>() {
            @Override
            public void onChanged(@Nullable MoonTimes moonTimes) {
                if (moonTimes != null) {
                    if (moonTimes.getRise() != null) {
                        String moonRiseTime = Utils.getFormattedDate(moonTimes.getRise(), TIME_FORMAT, calendar);
                        moonUpTimeTextView.setText(moonRiseTime);
                    }
                    if (moonTimes.getSet() != null) {
                        String moonSetTime = Utils.getFormattedDate(moonTimes.getSet(), TIME_FORMAT, calendar);
                        moonDownTimeTextView.setText(moonSetTime);
                    }
                }
            }
        });
    }

    //Checks and request GPS settings and subscribe to location updates.
    private void checkGpsAndSubsribeToLocation() {
        if (Utils.checkGpsStatus(this)) {
            startLocationUpdates();
        } else {
            Utils.requestGpsSettings(locationRequest, this, new LocationSettingsCallback() {
                @Override
                public void gpsTurnedOn() {
                    startLocationUpdates();
                }

                @Override
                public void gpsTurnedOff(ResolvableApiException resolvable) {
                    try {
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}