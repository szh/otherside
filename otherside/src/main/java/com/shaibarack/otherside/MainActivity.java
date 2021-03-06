package com.shaibarack.otherside;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnCameraChangeListener, GoogleMap.OnMapClickListener, SearchView.OnQueryTextListener {

    private static final String PREFERENCE = "PREFERENCE";
    private static final String FIRST_RUN_PREFERENCE = "first_run";
    private static final String SHOW_ADDRESSES_PREFERENCE = "show_addresses";

    private GoogleMap mMap;
    private TextView mAddress;
    private boolean mShowAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraChangeListener(this);

        mAddress = (TextView) findViewById(R.id.address);

        // Restore "show addresses" preference
        mShowAddresses = getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                .getBoolean(SHOW_ADDRESSES_PREFERENCE, true);

        if (isFirstRun()) {
            showFirstRunDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_addresses).setChecked(mShowAddresses);
        /*MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_flip) {
            LatLng otherSide = flip(mMap.getCameraPosition().target);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(otherSide));
            return true;
        }

        if (id == R.id.action_addresses) {
            // Set the checked state
            item.setChecked(!item.isChecked());
            // Save the preference
            getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                    .edit()
                    .putBoolean(SHOW_ADDRESSES_PREFERENCE, item.isChecked())
                    .commit();
            mShowAddresses = item.isChecked();

            // Now refresh the current location toast, to show or hide the address immediately
            onCameraChange(mMap.getCameraPosition());
        }

        if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.about_message)
                    .setPositiveButton(R.string.alert_ok, null)
                    .create().show();
            return true;
        }

        if (id == R.id.action_help) {
            showFirstRunDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        new ReverseGeocode(this, mAddress, mShowAddresses).execute(cameraPosition.target);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        // Don't care
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO
        return true;
    }

    private static LatLng flip(LatLng target) {
        double flippedLat = -target.latitude;
        double flippedLng = ((target.longitude + 360) % 360) - 180;
        return new LatLng(flippedLat, flippedLng);
    }

    private boolean isFirstRun() {
        // Get old state
        boolean firstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                .getBoolean(FIRST_RUN_PREFERENCE, true);
        // Save new state
        getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                .edit()
                .putBoolean(FIRST_RUN_PREFERENCE, false)
                .commit();
        return firstRun;
    }

    private void showFirstRunDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.first_run_title)
                .setMessage(R.string.first_run_message)
                .setPositiveButton(R.string.alert_ok, null)
                .setIcon(R.drawable.ic_action_flip)
                .create().show();
    }
}
