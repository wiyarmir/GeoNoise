package es.guillermoorellana.geonoise.utils;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by Guillermo Orellana on 25/08/14.
 */
public class AsyncHeatmapFileLoader extends AsyncTask<String, Void, List<WeightedLatLng>> {

    private final HeatmapCapable target;

    public AsyncHeatmapFileLoader(HeatmapCapable target) {
        this.target = target;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<WeightedLatLng> doInBackground(String... strings) {
        List<WeightedLatLng> ret = new ArrayList<WeightedLatLng>();
        for (String string : strings) {
            try {
                CSVReader reader = new CSVReader(new FileReader(string));
                List<String[]> lines = reader.readAll();
                for (String[] line : lines) {
                    if (line[0].equals("magnitude")) // headers
                        continue;
                    WeightedLatLng item = new WeightedLatLng(new LatLng(Double.parseDouble(line[1]), Double.parseDouble(line[2])), Double.parseDouble(line[0]));
                    ret.add(item);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    protected void onPostExecute(List<WeightedLatLng> weightedLatLngs) {
        super.onPostExecute(weightedLatLngs);
        target.addHeatMap(weightedLatLngs);
    }
}
