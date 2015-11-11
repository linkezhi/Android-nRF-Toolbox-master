package no.nordicsemi.android.nrftoolbox.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.listener.SaveListener;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.bean.UserData;
import no.nordicsemi.android.nrftoolbox.hts.HTSManager;
import no.nordicsemi.android.nrftoolbox.hts.HTSService;
import no.nordicsemi.android.nrftoolbox.hts.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyFragment;
import no.nordicsemi.android.nrftoolbox.widget.MyMarkerView;

/**
 * Created by wowzhuo on 2015/11/2.
 */
public class HtsFragment extends BleProfileServiceReadyFragment<HTSService.RSCBinder> implements OnChartValueSelectedListener {


    private final String TAG = "HTSActivity";

    private static final String VALUE = "value";
    private static final DecimalFormat mFormattedTemp = new DecimalFormat("#0.00");
    private TextView mHTSValue;
    private TextView mHTSUnit;
    private Double mValueC;
    private LineChart mChart;
    private ArrayList<String> datas = new ArrayList<>(12);
    //保存温度值
    private List<Double> hrsDatas = new ArrayList();
    private LineData lineData;

    private String phomeNumber;
    private SharedPreferences mSettings = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_feature_hts,null);

        mHTSValue = (TextView) mView.findViewById(R.id.text_hts_value);
        mHTSUnit = (TextView) mView.findViewById(R.id.text_hts_unit);
        mChart = (LineChart) mView.findViewById(R.id.chart); //获取控件
        if (mValueC != null)
            mHTSValue.setText(String.valueOf(mValueC));
        return mView;

    }

    //	横竖屏切换时保存activity的温度值
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mValueC != null)
            outState.putDouble(VALUE, mValueC);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSettings = this.getActivity().getSharedPreferences("prefsname",Context.MODE_PRIVATE);
        phomeNumber = mSettings.getString("userid", "");
        initChart();
    }

    //接受广播设置mValuec的值
    @Override
    protected void onInitialize(final Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, makeIntentFilter());

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(VALUE))
                mValueC = savedInstanceState.getDouble(VALUE);

        }
    }

    @Override
    protected void onServiceBinded(HTSService.RSCBinder binder) {

    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.hts_feature_title;
    }



    @Override
    protected void onServiceUnbinded() {

    }

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return HTSService.class;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.hts_default_name;
    }

    @Override
    protected void setDefaultUI() {
        mValueC = null;
        mHTSValue.setText(R.string.not_available_value);

        setUnits();
    }

    @Override
    protected UUID getFilterUUID() {
        return HTSManager.HT_SERVICE_UUID;
    }

    @Override
    public void onServicesDiscovered(boolean optionalServicesFound) {

    }


    /**
     * 介绍温度数据
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (HTSService.BROADCAST_HTS_MEASUREMENT.equals(action)) {
                final double value = intent.getDoubleExtra(HTSService.EXTRA_TEMPERATURE, 0.0f);
                // Update GUI
                setHTSValueOnView(value);
                addEntrys((float) value);

                //saveData("13929549205",value);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        setUnits();
    }


    //设置单位
    private void setUnits() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

        switch (unit) {
            case SettingsFragment.SETTINGS_UNIT_C:
                mHTSUnit.setText(R.string.hts_unit_celsius);
                break;
            case SettingsFragment.SETTINGS_UNIT_F:
                mHTSUnit.setText(R.string.hts_unit_fahrenheit);
                break;
            case SettingsFragment.SETTINGS_UNIT_K:
                mHTSUnit.setText(R.string.hts_unit_kelvin);
                break;
        }
        if (mValueC != null)
            setHTSValueOnView(mValueC);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HTSService.BROADCAST_HTS_MEASUREMENT);
        return intentFilter;
    }




    private void setHTSValueOnView(double value) {
        mValueC = value;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

        switch (unit) {
            case SettingsFragment.SETTINGS_UNIT_F:
                value = value * 1.8 + 32;
                break;
            case SettingsFragment.SETTINGS_UNIT_K:
                value += 273.15;
                break;
            case SettingsFragment.SETTINGS_UNIT_C:
                break;
        }
        mHTSValue.setText(mFormattedTemp.format(value));

    }


    public void connectDevices(){
        onConnectClicked(null);
    }

    private void initChart() {

        for (int i = 0; i < 12; i++) {
            datas.add(i*5+"");
        }

        lineData = new LineData(datas);

        mChart.setDescription("Temperature");            //
        mChart.setDescriptionPosition(0, 0);
        mChart.setDrawGridBackground(true);// 是否显示表格颜色
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDoubleTapToZoomEnabled(false);

        mChart.setAutoScaleMinMaxEnabled(false);

        // enable touch gestures
//        mChart.setTouchEnabled(true);

        // enable scaling and dragging
//        mChart.setDragEnabled(true);
//        mChart.setScaleEnabled(true);
        mChart.setScaleEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
//        mChart.setPinchZoom(true);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);

        // set the marker to the chart
        mChart.setMarkerView(mv);

        XAxis xl = mChart.getXAxis();
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawGridLines(true);
        xl.setSpaceBetweenLabels(1);
        xl.setEnabled(true);
        xl.setAxisLineColor(Color.rgb(0xb7, 0xd7, 0xbf));//B7D7BF
        xl.setAxisLineWidth(2);
        xl.setLabelsToSkip(0);


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.setAxisMaxValue(38.2f);
        leftAxis.setAxisMinValue(34.0f);
        leftAxis.setLabelCount(12, false);
        leftAxis.setAxisLineColor(Color.rgb(0xb7, 0xd7, 0xbf));
        leftAxis.setAxisLineWidth(2);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.setData(new LineData(datas));

        Legend l = mChart.getLegend();      //描述

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        // dont forget to refresh the drawing
        mChart.invalidate();
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null,"℃");

        set.setLineWidth(1.5f);
        set.setCircleSize(4f);
        set.setDrawFilled(true);

        set.setDrawCircleHole(true);
        set.setCircleColor(Color.rgb(0xf0, 0x70, 0x39));//圆点颜色

        set.setColor(Color.argb(255, 0xed, 0x73, 0x37));   //连接线的颜色
        set.setFillColor(Color.argb(255, 0xf4, 0xc7, 0xa5));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    private void addEntrys(float value) {

        LineData data = mChart.getData();
        if (data != null){
            LineDataSet set = data.getDataSetByIndex(0);    //获得第一条线的数据集
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //采集到12个数据，则清空
            if (set.getEntryCount()>=12)
            {
                float Average = set.getAverage();
                double d = Double.parseDouble(String.valueOf(Average)) ;
                Log.v("User_data",""+d);
                saveData(phomeNumber, d);//上传数据
                set.clear();
            }
            set.setDrawFilled(true);
            set.setFillColor(Color.argb(255, 0xf4, 0xc7, 0xa5));
            Entry entry = new Entry(value,set.getEntryCount());
            data.addEntry(entry,0);
        }

        mChart.notifyDataSetChanged();
        mChart.setVisibleXRangeMaximum(12);
        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    /**
     * 上传用户体温值
     * @param phoneNumber
     * @param data
     */
    public void saveData (String phoneNumber,  Double data) {

        /*Double heart_rate = 0.0;
        Double j =0.0;
        hrsDatas.add(data);
        if( hrsDatas.size() >=12){
            for (int i=0;i<12;i++){
                j += hrsDatas.get(i);
                heart_rate = j/12;
            }*/
            UserData userScore = new UserData();
            userScore.setUser(phoneNumber);
            userScore.setHeart_rate(data);
//            final Double finalHeart_rate = data;
            userScore.save(getActivity(), new SaveListener() {
                @Override
                public void onSuccess() {
//                    Log.v("User_data", "heart_rate:" + data);
                    hrsDatas.clear();
                }

                @Override
                public void onFailure(int i, String s) {
                    Log.v("User_data", "heart_rate" + " onFailure " + i + " " + s);
                    hrsDatas.clear();
                }
            });
    }

}
