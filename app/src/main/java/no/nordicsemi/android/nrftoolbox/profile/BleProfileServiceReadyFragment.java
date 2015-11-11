package no.nordicsemi.android.nrftoolbox.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.UUID;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LocalLogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * Created by pc on 2015/11/1.
 */
public abstract  class BleProfileServiceReadyFragment<E extends BleProfileService.LocalBinder> extends Fragment
    implements ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks{

    protected static final int REQUEST_ENABLE_BT = 2;
    private ILogSession mLogSession;
    private static final String LOG_URI = "log_uri";
    private String mDeviceName;

    protected View mView;

    private E mService;

    private static final String DEVICE_NAME = "device_name";

    private static final String TAG = "BleProfileServiceReadyFragment";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }

        if (savedInstanceState != null) {
            final Uri logUri = savedInstanceState.getParcelable(LOG_URI);
            mLogSession = Logger.openSession(getActivity().getApplicationContext(), logUri);
        }

        onInitialize(savedInstanceState);

        //放在onCreateView中执行
        //TODO
        //final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        //setSupportActionBar(toolbar);
        //setUpView();

    }

    @Override
    public void onStart() {
        super.onStart();
        final Intent service = new Intent(getActivity(), getServiceClass());
        if (getActivity().bindService(service, mServiceConnection, 0)) // we pass 0 as a flag so the service will not be created if not exists
            Logger.d(mLogSession, "Binding to the service..."); // (* - see the comment below)
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCommonBroadcastReceiver, makeIntentFilter());
    }


    @Override
    public void onStop() {
        super.onStop();


        try {
            // We don't want to perform some operations (e.g. disable Battery Level notifications) in the service if we are just rotating the screen.
            // However, when the activity is finishing, we may want to disable some device features to reduce the battery consumption.
            if (mService != null)
                mService.setActivityIsFinishing(getActivity().isFinishing());

            Logger.d(mLogSession, "Unbinding from the service...");
            getActivity().unbindService(mServiceConnection);
            mService = null;

            Logger.d(mLogSession, "Activity unbinded from the service");
            onServiceUnbinded();
            mDeviceName = null;
            mLogSession = null;
        } catch (final IllegalArgumentException e) {
            // do nothing, we were not connected to the sensor
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCommonBroadcastReceiver);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final E bleService = mService = (E) service;
            mLogSession = mService.getLogSession();
            Logger.d(mLogSession, "Activity binded to the service");
            onServiceBinded(bleService);

            // update UI
            //TODO  初始化控件
            mDeviceName = bleService.getDeviceName();

            // and notify user if device is connected
            if (bleService.isConnected())
                onDeviceConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Logger.d(mLogSession, "Activity disconnected from the service");

            mService = null;
            mDeviceName = null;
            mLogSession = null;
            onServiceUnbinded();
        }
    };



    //子类实现
    protected abstract void onServiceBinded(E binder);

    protected abstract void onServiceUnbinded();

    //子类返回
    protected abstract Class<? extends BleProfileService> getServiceClass();

    protected abstract int getDefaultDeviceName();

    protected abstract void setDefaultUI();

    protected abstract UUID getFilterUUID();

    public abstract void onServicesDiscovered(final boolean optionalServicesFound);

    /**
     * Called after the view and the toolbar has been created.
     */
    protected final void setUpView() {
        // set GUI
    }

    protected E getService() {
        return mService;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(DEVICE_NAME, mDeviceName);
        if (mLogSession != null)
            outState.putParcelable(LOG_URI, mLogSession.getSessionUri());
    }


//    @Override
//    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        mDeviceName = savedInstanceState.getString(DEVICE_NAME);
//    }


    /**
     * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
     */
    public void onConnectClicked(final View view) {
        if (isBLEEnabled()) {
            if (mService == null) {
                setDefaultUI();
                showDeviceScanningDialog(getFilterUUID());
            } else {
                Logger.v(mLogSession, "Disconnecting...");
                mService.disconnect();
            }
        } else {
            showBLEDialog();
        }
    }

    private void showDeviceScanningDialog(final UUID filter) {
        final ScannerFragment dialog = ScannerFragment.getInstance(filter);
        dialog.show(getActivity().getSupportFragmentManager(), "scan_fragment");


    }



    protected int getLoggerProfileTitle() {
        return 0;
    }
    protected Uri getLocalAuthorityLogger() {
        return null;
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        final int titleId = getLoggerProfileTitle();
        if (titleId > 0) {
            mLogSession = Logger.newSession(getActivity().getApplicationContext(), getString(titleId), device.getAddress(), name);
            // If nRF Logger is not installed we may want to use local logger
            if (mLogSession == null && getLocalAuthorityLogger() != null) {
                mLogSession = LocalLogSession.newSession(getActivity().getApplicationContext(), getLocalAuthorityLogger(), device.getAddress(), name);
            }
        }

        // The device may not be in the range but the service will try to connect to it if it reach it
        Logger.d(mLogSession, "Creating service...");
        final Intent service = new Intent(getActivity(), getServiceClass());
        service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
        if (mLogSession != null)
            service.putExtra(BleProfileService.EXTRA_LOG_URI, mLogSession.getSessionUri());
        getActivity().startService(service);
        Logger.d(mLogSession, "Binding to the service...");
        getActivity().bindService(service, mServiceConnection, 0);
    }

    @Override
    public void onDialogCanceled() {

    }

    @Override
    public void onDeviceConnected() {
    }

    @Override
    public void onDeviceDisconnecting() {

    }

    @Override
    public void onDeviceDisconnected() {

        try {
            Logger.d(mLogSession, "Unbinding from the service...");
            getActivity().unbindService(mServiceConnection);
            mService = null;

            Logger.d(mLogSession, "Activity unbinded from the service");
            onServiceUnbinded();
            mDeviceName = null;
            mLogSession = null;
        } catch (final IllegalArgumentException e) {
            // do nothing. This should never happen but does...
        }
    }

    @Override
    public void onLinklossOccur() {
    }

    public void onDeviceReady() {
        // empty default implementation
    }

    @Override
    public void onBondingRequired() {

    }

    @Override
    public void onBonded() {

    }

    @Override
    public void onDeviceNotSupported() {
        showToast(R.string.not_supported);
    }


    @Override
    public void onBatteryValueReceived(int value) {
        //收到电量值
    }

    @Override
    public void onError(String message, int errorCode) {
        DebugLogger.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
        showToast(message + " (" + errorCode + ")");
    }

    protected boolean isDeviceConnected() {
        return mService != null;
    }

    protected String getDeviceName() {
        return mDeviceName;
    }

    private final BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BleProfileService.BROADCAST_CONNECTION_STATE: {
                    final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);

                    switch (state) {
                        case BleProfileService.STATE_CONNECTED: {
                            mDeviceName = intent.getStringExtra(BleProfileService.EXTRA_DEVICE_NAME);
                            onDeviceConnected();
                            break;
                        }
                        case BleProfileService.STATE_DISCONNECTED: {
                            onDeviceDisconnected();
                            mDeviceName = null;
                            break;
                        }
                        case BleProfileService.STATE_LINK_LOSS: {
                            onLinklossOccur();
                            break;
                        }
                        case BleProfileService.STATE_CONNECTING:
                        case BleProfileService.STATE_DISCONNECTING:
                            // current implementation does nothing in this states
                        default:
                            // there should be no other actions
                            break;
                    }
                    break;
                }
                case BleProfileService.BROADCAST_SERVICES_DISCOVERED: {
                    final boolean primaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_PRIMARY, false);
                    final boolean secondaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_SECONDARY, false);

                    if (primaryService) {
                        onServicesDiscovered(secondaryService);
                    } else {
                        onDeviceNotSupported();
                    }
                    break;
                }
                case BleProfileService.BROADCAST_DEVICE_READY: {
                    onDeviceReady();
                    break;
                }
                case BleProfileService.BROADCAST_BOND_STATE: {
                    final int state = intent.getIntExtra(BleProfileService.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                    switch (state) {
                        case BluetoothDevice.BOND_BONDING:
                            onBondingRequired();
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            onBonded();
                            break;
                    }
                    break;
                }
                case BleProfileService.BROADCAST_BATTERY_LEVEL: {
                    final int value = intent.getIntExtra(BleProfileService.EXTRA_BATTERY_LEVEL, -1);
                    if (value > 0)
                        onBatteryValueReceived(value);
                    break;
                }
                case BleProfileService.BROADCAST_ERROR: {
                    final String message = intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE);
                    final int errorCode = intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0);
                    onError(message, errorCode);
                    break;
                }
            }
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
        intentFilter.addAction(BleProfileService.BROADCAST_SERVICES_DISCOVERED);
        intentFilter.addAction(BleProfileService.BROADCAST_DEVICE_READY);
        intentFilter.addAction(BleProfileService.BROADCAST_BOND_STATE);
        intentFilter.addAction(BleProfileService.BROADCAST_BATTERY_LEVEL);
        intentFilter.addAction(BleProfileService.BROADCAST_ERROR);
        return intentFilter;
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


    protected void showToast(final int messageResId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), messageResId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void showToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
