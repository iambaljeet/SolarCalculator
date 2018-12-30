package com.app.solarcalculator.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.solarcalculator.R;
import com.app.solarcalculator.callback.AlertLocationSelectedCallback;
import com.app.solarcalculator.models.Pins;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PinsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    AlertLocationSelectedCallback alertLocationSelectedCallback;
    List<Pins> pinsList;
    AlertDialog alertDialog;

    public PinsAdapter(Activity context, List<Pins> pinsList,
                       AlertLocationSelectedCallback alertLocationSelectedCallback, AlertDialog alertDialog) {
        this.context = context;
        this.pinsList = pinsList;
        this.alertLocationSelectedCallback = alertLocationSelectedCallback;
        this.alertDialog = alertDialog;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pins_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Pins pins = pinsList.get(position);

        viewHolder.location_name_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(pins.latitude, pins.longitude);
                alertLocationSelectedCallback.locationSelected(latLng);
                alertDialog.dismiss();
            }
        });
        viewHolder.location_name_text_view.setText(String.format("Your Location: %s %s", pins.latitude, pins.longitude));
    }

    public void newDataInsserted(List<Pins> pins) {
        pinsList = pins;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        Log.e("PinsAdapter", "pinsList: " + pinsList.size());
        return pinsList == null ? 0 : pinsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView location_name_text_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            location_name_text_view = itemView.findViewById(R.id.location_name_text_view);
        }
    }

//    @Override
//    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView);
//        if (alertDialog != null) {
//            alertDialog.dismiss();
//            alertDialog = null;
//        }
//    }
}
