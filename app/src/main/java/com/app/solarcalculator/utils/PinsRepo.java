package com.app.solarcalculator.utils;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.app.solarcalculator.callback.AsyncResult;
import com.app.solarcalculator.database.PinsDao;
import com.app.solarcalculator.database.PinsDatabse;
import com.app.solarcalculator.models.Pins;

import java.util.List;

public class PinsRepo implements AsyncResult {
    private PinsDao pinsDao;
    private MutableLiveData<List<Pins>> allPinsList;

    public PinsRepo(Application application) {
        PinsDatabse db = PinsDatabse.getDatabase(application);
        pinsDao = db.pinsDao();
        allPinsList = new MutableLiveData<>();
    }

    public void insertPin(Pins pins, Activity activity) {
        new InsertAsyncTask(pinsDao, activity).execute(pins);
    }

    public MutableLiveData<List<Pins>> getAllPins() {
        GetAllPinsAsyncTask getAllPinsAsyncTask = new GetAllPinsAsyncTask(pinsDao);
        getAllPinsAsyncTask.delegate = this;
        getAllPinsAsyncTask.execute();

        return allPinsList;
    }

    @Override
    public void asyncFinished(List<Pins> pinsList) {
        allPinsList.setValue(pinsList);
    }

    private static class InsertAsyncTask extends AsyncTask<Pins, Void, Long> {

        private PinsDao mAsyncTaskDao;
        private Activity mActivity;

        InsertAsyncTask(PinsDao dao,
                        Activity activity) {
            mAsyncTaskDao = dao;
            mActivity = activity;
        }

        @Override
        protected Long doInBackground(final Pins... params) {
            return mAsyncTaskDao.insertPin(params[0])[0];
        }

        @Override
        protected void onPostExecute(Long l) {
            super.onPostExecute(l);
            if (mActivity != null) {
                if (l > 0) {
                    Utils.showShortToast(mActivity, "Location saved successfully.");
                } else {
                    Utils.showShortToast(mActivity, "Location not saved, Please try again.");
                }
            }
            Log.d("PinsRepo", "Inserted Value: " + l);
            mActivity = null;
        }
    }

    private static class GetAllPinsAsyncTask extends
            AsyncTask<String, Void, List<Pins>> {

        private PinsDao mAsyncTaskDao;
        private PinsRepo delegate = null;

        GetAllPinsAsyncTask(PinsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<Pins> doInBackground(final String... params) {
            return mAsyncTaskDao.getAllPins();
        }

        @Override
        protected void onPostExecute(List<Pins> result) {
            delegate.asyncFinished(result);
        }
    }
}