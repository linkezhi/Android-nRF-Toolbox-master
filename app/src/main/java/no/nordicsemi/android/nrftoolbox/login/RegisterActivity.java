package no.nordicsemi.android.nrftoolbox.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONObject;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.RegisterPicPopupWindow;
import no.nordicsemi.android.nrftoolbox.bean.UserSms;
import no.nordicsemi.android.nrftoolbox.utility.PhoneUtils;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public EditText register_number;
    public Button get_code;
    public ImageButton returnMainActivity;
    private Boolean isRegister = true;//新账号注册为true，旧账号修改密码为false
    private String registerState;
    public String phoneNumber;
    private RegisterPicPopupWindow menuWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_register);

        registerState();//判断是注册新账号或者修改密码

        EventHandler eh=new EventHandler(){

            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
                Log.v("SMSS", "注册回调函数");
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调

        intView();

    }

    /**
     * 注册新账号或忘记密码
     */
    public void registerState (){
        Bundle bundle = getIntent().getExtras();
        registerState = bundle.getString("isRegister");
        if(registerState.equals("true")){
            Log.v("isRegister","注册新账号");
            isRegister = true;
        }else {
            Log.v("isRegister","忘记密码，修改密码");
            isRegister = false;
        }
    }

    public void intView () {
        register_number = (EditText) findViewById(R.id.register_number);
        get_code = (Button) findViewById(R.id.get_code);
        returnMainActivity = (ImageButton) findViewById(R.id.returnMainActivity);
        get_code.setOnClickListener(this);
        returnMainActivity.setOnClickListener(this);
        register_number.addTextChangedListener(new EditChangedListener());
}


    @Override
    public void onClick(View v) {
    switch (v.getId()){
        //获取验证码
    case R.id.get_code:
        /*Log.v("SMSS","点击获取验证码");
        if(!TextUtils.isEmpty(register_number.getText().toString())){
            SMSSDK.getVerificationCode("86", register_number.getText().toString());
            phoneNumber = register_number.getText().toString();
            Log.v("SMSS", "发送号码" + register_number.getText().toString());
            //检测是否已注册
            querayCount(phoneNumber);

            //跳转到密码设置界面
            //pwdIntent();
        }else {
            Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
        }*/
        if(!TextUtils.isEmpty(register_number.getText().toString())){
            phoneNumber = register_number.getText().toString();
            if(isRegister){
                //检测是否已注册
                querayCount(phoneNumber);
            }else {
                //发送短信验证码
                getSmssCode();
                //跳转到密码设置界面
                pwdIntent(false);
            }
            // getSmssCode ();
        }else {
            Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
        }

        break;
    case R.id.returnMainActivity:
        RegisterActivity.this.finish();
        break;
}
    }

    /**
     * 获取短信验证码
     */
    public void getSmssCode (){
        Log.v("SMSS","点击获取验证码");
        if(!TextUtils.isEmpty(register_number.getText().toString())){
            SMSSDK.getVerificationCode("86", register_number.getText().toString());
            phoneNumber = register_number.getText().toString();
            Log.v("SMSS", "发送号码" + register_number.getText().toString());
        }else {
            Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    /**
     * 跳转密码设置界面，pwdState为true为新账号注册，flase为修改密码
     * @param pwdState
     */
    public void pwdIntent(Boolean pwdState) {
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, PwdActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("registerNumber", phoneNumber);//注册号码
        if(pwdState){
            mBundle.putString("pwdState","true");
        }else {
            mBundle.putString("pwdState","flase");
        }

        intent.putExtras(mBundle);
        startActivity(intent);
    }


    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Log.v("SMSS", "函数回调");
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                    Log.v("handleMessage","验证码已经发送");
                }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 根据服务器返回的网络错误，给toast提示
//#elif def{lang} == en
// show toast according to the error code
//#endif

                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    int status = object.optInt("status");//错误代码
                    Log.v("error code",status+"");
                    switch (status){
                        case 463:
                            Toast.makeText(getApplicationContext(), R.string.smssdk_error_desc_463, Toast.LENGTH_SHORT).show();
                            break;
                        case 465:
                            Toast.makeText(getApplicationContext(), R.string.smssdk_error_desc_465, Toast.LENGTH_SHORT).show();
                            break;
                        case 462:
                            Toast.makeText(getApplicationContext(), R.string.smssdk_error_desc_462, Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(getApplicationContext(), R.string.smssdk_error_desc_500, Toast.LENGTH_SHORT).show();
                            break;
                        case 468:
                            Toast.makeText(getApplicationContext(), R.string.smssdk_error_desc_468, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (Exception e) {
                    //do something
                }
            }

        }

    };

    /**
     * 文本变化的监听
     */
    class EditChangedListener implements TextWatcher {
    private CharSequence temp;//监听前的文本
    private int editStart;//光标开始位置
    private int editEnd;//光标结束位置
    private final int charMaxNum = 11;
    private boolean DEBUG = true;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        if (DEBUG) {
            Log.v("phoneNumber", "输入文本之前的状态");
            temp = s;
        }

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (DEBUG) {
            Log.v("phoneNumber", "还能输入" + (charMaxNum - s.length()) + "字符");
            String numberNo = register_number.getText().toString();
            if(PhoneUtils.isMobileNO(numberNo)){
                Log.v("phoneNumber", "正确手机号码");
                get_code.setClickable(true);
                get_code.setBackgroundColor(Color.parseColor("#F62B59") );
            }else {
                get_code.setClickable(false);
                get_code.setBackgroundColor(Color.parseColor("#B36A7B") );
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (DEBUG) {
            if (DEBUG)
                Log.v("phoneNumber", "输入文字后的状态");
            /** 得到光标开始和结束位置 ,超过最大数后记录刚超出的数字索引进行控制 */
            editStart = register_number.getSelectionStart();
            editEnd = register_number.getSelectionEnd();
            if (temp.length() >= charMaxNum) {
                String numberNo = register_number.getText().toString();
                if(PhoneUtils.isMobileNO(numberNo)){
                    Log.v("phoneNumber", "正确手机号码");
                    get_code.setClickable(true);
                    get_code.setBackgroundColor(Color.parseColor("#F62B59") );
                }else {
                    Toast.makeText(getApplicationContext(), "请输入正确手机号码", Toast.LENGTH_LONG).show();
                    s.delete(editStart - 1, editEnd);
                    int tempSelection = editStart;
                    register_number.setText(s);
                    register_number.setSelection(tempSelection);
                }

            }
        }
    }
}

    /**
     * 查询号码是否注册
     * @param phoneNumber
     */

    public void querayCount(final String phoneNumber) {

        Log.v("querayCount", "查询手机号" + phoneNumber);
        BmobQuery<UserSms> queryPhone = new BmobQuery<UserSms>();
        queryPhone.addWhereEqualTo("user", phoneNumber);
        queryPhone.count(this, UserSms.class, new CountListener() {
            @Override
            public void onSuccess(int i) {
                if(i != 0){
                    Log.v("querayCount","已注册 "+ i);
                    //弹出底部对话框
                    showWindow();
                }else {
                    Log.v("querayCount","未注册 "+ i);
                    //发送短信验证码
                    getSmssCode ();
                    //跳转到密码设置界面
                    pwdIntent(true);
                }

            }

            @Override
            public void onFailure(int i, String s) {
                Log.v("querayCount","查询失败"+s+i);
            }
        });

    }

    /**
     * 底部弹出窗
     */
    public void showWindow(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(register_number.getWindowToken(), 0);
        //实例化SelectPicPopupWindow
        menuWindow = new RegisterPicPopupWindow(RegisterActivity.this, itemsOnClick);
        //显示窗口
        menuWindow.showAtLocation(RegisterActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }
    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener(){

        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.fg_pwd:
                    //发送短信验证码
                    getSmssCode();
                    //密码注册
                    pwdIntent(false);
                    break;
                case R.id.rt_login:
                    RegisterActivity.this.finish();
                    break;
                default:
                    break;
            }


        }

    };


}
