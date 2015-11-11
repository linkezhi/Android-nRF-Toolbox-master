package no.nordicsemi.android.nrftoolbox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.fragment.HrsFragment;
import no.nordicsemi.android.nrftoolbox.fragment.HtsFragment;
import no.nordicsemi.android.nrftoolbox.fragment.MainTabHrs;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;


public class HrsHtsMonitorActivity extends FragmentActivity implements View.OnClickListener,ScannerFragment.OnDeviceSelectedListener {

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    protected static final int REQUEST_ENABLE_BT = 2;
    //底部按钮
    private RadioGroup rg_music_fragment;
    private RadioButton rb_artist, rb_album;

    //菜单栏
    private ImageButton setMune,returnRegister;
    private HtsFragment htsFragment;
    private HrsFragment hrsFragment;

    private PopupWindow popupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrs_hts_monitor);

        initView();
        iniViewPager();

    }

    /**
     * 初始化ViewPager
     */
    private void iniViewPager() {

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()){
            @Override
            public int getCount() {
                return mFragments.size();
            }
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                return mFragments.get(position);
            }
        };

        mViewPager.setAdapter(mAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int currentIndex;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                resetTabBtn();//重置底部RadioGroup按钮
                switch (position) {
                    case 0:
                        rb_artist.setChecked(true);
                        break;
                    case 1:
                        rb_album.setChecked(true);
                        break;
                }
                currentIndex = position;
                Log.v("onPageSelected", " " + position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 按钮重置
     */
    protected void resetTabBtn(){
        rb_album.setChecked(false);
        rb_artist.setChecked(false);
    }

    private void initView(){

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        rg_music_fragment= (RadioGroup) findViewById(R.id.rg_music_fragment);
        rb_album = (RadioButton) findViewById(R.id.rb_album);
        rb_artist = (RadioButton) findViewById(R.id.rb_artist);
        setMune = (ImageButton) findViewById(R.id.ib_more);
        returnRegister = (ImageButton) findViewById(R.id.returnRegister);
        setMune.setOnClickListener(this);
        //添加viewPage的fragment
        htsFragment = new HtsFragment();
        MainTabHrs  hrsFragment = new MainTabHrs();
        mFragments.add(htsFragment);
        mFragments.add(hrsFragment);

        //fragmentTab的切换
        rg_music_fragment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rb_album.getId()) {
                    mViewPager.setCurrentItem(1);
                } else {
                    mViewPager.setCurrentItem(0);
                }
            }
        });


        returnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HrsHtsMonitorActivity.this.finish();
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_more:
                showMenu(v);
                break;
            case R.id.menu_connect:
                Toast.makeText(this,"connect",Toast.LENGTH_SHORT).show();
                HtsFragment fragment = (HtsFragment) mFragments.get(0);
                fragment.connectDevices();
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
                break;
            case R.id.menu_account:
                Toast.makeText(this, "account", Toast.LENGTH_SHORT).show();
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
                break;
            case R.id.menu_exit:
                Toast.makeText(this,"exit",Toast.LENGTH_SHORT).show();
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
                break;
            case R.id.menu_history:
                Toast.makeText(this,"history",Toast.LENGTH_SHORT).show();
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
                break;
        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        HtsFragment fragment = (HtsFragment) mFragments.get(0);
        fragment.onDeviceSelected(device,name);
    }

    @Override
    public void onDialogCanceled() {

    }

    /**
     * 弹出菜单
     * @param parent
     */
    private void showMenu(View parent) {
        if (popupWindow == null){
            View view = LayoutInflater.from(this).inflate(R.layout.menu_layout,null);
            initMenu(view);//菜单控件的监听
            popupWindow = new PopupWindow(view,300,350);//弹出框的宽高
        }

        popupWindow.setFocusable(true);         //获取焦点
        popupWindow.setOutsideTouchable(true);  //外部点击隐藏菜单
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        int[] location = new int[2];
        parent.getLocationOnScreen(location);   //获取点击位置

        popupWindow.showAsDropDown(parent,-220,0); //弹出框的位置

    }

    /**
     *  获取菜单控件并设置监听事件
     * @param view
     */
    private void initMenu(View view) {
        view.findViewById(R.id.menu_connect).setOnClickListener(this);
        view.findViewById(R.id.menu_account).setOnClickListener(this);
        view.findViewById(R.id.menu_exit).setOnClickListener(this);
        view.findViewById(R.id.menu_history).setOnClickListener(this);
    }

    public void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

}
