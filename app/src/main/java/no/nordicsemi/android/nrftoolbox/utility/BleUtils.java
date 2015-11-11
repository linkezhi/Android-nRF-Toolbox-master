package no.nordicsemi.android.nrftoolbox.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.BleApplication;
import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by wowzhuo on 2015/11/4.
 */
public class BleUtils {

    public static void ensureBLESupported() {
        if (!BleApplication.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(BleApplication.getInstance(), R.string.no_ble, Toast.LENGTH_LONG).show();
            BleApplication.getInstance().finish();
        }
    }

    public static boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager)  BleApplication.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }


}
