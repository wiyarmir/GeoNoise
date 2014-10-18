package es.wiyarmir.geonoise.utils;

import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.List;

/**
 * Created by wiyarmir on 25/08/14.
 */
public interface HeatmapCapable {
    void addHeatMap(List<WeightedLatLng> weightedLatLngs);
}
