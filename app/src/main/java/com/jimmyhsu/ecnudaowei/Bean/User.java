package com.jimmyhsu.ecnudaowei.Bean;

/**
 * Created by jimmyhsu on 2016/10/27.
 */

public class User {

    public static String TB_NAME = "usertb";
    public static final String COL_NAME = "col_name";
    public static final String COL_REGDATE = "col_regdate";
    public static final String COL_SIGNATURE = "col_signature";
    public static final String COL_USERINFO_ID = "col_userinfo_id";
    public static final String COL_SEX = "col_sex";
    public static final String COL_STU_ID = "col_stu_id";
    public static final String COL_MOBILE = "col_mobile";
    public static final String COL_AGE = "col_age";


    private String name;
    private long regdate;
    private String signature;
    private int id;
    private int sex;
    private String studentId;
    private String mobile;
    private int age;


    public User(String name, long regdate, String signature, int id, int sex, String studentId, String mobile, int age) {
        this.name = name;
        this.regdate = regdate;
        this.signature = signature;
        this.id = id;
        this.sex = sex;
        this.studentId = studentId;
        this.mobile = mobile;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRegdate() {
        return regdate;
    }

    public void setRegdate(long regdate) {
        this.regdate = regdate;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
