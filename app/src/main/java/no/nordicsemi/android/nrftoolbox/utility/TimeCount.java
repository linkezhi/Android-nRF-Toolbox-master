package no.nordicsemi.android.nrftoolbox.utility;

import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;

/**
 * Created by hj on 2015/11/3.
 */
public class TimeCount extends CountDownTimer {
    private Activity mActivity;
    private Button btn;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public TimeCount(long millisInFuture, long countDownInterval,Activity mActivity,Button btn) {
        super(millisInFuture, countDownInterval);
        this.mActivity = mActivity;
        this.btn = btn;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        btn.setClickable(false);//设置不能点击
        btn.setText(millisUntilFinished / 1000 + "秒后可重发");//设置倒计时
        Spannable span = new SpannableString(btn.getText().toString());//获取按钮的文字
        span.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);//讲倒计时时间显示为红色
        btn.setText(span);
    }

    @Override
    public void onFinish() {
        btn.setText("重获验证码");
        btn.setClickable(true);//重新获得点击
        //btn.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_btn_back));//还原背景色
    }
}
