package com.app.solarcalculator.utils;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

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

    public void insertPin(Pins pins) {
        new InsertAsyncTask(pinsDao).execute(pins);
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

    private static class InsertAsyncTask extends AsyncTask<Pins, Void, Void> {

        private PinsDao mAsyncTaskDao;

        InsertAsyncTask(PinsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Pins... params) {
            mAsyncTaskDao.insertPin(params[0]);
            return null;
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