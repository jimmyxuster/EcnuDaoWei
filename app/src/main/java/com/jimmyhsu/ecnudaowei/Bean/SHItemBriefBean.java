package com.jimmyhsu.ecnudaowei.Bean;

import com.jimmyhsu.ecnudaowei.SecondHandDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jimmyhsu on 2016/10/9.
 */
public class SHItemBriefBean {
    private int id;
    private String name;
    private String time;
    private String touXiangUrl;
    private String imageUrl;
    private int price;
    private String title;

    public SHItemBriefBean() {
    }

    public SHItemBriefBean(int id, String name, String time, String touXiangUrl, String imageUrl, int price, String title) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.touXiangUrl = touXiangUrl;
        this.imageUrl = imageUrl;
        this.price = price;
        this.title = title;
    }


    public SHItemBriefBean(int id, String name, long time, String touXiangUrl, String imageUrl, int price, String title) {
        this.id = id;
        this.name = name;
        this.time = SecondHandDetailActivity.getTimeDes(time);
//        Calendar calendar = Calendar.getInstance();
//        Calendar today = Calendar.getInstance();
//        Calendar yesterday = Calendar.getInstance();
//        yesterday.add(Calendar.DATE, -1);
//        calendar.setTimeInMillis(time);
//        if(calendar.after(today)){
//            this.time = "今天";
//        }else if(calendar.before(today) && calendar.after(yesterday)){
//
//            this.time = "昨天";
//        }else{
//            this.time = new SimpleDateFormat("MM-dd").format(new Date(time));
//        }
        this.touXiangUrl = touXiangUrl;
        this.imageUrl = imageUrl;
        this.price = price;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTime(long time) {
         this.time = new SimpleDateFormat("MM月dd日HH:mm").format(new Date(time));
    }

    public String getTouXiangUrl() {
        return touXiangUrl;
    }

    public void setTouXiangUrl(String touXiangUrl) {
        this.touXiangUrl = touXiangUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
