package no.nordicsemi.android.nrftoolbox.fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;

/**
 * Created by hj on 2015/11/3.
 */
public abstract class BleProfileFragment extends Fragment
        implements BleManagerCallbacks, ScannerFragment.OnDeviceSelectedListener {

    private static final String TAG = "BaseProfileActivity";

    private static final String CONNECTION_STATUS = "connection_status";
    private static final String DEVICE_NAME = "device_name";
    private static final int REQUEST_ENABLE_BT = 2;

    private BleManager<? extends BleManagerCallbacks> mBleManager;

    private TextView mDeviceNameView;
    private TextView mBatteryLevelView;
    private Button mConnectButton;
    private ILogSession mLogSession;

    private boolean mDeviceConnected = false;
    private String mDeviceName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }

        mBleManager = initializeManager();
        onInitialize(savedInstanceState);

//        onViewCreated(savedInstanceState);

    }



    /**
     *  初始化操作
     * @param savedInstanceState
     */
    protected void onInitialize(final Bundle savedInstanceState) {
        // empty default implementation
    }

    /**
     * 手机是否支持BLE
     */
    private void ensureBLESupported() {
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.no_ble, Toast.LENGTH_LONG).show();
            //finish();
        }
    }
    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    /**
     * Initializes the Bluetooth Low Energy manager. A manager is used to communicate with profile's services.
     *
     * @return the manager that was created
     */
    protected abstract BleManager<? extends BleManagerCallbacks> initializeManager();


    @Override
    public void onDeviceConnected() {

    }

    @Override
    public void onDeviceDisconnecting() {

    }

    @Override
    public void onDeviceDisconnected() {

    }

    @Override
    public void onLinklossOccur() {

    }

    @Override
    public void onServicesDiscovered(boolean optionalServicesFound) {

    }

    @Override
    public void onDeviceReady() {

    }

    @Override
    public void onBatteryValueReceived(int value) {

    }

    @Override
    public void onBondingRequired() {

    }

    @Override
    public void onBonded() {

    }

    @Override
    public void onError(String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported() {

    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {

    }

    @Override
    public void onDialogCanceled() {

    }
}
