package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wowzhuo on 2015/11/4.
 */
public class BleApplication extends Application {


    private List<Activity>  activities;

    private static BleApplication app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        activities = new ArrayList<>();
    }


    public static BleApplication getInstance(){
        return app;
    }

    public void add(Activity activity){
        activities.add(activity);
    }

    public void finish(){
        for (int i = 0;i<activities.size();i++){
            activities.get(i).finish();
        }
    }


}
