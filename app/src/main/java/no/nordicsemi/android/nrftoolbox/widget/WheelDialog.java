package no.nordicsemi.android.nrftoolbox.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import no.nordicsemi.android.nrftoolbox.R;

public class WheelDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private ArrayList<String> mList;
    private Context mContext;

    private Button button_ok,button_cancel;
    private WheelView mWheel;

    private  OnDateSetListener listener;

    private TextView tv_ext;

    private String ext;

    public interface OnDateSetListener{
        void onDateSet(WheelView view, int index);
    }

    public void setOnDateSetListener(OnDateSetListener l){
        listener = l;
    }

    public WheelView getWheelView(){
        return mWheel;
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.wheel_layout, null);
        mWheel = (WheelView) view.findViewById(R.id.wheelView);
        tv_ext = (TextView) view.findViewById(R.id.tv_ext);

        tv_ext.setText(ext);

        mWheel.setData(mList);
        setView(view);
        setButton(BUTTON_POSITIVE, "ok", this);
        setButton(BUTTON_NEGATIVE, "cancel", this);
    }
    public WheelDialog(Context context, ArrayList<String> list,String ext) {
        super(context);
        this.mList = list;
        mContext = context;
        this.ext = ext;
        initView();

    }


    protected WheelDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected WheelDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (listener != null) {
                    listener.onDateSet(mWheel,mWheel.getSelected());
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }


}
