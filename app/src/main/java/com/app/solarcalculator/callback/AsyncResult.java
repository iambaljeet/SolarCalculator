package com.app.solarcalculator.callback;

import com.app.solarcalculator.models.Pins;

import java.util.List;

public interface AsyncResult {
    void asyncFinished(List<Pins> pinsList);
}
