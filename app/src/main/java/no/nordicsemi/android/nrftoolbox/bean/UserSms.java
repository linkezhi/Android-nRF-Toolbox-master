package no.nordicsemi.android.nrftoolbox.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by hj on 2015/10/15.
 */
public class UserSms extends BmobObject {
    private String user;
    private String pwd;
    private String birth;
    private String  height;
    private String  weight;
//    private String objectId;



    public UserSms() {
        this.setTableName("UserSms");
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }


   /* public String getObjectId(){
        return objectId;
    }

    public void setObjectId (String objectId){
        this.objectId=objectId;
    }*/

}
