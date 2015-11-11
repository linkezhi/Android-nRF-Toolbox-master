package no.nordicsemi.android.nrftoolbox.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import no.nordicsemi.android.nrftoolbox.HrsHtsMonitorActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.bean.UserSms;
import no.nordicsemi.android.nrftoolbox.utility.TimeCount;

public class PwdActivity extends AppCompatActivity implements View.OnClickListener {

    public EditText code,pwd,res_pwd;
    public Button get_code,register;
    public ImageButton returnRegister;
    public String objectIdvalue;
    public String passwordValue;
    public String phoneNumber;
    public String pwdState;
    public String CODE;
    public String PWD;
    public String RES_PWD;
    private SharedPreferences mSettings = null;
    private TimeCount timeCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_pwd);
        //本地保存用户名密码
        mSettings = getSharedPreferences("prefsname", Context.MODE_PRIVATE); //模式为私有

        intView();

        //获取注册号码
        Bundle bundle = getIntent().getExtras();
        phoneNumber = bundle.getString("registerNumber");
        pwdState =bundle.getString("pwdState");//新号码注册或修改密码

        //修改密码先获取objectId和密码
        upData(phoneNumber);

        EventHandler eh=new EventHandler(){

            @Override
            public void afterEvent(int event, int result, Object data) {

                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }

    private void intView() {
        get_code = (Button) findViewById(R.id.pwd_getcode);
        register = (Button) findViewById(R.id.pwd_register);

        returnRegister = (ImageButton) findViewById(R.id.returnRegister);
        code = (EditText) findViewById(R.id.code);
        pwd = (EditText) findViewById(R.id.pwd_accound);
        res_pwd = (EditText) findViewById(R.id.pwd_res_pwd);

        get_code.setOnClickListener(this);
        register.setOnClickListener(this);
        returnRegister.setOnClickListener(this);

        code.addTextChangedListener(new EditChangedListener());
        pwd.addTextChangedListener(new EditChangedListener());
        res_pwd.addTextChangedListener(new EditChangedListener());
        //验证码倒计时
        timeCount = new TimeCount(60000,1000,this,get_code);
        timeCount.start();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.pwd_register:
                //注册
                CODE = code.getText().toString();
                PWD = pwd.getText().toString();
                RES_PWD = res_pwd.getText().toString();
                /*//登陆
                updataPwd2();
               //修改密码
                updataPwd3(PWD);*/

                if (PWD.equals(RES_PWD)){
                    if(!TextUtils.isEmpty(CODE)){
                        SMSSDK.submitVerificationCode("86", phoneNumber, CODE);
                    }else {
                        Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "密码错误请重新输入", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.pwd_getcode:
                //验证码倒数
                timeCount.start();
                //重新获取验证码
                SMSSDK.getVerificationCode("86", phoneNumber);
                break;
            case R.id.returnRegister:
                PwdActivity.this.finish();
                break;
        }
    }

    /**
     * 跳转信息设置页面
     */
    public void accountInformation() {

        //  本地存储用户账号密码
        saveUserName(phoneNumber,PWD);
        Intent intent = new Intent();
        intent.setClass(PwdActivity.this, informationActivity.class);

        Bundle mBundle = new Bundle();
        mBundle.putString("registerNumber", phoneNumber);//压入数据
        intent.putExtras(mBundle);

        startActivity(intent);
    }

    /**
     * 跳转监控页面
     */
    public void  HRSInformation() {
        Intent intent = new Intent();
        intent.setClass(this, HrsHtsMonitorActivity.class);
        startActivity(intent);
    }

    /**
     * 上传用户手机号，密码
     * @param phoneNumber
     * @param pwd
     */
    public void saveData (final String phoneNumber, final String pwd) {
        UserSms userScore = new UserSms();
        userScore.setUser(phoneNumber);
        userScore.setPwd(pwd);

        userScore.save(getApplicationContext(), new SaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                accountInformation();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * BmodUser注册新用户
     */
    public void  regUser(final String user, final String pwd){
        BmobUser bu = new BmobUser();
        bu.setUsername(user);
        bu.setPassword(pwd);
        bu.signUp(this, new SaveListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_LONG)
                        .show();
                accountInformation();
            }

            @Override
            public void onFailure(int code, String msg) {
                Toast.makeText(getApplicationContext(), "注册失败：" + msg,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 修改用户密码先获取id
     */
    public void upData(final String phoneNumber){
        Log.v("updatapwd", " GetPwd start"+phoneNumber+pwdState);
        //先获取已注册号码的id
        if(pwdState.equals("flase")){
            Log.v("updatapwd", " GetPwd query");
            BmobQuery<UserSms> query = new BmobQuery<UserSms>();
            query.addWhereEqualTo("user", phoneNumber);
            query.findObjects(this, new FindListener<UserSms>() {
                @Override
                public void onSuccess(List<UserSms> list) {
                    for (UserSms userScore : list) {
                        //获得数据的objectId信息
                        objectIdvalue = userScore.getObjectId();
                        passwordValue = userScore.getPwd();
                        Log.v("updatapwd", " GetPwd objectId ==" + objectIdvalue+" passwordValue=="+passwordValue);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.v("updatapwd", "GetPwd msg:" + s+"code:"+i);
                }
            });
        }else {
            Log.v("updatapwd", " GetPwd pwdState==true");
        }
    }

    /**
     *  根据id修改密码
     */
    public void updataPwd(String PWD){
        UserSms userScore = new UserSms();
//        IclothUser userScore = new IclothUser();
        userScore.setPwd(PWD);
        userScore.update(this, objectIdvalue, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.v("updatapwd", "更新成功：");
                HRSInformation();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.v("updatapwd", "更新失败：" + s);
            }
        });
    }

    /**
     * BmodUser登陆
     */
    public void updataPwd2(){
        Log.v("updatapwd", " updataPwd2 user:"+phoneNumber+" pwd:"+passwordValue);
        BmobUser bu2 = new BmobUser();
        bu2.setUsername(phoneNumber);
        bu2.setPassword(passwordValue);
        bu2.login(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.v("updatapwd", "updataPwd2: success");
            }

            @Override
            public void onFailure(int code, String msg) {
                Log.v("updatapwd", "updataPwd2: failure code:" + code + " msg:" + msg);
            }
        });
    }

    /**
     * BmodUser登陆后修改密码
     */
    public void updataPwd3(String PWD){
        BmobUser.updateCurrentUserPassword(getApplicationContext(), passwordValue, PWD, new UpdateListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.v("updatapwd", "updataPwd3，可以用新密码进行登录啦");
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                Log.v("updatapwd", "updataPwd3：" + msg + "(" + code +")");
            }
        });
    }

    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
                   if(pwdState.equals("true")){
                       //注册新账号
                       saveData(phoneNumber,PWD);
                       //BmodUser注册
                       //regUser(phoneNumber,PWD);
                   }else {
                       //修改密码
                       updataPwd( PWD);
                   }


                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                    Log.v("handleMessage", "验证码已经发送");
                }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                ((Throwable) data).printStackTrace();
                /*int resId = getStringRes(RegisterActivity.this, "smssdk_network_error");*/
                Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
                Log.v("register", "错误data:" + data);
                /*if (resId > 0) {v
                    Toast.makeText(RegisterActivity.this, resId, Toast.LENGTH_SHORT).show();
                }*/
            }

        }

    };

    /**
     * 监听输入框的输入，改变UI
     */
    class EditChangedListener implements TextWatcher {
        private boolean DEBUG = true;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (DEBUG) {
                CODE = code.getText().toString();
                PWD = pwd.getText().toString();
                RES_PWD = res_pwd.getText().toString();
                if(!TextUtils.isEmpty(CODE)){
                    if(!TextUtils.isEmpty(PWD) &&!TextUtils.isEmpty(RES_PWD) ){
                        register.setBackgroundColor(Color.parseColor("#F62B59") );
                    }

                }else {
                    register.setBackgroundColor(Color.parseColor("#B36A7B") );
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (DEBUG) {
                if (DEBUG)
                    Log.v("phoneNumber", "输入文字后的状态");
                /** 得到光标开始和结束位置 ,超过最大数后记录刚超出的数字索引进行控制 */

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    /**
     * 用户账号和密码保存
     */
    private void saveUserName(String userid,String pwd) {
        SharedPreferences.Editor editor = mSettings.edit();// 获取编辑器
        editor.putString("userid", userid);
        editor.putString("pwd", pwd);
        editor.putBoolean("ISCHECK", true);
        editor.commit(); //保存数据
    }
}
