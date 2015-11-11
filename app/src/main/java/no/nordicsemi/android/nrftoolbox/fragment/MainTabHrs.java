package no.nordicsemi.android.nrftoolbox.fragment;


import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Random;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.hrs.HRSManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;

/**
 * Created by hj on 2015/11/2.
 */
public class MainTabHrs extends Fragment implements BleManagerCallbacks,
        ScannerFragment.OnDeviceSelectedListener,HRSManagerCallbacks
{
    private TextView mHRSValue;
    private TextView mHRSPosition;

    private LineChart chart;
    private LineData chartData;

    private ArrayList<String> datas  = new ArrayList<>();
    private ArrayList<String> xVals;
    private ArrayList<Entry> yVals;

    private LineDataSet dataSet;

    private Random random;//用于产生随机数
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.activity_feature_hrs_new,container, false);

        setGUI(mView);//mpandroidchart线图的初始化
        return  mView;

    }

    /**
     * 线图的初始化
     * @param V
     */
    private void setGUI(View V) {
        chart = (LineChart) V.findViewById(R.id.chart);//初始化控件

        //线形图样式属性设置
        chart.setDescription("");//右底部的图表描述
        chart.setTouchEnabled(false);//禁止滑动交互

        //初始空值数据
        for (int i = 0; i < 12; i++) {
            datas.add(i*5+"");
        }
        chartData = new LineData(datas);
        //x轴样式
        XAxis xl = chart.getXAxis();
        xl.setAvoidFirstLastClipping(true);

        xl.setDrawGridLines(true);//表格内纵轴显示
        xl.setSpaceBetweenLabels(1);//纵轴宽度

        xl.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴的位置
        xl.setAxisLineWidth(2);//x轴宽度
        xl.setLabelsToSkip(0);//x坐标间隔值
        xl.setAxisLineColor(Color.rgb(0xb7, 0xd7, 0xbf));//B7D7BF

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);//隐藏右边的y轴

        YAxis yl = chart.getAxisLeft();
        //y最大最小值
        yl.setStartAtZero(false);
        yl.setAxisMaxValue(38.2f);
        yl.setAxisMinValue(34.0f);
        yl.setAxisLineWidth(2);
        yl.setAxisLineColor(Color.rgb(0xb7, 0xd7, 0xbf));

        yl.setLabelCount(12,false);

//        data = getLineData(36, 100);//图标横纵坐标数据显示

        chart.setData(chartData); // 设置初始数据

        Legend l = chart.getLegend();      //描述
        l.setForm(Legend.LegendForm.LINE);
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        chart.invalidate();
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

        LineData data = chart.getData();
        if (data != null){
            LineDataSet set = data.getDataSetByIndex(0);//获得第一条线的数据集
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //采集到12个数据，则清空
            if (set.getEntryCount()>=12)
            {
                float Average = set.getAverage();
                double d = Double.parseDouble(String.valueOf(Average)) ;
                Log.v("User_data", "" + d);
//                saveData(phomeNumber, d);//上传数据
                set.clear();
            }
            set.setDrawFilled(true);
            set.setFillColor(Color.argb(255, 0xf4, 0xc7, 0xa5));
            Entry entry = new Entry(value,set.getEntryCount());
            data.addEntry(entry,0);
        }

        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(12);
        chart.invalidate();
    }
    /**
     *生成一个数据
     * @param count 表示图表中有多少个坐标点
     * @param range 用来生成range以内的随机数
     * @return
     */
    private LineData getLineData(int count, int range) {
        // x轴显示的数据，这里默认使用数字下标显示
        xVals = new ArrayList<String>();
        for(int i=0; i<count; i++){
            xVals.add("" + i);
        }

        //y轴数据
        yVals = new ArrayList<Entry>();
        for(int i=0; i<count; i++){
            float value = (float) (Math.random() * range) + 3;
            yVals.add(new Entry(value, i));
        }

        // y轴的数据集合
         dataSet = new LineDataSet(yVals,"");
        ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
        lineDataSets.add(dataSet);
        LineData data = new LineData(xVals, lineDataSets);
        return data;
    }


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

    @Override
    public void onHRSensorPositionFound(String position) {

    }

    @Override
    public void onHRValueReceived(int value) {

    }
}