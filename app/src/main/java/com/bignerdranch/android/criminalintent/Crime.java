package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by danielmakover on 22/02/2018.
 */

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private Date mTime;
    private boolean mSolved;
    private String mSuspect;

    //Constructor A
    public Crime(){
       this(UUID.randomUUID());
    }

    //Constructor B
    public Crime(UUID id){
        mId = id;
        mDate = new Date();
        mTime = new Date();
    }

    ////Getters and Setters//////
    public UUID getId() {
        return mId;
    }
    public void setId(UUID id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }
    public void setDate(Date date) {
        mDate = date;
    }

    public Date getTime(){return mTime;}
    public void setTime(Date time){mTime = time;}

    public boolean isSolved() {
        return mSolved;
    }
    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect(){
        return mSuspect;
    }

    public void setSuspect(String suspect){
        mSuspect = suspect;
    }

    public String getPhotoFileName(){
        return "IMG_" + getId().toString() + ".jpg";
    }
}
