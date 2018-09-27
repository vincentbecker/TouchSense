/* This file is part of EmgVisualizer.

    EmgVisualizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EmgVisualizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EmgVisualizer.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import ch.ethz.inf.vs.fingerforce.R;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.MySensorManager;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.MainActivity;
import ch.ethz.inf.vs.fingerforce.emgdata.model.EventBusProvider;

import java.util.ArrayList;

/**
 * Fragment for displaying scanned myos
 * @author Nicola
 */
public class MyoListFragment extends Fragment {

    private static final int REQUEST_ENABLE_ACCESS_FINE_LOCATION = 1235;

    /** TAG for debugging purpose */
    private static final String TAG = "MyoListFragment";

    /** Reference to imagebutton for triggering scan */
    private ImageButton btnScan;
    /** Reference to listview to display found myos */
    private ListView lstMyo;

    /** ArrayList of found Myos */
    private ArrayList<Pair<String, String>> deviceList = new ArrayList<>();
    /** Reference to ArrayAdapter for Myo list */
    private ArrayAdapter adapter;

    /** Scan period in milliseconds */
    private static final long SCAN_PERIOD = 5000;

    /** Reference to Handler for thread execution */
    private Handler mHandler;

    /** Reference to bluetooth adapter */
    private BluetoothAdapter mBluetoothAdapter;
    /** Reference to bluetooth LE scanner for scanning */
    private BluetoothLeScanner mBluetoothLeScanner;
    /** Reference to BT scan callback */
    private ScanMyoListCallback mCallback;

    /** Code to request BT enabled. */
    private int REQUEST_ENABLE_BT = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_myolist, container, false);

        mHandler = new Handler();

        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Activity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mCallback = new ScanMyoListCallback();

        lstMyo = (ListView) view.findViewById(R.id.myolist_found_list);
        btnScan = (ImageButton) view.findViewById(R.id.myolist_scan_button);

        btnScan.setOutlineProvider(new ViewOutlineProvider() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override

            // Set oval button
            public void getOutline(View view, Outline outline) {
                int diameter = getResources().getDimensionPixelSize(R.dimen.diameter);
                outline.setOval(0, 0, diameter, diameter);
            }
        });
        btnScan.setClipToOutline(true);

        // Set button click callback
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocationAccessPermissions();

                // Ask for BT activation.
                if (mBluetoothAdapter.isEnabled()) {
                    if (lstMyo.getCheckedItemPosition() != -1) {
                        Pair selected = (Pair) adapter.getItem(lstMyo.getCheckedItemPosition());
                        MySensorManager.getInstance().setMyo((String) selected.first, (String) selected.second, getActivity());
                        ((MainActivity) getActivity()).changeFragmentMyoControl();
                    } else {
                        scanDevice();
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

            }
        });

        adapter = new MyoArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, deviceList);
        lstMyo.setAdapter(adapter);
        lstMyo.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        // Set list click callback
        lstMyo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lstMyo.setItemChecked(i, true);
                btnScan.setImageResource(R.drawable.ic_checkbox_marked_circle_outline_white_24dp);
                Log.d(TAG, "Selected " + i + " check " + lstMyo.getCheckedItemPosition() + " select " + lstMyo.getSelectedItemPosition());
            }
        });
        return view;
    }

    private void checkLocationAccessPermissions() {
        // Check the location access permissions
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("MAIN_ACTIVITY", "onResume, requesting permission");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ENABLE_ACCESS_FINE_LOCATION);
        } else {
            enableLocation();
        }
    }

    private void enableLocation() {
        //Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        boolean locationEnabled = gps_enabled || network_enabled;
        if (!locationEnabled) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            // Setting Dialog Title
            alertDialog.setTitle(getString(R.string.location_title));
            // Setting Dialog Message
            alertDialog.setMessage(R.string.location_message);
            // On pressing Settings button
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopApp();
                }
            });
            alertDialog.show();
        }
    }

    private void stopApp() {
        Toast.makeText(getActivity(), getString(R.string.no_permission), Toast.LENGTH_LONG).show();
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanDevice();
        }
    }

    /**
     * Method for starting scan for Myos in nearby
     */
    public void scanDevice() {
        try {
            Toast.makeText(getActivity(), MyoListFragment.this.getString(R.string.scan_start), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        resetList();
        // Scanning Time out by Handler.
        // The device scanning needs high energy.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mCallback);
                adapter.notifyDataSetChanged();
                try {
                    Toast.makeText(getActivity(), MyoListFragment.this.getString(R.string.scan_over), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, SCAN_PERIOD);
        mBluetoothLeScanner.startScan(mCallback);
    }

    protected class ScanMyoListCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            String deviceName = device.getName();

            String msg = "name=" + device.getName() + ", bondStatus="
                    + device.getBondState() + ", address="
                    + device.getAddress() + ", type" + device.getType();
            Log.d(TAG, msg);

            Pair<String, String> foundDevice = new Pair<>(deviceName, deviceAddress);
            if (!deviceList.contains(foundDevice)) {
                deviceList.add(foundDevice);
            }
        }
    }

    /**
     * Method for resetting Myo found list
     */
    private void resetList() {
        deviceList.clear();
        btnScan.setImageResource(R.drawable.ic_magnify_white_24dp);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBusProvider.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBusProvider.unregister(this);
    }
}