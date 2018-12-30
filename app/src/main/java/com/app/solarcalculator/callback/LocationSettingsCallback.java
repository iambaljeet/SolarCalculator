package com.app.solarcalculator.callback;

import com.google.android.gms.common.api.ResolvableApiException;

public interface LocationSettingsCallback {
    void gpsTurnedOn();

    void gpsTurnedOff(ResolvableApiException resolvable);
}
