package no.nordicsemi.android.nrftoolbox.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.smssdk.SMSSDK;
import no.nordicsemi.android.nrftoolbox.HrsHtsMonitorActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.bean.UserSms;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mSettings = null;

    public static String APPID = "f7f7b3f84642a2559abc324d760c9567";//数据库服务器应用秘钥
    private static String APPKEY = "b2d56334637c";// 短信SDK应用后台注册得到的APPSECRET
    private static String APPSECRET = "c941bf924bc20b63eb8e1000556ffa7b";//SMSS秘钥

    public Button login;
    public EditText login_accound, login_pwd;
    public TextView  register, login_quit;
    public String phoneNumber, pwd;



    public BmobQuery<UserSms> query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences("prefsname", Context.MODE_PRIVATE); //模式为私有
        autoLogin();//自动登录
        setContentView(R.layout.activity_main);
        //初始化Bmob SDK数据库服务
        Bmob.initialize(getApplicationContext(), APPID);
        //初始化bugly服务
        CrashReport.initCrashReport(this, "900010093", false);
        //初始化SMSS短信服务
        SMSSDK.initSDK(this, APPKEY, APPSECRET);
        //用户数据模型
        initView();


    }

    public void initView() {

        login = (Button) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        login_accound = (EditText) findViewById(R.id.login_accound);
        login_pwd = (EditText) findViewById(R.id.login_pwd);
        login_quit = (TextView) findViewById(R.id.quit);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        login_quit.setOnClickListener(this);

//        login_accound.addTextChangedListener(new EditChangedListener());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login://账号登陆
                loginIcloth();
                break;
            case R.id.register://账号注册
                //跳转到注册页面
                accountRegister(true);
                break;
            case R.id.quit:
                //忘记密码
                accountRegister(false);
                //System.exit(0);
                break;
        }

    }



    /**
     * 账号登陆
     */
    public void loginIcloth() {
        phoneNumber = login_accound.getText().toString();
        pwd = login_pwd.getText().toString();

        if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(pwd)) {

            querayData(phoneNumber, pwd);
            //BmobUser登陆
            //userLogin(phoneNumber,pwd);
        } else {
            Toast.makeText(getApplicationContext(), "手机号码与密码不能为空", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 登录查询
     * @param phoneNumber
     * @param userPwd
     */

    public void querayData(final String phoneNumber, final String userPwd) {

        Log.v("querayData", "查询手机号" + phoneNumber);
        BmobQuery<UserSms> queryPhone = new BmobQuery<UserSms>();
        queryPhone.addWhereEqualTo("user", phoneNumber);
          queryPhone.findObjects(this, new FindListener<UserSms>() {
              @Override
              public void onSuccess(List<UserSms> list) {
                  Log.v("querayData", "查询成功");
                  for (UserSms userScore : list) {
                      //获得playerName的信息
                      String USER = userScore.getUser();
                      String PWD = userScore.getPwd();
                      if (USER.equals(phoneNumber)) {
                          Log.v("querayData", USER + "号码已注册");
                          if (PWD.equals(userPwd)) {
                              HRSInformation();
                              saveUserName(USER, PWD);
                          } else {
                              Toast.makeText(getApplicationContext(), "密码错误请重新输入", Toast.LENGTH_SHORT).show();
                              Log.v("querayData", "密码错误:");
                          }
                      }
                  }
                  if (list.size() == 0) {
                      Toast.makeText(getApplicationContext(), "该号码未注册", Toast.LENGTH_SHORT).show();
                  }
              }

              @Override
              public void onError(int i, String s) {
                  Log.v("querayData", "查询失败"+ s);
              }
          });

    }

    /**
     *BmobUser登陆
     */
    public void userLogin (final String user, final String pwd){
        BmobUser bu = new BmobUser();
        bu.setUsername(user);
        bu.setPassword(pwd);
        bu.login(getApplicationContext(), new SaveListener() {

            @Override
            public void onSuccess() {
                Log.v("userlogin","成功"+user+" "+pwd);
                HRSInformation();
                saveUserName(user, pwd);
            }

            @Override
            public void onFailure(int code, String msg) {
                Log.v("userlogin","失败代码"+code+" msg"+msg);
                Toast.makeText(MainActivity.this, "用户名或密码错误",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    /**
     * 跳转到Hrs页面
     */
    public void HRSInformation() {
        Intent intent = new Intent();
        intent.setClass(this, HrsHtsMonitorActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转注册页面
     */
    public void accountRegister(Boolean State) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, RegisterActivity.class);
        Bundle mBundle = new Bundle();
        if(State){
            mBundle.putString("isRegister", "true");//注册
        }else {
            mBundle.putString("isRegister", "false");//忘记密码
        }
        intent.putExtras(mBundle);
        startActivity(intent);
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

    /**
     * 自动登录
     */
    private void autoLogin (){

        String userNameValue;
        String passwordValue;
        Boolean autoValue;

        autoValue = mSettings.getBoolean("ISCHECK", false);//是否自动登录
        userNameValue = mSettings.getString("userid", "");
        passwordValue  = mSettings.getString("pwd", "");

        if(autoValue){
            Log.v("userlogin", "自动登录查询" + userNameValue+passwordValue);
            querayData(userNameValue, passwordValue);
            //BmobUser登陆
           // userLogin(userNameValue,passwordValue);

        }

    }



}