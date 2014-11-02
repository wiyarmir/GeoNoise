package es.wiyarmir.geonoise.utils;

import android.location.Location;

/**
* Created by wiyarmir on 02/11/14.
*/
public interface LocationNoiseUpdatesListener {
    public void onLocationNoiseUpdate(Location location, double noise);

    public boolean isInterestedInLocationNoiseUpdates();
}
