package com.app.solarcalculator.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.app.solarcalculator.R;
import com.app.solarcalculator.activity.MapsActivity;
import com.app.solarcalculator.adapter.PinsAdapter;
import com.app.solarcalculator.callback.AlertLocationSelectedCallback;
import com.app.solarcalculator.models.Pins;
import com.app.solarcalculator.service.GoldenHourReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.content.Context.ALARM_SERVICE;

public class Utils {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static Marker addOrMoveMarker(GoogleMap googleMap,
                                       LatLng latLng, Marker marker) {
        if (marker == null) {
            marker = googleMap.addMarker(new MarkerOptions().position(latLng));
        } else {
            marker.setPosition(latLng);
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
        return marker;
    }

    public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                activity.finish();

            return false;
        }
        return true;
    }

    public static boolean hasPermission(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static void scheduleAlarm(Activity activity, long timeMillis) {
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(activity, GoldenHourReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeMillis,
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeMillis,
                    pendingIntent);
        }
    }

    public static void showShortToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static String getCurrentDate(long millis, Calendar calendar) {
        return getFormattedDate(millis, "EEEE, MMMM dd, yyyy", calendar);
    }

    public static String getFormattedDate(long milliSeconds, String dateFormat, Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getFormattedDate(Date date, String dateFormat, Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        calendar.setTime(date);
        return formatter.format(calendar.getTime());
    }

    public static long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

    public static long getPreviousDay(long millis, Calendar calendar) {
        calendar.setTimeInMillis(millis);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTimeInMillis();
    }

    public static long getNextDay(long millis, Calendar calendar) {
        calendar.setTimeInMillis(millis);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    public static void customPinsDialog(LifecycleOwner lifecycleOwner, final Activity context,
                                        MutableLiveData<List<Pins>> pinsArrayList,
                                        final AlertLocationSelectedCallback alertLocationSelectedCallback) {

        final List<Pins>[] pinsList = new List[]{new ArrayList<>()};

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.my_pins_dialog, null);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        AppCompatButton button_close = dialogView.findViewById(R.id.button_close);
        RecyclerView all_pins_recycler_view = dialogView.findViewById(R.id.all_pins_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        PinsAdapter pinsAdapter = new PinsAdapter(context, pinsList[0], alertLocationSelectedCallback, alertDialog);
//
        all_pins_recycler_view.setLayoutManager(linearLayoutManager);
        all_pins_recycler_view.setAdapter(pinsAdapter);

        pinsArrayList.observe(lifecycleOwner, new Observer<List<Pins>>() {
            @Override
            public void onChanged(@Nullable List<Pins> pins) {
                pinsAdapter.newDataInsserted(pins);
            }
        });

        button_close.setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_dialog_bg));
        }

        alertDialog.show();
    }
}