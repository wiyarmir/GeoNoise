package es.guillermoorellana.geonoise.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.List;
import java.util.Random;

import es.guillermoorellana.geonoise.R;
import es.guillermoorellana.geonoise.utils.HeatmapCapable;
import es.guillermoorellana.geonoise.utils.LocationNoiseUpdatesListener;
import es.guillermoorellana.geonoise.utils.Utils;

public class MapsFragment extends Fragment implements HeatmapCapable, LocationNoiseUpdatesListener {


    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = Utils.helix_index;
    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;
    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = Utils.cubehelix();
    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);
    private static final String TAG = "MAPFRAGMENT";
    private static View view = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private FilePickerDialogFragment fpdf;
    private Random random = new Random();


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) { // already have a view
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) { // it has a parent, de attach it
                parent.removeView(view);
            }
        }
        try {
            view = inflater.inflate(R.layout.activity_maps, container, false);
        } catch (InflateException e) {
            //map is somehow already there
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.load:
                fpdf = new FilePickerDialogFragment(this);
                fpdf.show(getFragmentManager(), "filedialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                Log.d("TAG", '"' + Double.toString(65d + random.nextDouble() * 15d) + "\",\"" + point.latitude + "\",\"" + point.longitude + '"');
            }
        });
    }

    public void addHeatMap(List<WeightedLatLng> list) {

        if (list.isEmpty()) {
            Toast.makeText(getActivity(), "Empty!", Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            if (!defaultSharedPreferences
                    .getString("map_cubehelix", "0").equals("0")) {
                mProvider = new HeatmapTileProvider.Builder()
                        .opacity(Double.parseDouble(defaultSharedPreferences.getString("map_transparency", ".5")))
                        .radius(Integer.parseInt(defaultSharedPreferences.getString("map_convolution", "30")))
                        .weightedData(list)
                        .gradient(ALT_HEATMAP_GRADIENT)
                        .build();
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            } else {
                mProvider = new HeatmapTileProvider.Builder()
                        .opacity(Double.parseDouble(defaultSharedPreferences.getString("map_transparency", ".5")))
                        .radius(Integer.parseInt(defaultSharedPreferences.getString("map_convolution", "30")))
                        .weightedData(list)
                        .build();
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(list.get(0).getPoint().y, list.get(0).getPoint().x), 14.0f));
        }
    }

    @Override
    public void onLocationNoiseUpdate(Location location, double noise) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
    }

    @Override
    public boolean isInterestedInLocationNoiseUpdates() {
        return true;
    }
}