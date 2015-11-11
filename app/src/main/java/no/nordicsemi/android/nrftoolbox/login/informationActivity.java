package no.nordicsemi.android.nrftoolbox.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import no.nordicsemi.android.nrftoolbox.HrsHtsMonitorActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.bean.UserSms;
import no.nordicsemi.android.nrftoolbox.widget.WheelDialog;
import no.nordicsemi.android.nrftoolbox.widget.WheelView;

public class informationActivity extends AppCompatActivity implements View.OnClickListener /*implements View.OnClickListener*/{

    private Button birth,birthDate;
    private Button userHeight,userWeight;
    public Button commint;
    public ImageButton returnPwd;
    private Calendar calendar;
    public String phoneNumber;
    public String objectIdvalue;
    private  ArrayList<String> list;
    private WheelDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_information);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            phoneNumber = bundle.getString("registerNumber");
        }

        initView();
        initListener();

    }

    private void initListener() {

        birth.setOnClickListener(this);
        birthDate.setOnClickListener(this);
        commint.setOnClickListener(this);
        returnPwd.setOnClickListener(this);
        userHeight.setOnClickListener(this);
        userWeight.setOnClickListener(this);
    }


    public void initView(){
       birth = (Button) findViewById(R.id.birth);
       birthDate = (Button) findViewById(R.id.birthDate);
       commint = (Button) findViewById(R.id.commint);
       returnPwd = (ImageButton) findViewById(R.id.returnRegister);
       userHeight = (Button) findViewById(R.id.height);
       userWeight = (Button) findViewById(R.id.weight);

       calendar = Calendar.getInstance();
       //获取用户的objectId信息
       update();

       commint.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {

           }
       });
       returnPwd.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent=new Intent();
               intent.setClass(informationActivity.this, PwdActivity.class);
               startActivity(intent);
               informationActivity.this.finish();
           }
       });

   }

    /**
     * 获取用户id
     */
    public void update () {
        BmobQuery<UserSms> query = new BmobQuery<UserSms>();
        query.addWhereEqualTo("user", phoneNumber);
        query.findObjects(this, new FindListener<UserSms>() {
            @Override
            public void onSuccess(List<UserSms> list) {
                for (UserSms userScore : list) {
                    //获得数据的objectId信息
                    objectIdvalue = userScore.getObjectId();
                    Log.v("updateInformation", " String objectId ==" + objectIdvalue);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.v("updateInformation", "onError:" + s);
            }
        });
    }

/**
 * 提交用户信息
 */
    public void commintInfor (final String height, final String weight,final String birth){
        //UserSms userScore = new UserSms();
        UserSms userScore = new UserSms();
        userScore.setBirth(birth);
        userScore.setHeight(height);
        userScore.setWeight(weight);

        userScore.update(this, objectIdvalue, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.v("updateInformation", "更新成功：");
                HRSInformation();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("updateInformation", "更新失败：" + s);
            }
        });

    }

    public void  HRSInformation() {
        Intent intent = new Intent();
        intent.setClass(this, HrsHtsMonitorActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.birth:
            case R.id.birthDate:
                showDate();
                break;
            case R.id.commint:
                String heightValue = userHeight.getText().toString();
                String weightValue = userWeight.getText().toString();
               final String birthYearValue = birth.getText().toString();
               final String birthDataValue = birthDate.getText().toString();
               String birthValue = birthYearValue + birthDataValue;
                commintInfor(heightValue, weightValue, birthValue);
                break;
            case R.id.returnRegister:
                finish();
                break;
            case R.id.height:
                list = new ArrayList<>();
                for (int i=140;i<210;i++){
                    list.add(i+"");
                }
                showDialog(list, "厘米");
                break;
            case R.id.weight:
                list = new ArrayList<>();
                for (int i=35;i<100;i++){
                    list.add(i+"");
                }
                showDialog(list,"千克");
                break;

        }
    }



    private void showDate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(informationActivity.this);
        View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        //通过反射机制，访问private的mDaySpinner成员，并隐藏它
        try {
            Field daySpinner = datePicker.getClass().getDeclaredField("mDaySpinner");
            daySpinner.setAccessible(true);
            ((View) daySpinner.get(datePicker)).setVisibility(View.GONE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        datePicker.setCalendarViewShown(false);

        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(view);
        builder.setTitle(R.string.date_picker_dialog_title);
        builder.setPositiveButton(R.string.date_picker_button_right, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuffer sb = new StringBuffer();
                StringBuffer MOUTH = new StringBuffer();
                sb.append(String.format("%d年",
                        datePicker.getYear()
                ));
                MOUTH.append(String.format("%02d月",

                        datePicker.getMonth() + 1
                ));

                birth.setText(sb);
                birthDate.setText(MOUTH);

                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.date_picker_button_cancel, new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    public void showDialog(final ArrayList<String> list, final String ext){
        dialog = new WheelDialog(this,list,ext);

        dialog.setOnDateSetListener(new WheelDialog.OnDateSetListener() {
            @Override
            public void onDateSet(WheelView view, int index) {
                 if (ext.equals("厘米")){
                    userHeight.setText(list.get(index)+"厘米");
                 }else if (ext.equals("千克")){
                     userWeight.setText(list.get(index) + "千克");
                 }
            }
        });

//        Window window = dialog.getWindow();
//        window.setWindowAnimations(R.style.mystyle);  //添加动画
        dialog.show();
    }
}

