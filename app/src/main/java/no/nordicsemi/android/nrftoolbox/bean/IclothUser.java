package no.nordicsemi.android.nrftoolbox.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by hj on 2015/11/9.
 */
public class IclothUser extends BmobUser {

    private String birth;
    private String  height;
    private String  weight;
    private String password;

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public IclothUser() {
        this.setTableName("_User");
    }
    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
