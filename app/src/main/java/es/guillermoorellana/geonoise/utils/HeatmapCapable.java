package es.guillermoorellana.geonoise.utils;

import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.List;

/**
 * Created by Guillermo Orellana on 25/08/14.
 */
public interface HeatmapCapable {
    void addHeatMap(List<WeightedLatLng> weightedLatLngs);
}
