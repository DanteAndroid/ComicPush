package com.danteandroid.emulatelogin.main;

import io.realm.RealmObject;

/**
 * Created by yons on 17/11/23.
 */

public class Book extends RealmObject{
    public String title;
    public String author;
    public String vol;
    public String intro;
    public String cover;
    public String link;
    public String bookState;
    public String publishState;
    public String bookInSiteState;
    public String info;

    public Book(){

    }

    public Book(String title, String author, String vol, String cover) {
        this.title = title;
        this.author = author;
        this.vol = vol;
        this.cover = cover;
    }
}
