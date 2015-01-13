package es.guillermoorellana.geonoise.utils;

import android.location.Location;

/**
* Created by Guillermo Orellana on 02/11/14.
*/
public interface LocationNoiseUpdatesListener {
    public void onLocationNoiseUpdate(Location location, double noise);

    public boolean isInterestedInLocationNoiseUpdates();
}
