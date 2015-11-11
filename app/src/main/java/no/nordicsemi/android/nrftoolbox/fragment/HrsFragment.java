package no.nordicsemi.android.nrftoolbox.fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.BleApplication;
import no.nordicsemi.android.nrftoolbox.HrsHtsMonitorActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.hrs.HRSManager;
import no.nordicsemi.android.nrftoolbox.hrs.HRSManagerCallbacks;
//import no.nordicsemi.android.nrftoolbox.hrs.HrsService;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.utility.BleUtils;

/**
 * Created by wowzhuo on 2015/11/4.
 */
public class HrsFragment extends Fragment implements BleManagerCallbacks, ScannerFragment.OnDeviceSelectedListener, HRSManagerCallbacks {

    private HRSManager mBleManager;

//    private HrsService service;
    private TextView mHRSValue;
    private TextView mHRSPosition;

    private boolean mDeviceConnected = false;
    private LogSession mLogSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        BleUtils.ensureBLESupported();
//
//        if (!BleUtils.isBLEEnabled()) {
//            ((HrsHtsMonitorActivity)getActivity()).showBLEDialog();
//        }

        mBleManager = new HRSManager(getActivity());
        mBleManager.setGattCallbacks(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBleManager.disconnect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_feature_hrs_new,null);
        mHRSValue = (TextView) view.findViewById(R.id.text_hrs_value);
        mHRSPosition = (TextView) view.findViewById(R.id.text_hrs_position);
        return view;
    }


    @Override
    public void onDeviceConnected() {
        mDeviceConnected = true;
        //更新UI
    }

    @Override
    public void onDeviceDisconnecting() {

    }

    @Override
    public void onDeviceDisconnected() {
        mDeviceConnected = false;
        mBleManager.close();
        //更新UI
    }

    @Override
    public void onLinklossOccur() {
        mDeviceConnected = false;
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
        Toast.makeText(getActivity(),"The device is now bonded.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported() {

    }

    private boolean isDeviceConnect(){
        return mDeviceConnected;
    }
    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        final int titleId = R.string.hrs_feature_title;
        if (titleId > 0) {
            mLogSession = Logger.newSession(BleApplication.getInstance(), getString(titleId), device.getAddress(), name);
            // If nRF Logger is not installed we may want to use local logger
        }
        mBleManager.setLogger(mLogSession);
        mBleManager.connect(device);
    }

    @Override
    public void onDialogCanceled() {

    }

    @Override
    public void onHRSensorPositionFound(String position) {
        mHRSPosition.setText(position);
    }

    @Override
    public void onHRValueReceived(int value) {
        mHRSValue.setText("" + value);
    }


    public void onConnectClick(){
        if (BleUtils.isBLEEnabled()) {
            if (!mDeviceConnected) {
                showDeviceScanningDialog(HRSManager.HR_SERVICE_UUID);
            } else {
                mBleManager.disconnect();
            }
        } else {
            ((HrsHtsMonitorActivity)getActivity()).showBLEDialog();
        }
    }

    /**
     * Shows the scanner fragment.
     *
     * @param filter
     *            the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
     *            services
     * @see # getFilterUUID()
     */
    private void showDeviceScanningDialog(final UUID filter) {
        final ScannerFragment dialog = ScannerFragment.getInstance(filter);
        dialog.show(getActivity().getSupportFragmentManager(), "scan_fragment");
    }
}
