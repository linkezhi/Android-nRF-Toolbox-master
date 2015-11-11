package no.nordicsemi.android.nrftoolbox.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by hj on 2015/11/4.
 */
public class UserData extends BmobObject {
    private String user;
    private Double heart_rate;
    private Double temperature;

    public UserData() {
        this.setTableName("UserData");
    }
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getHeart_rate() {
        return heart_rate;
    }

    public void setHeart_rate(Double heart_rate) {
        this.heart_rate = heart_rate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }


}
