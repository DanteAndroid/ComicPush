package com.danteandroid.comicpush.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yons on 17/11/23.
 */

public class Book extends RealmObject{
    @PrimaryKey
    public int bookId;

    public String title="";
    public String author="";
    public String vol="";
    public String desc="";
    public String cover="";
    public String rate="";
    public String rateNumber="";
    public String bookState="";
    public String publishState="";
    public String bookInSiteState="";
    public String info="";
    public RealmList<Volume> volList;

    public Book(){

    }

    public Book(String title, String author, String vol, String cover, int bookId) {
        this.title = title;
        this.author = author;
        this.vol = vol;
        this.cover = cover;
        this.bookId = bookId;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", vol='" + vol + '\'' +
                ", desc='" + desc + '\'' +
                ", cover='" + cover + '\'' +
                ", rate='" + rate + '\'' +
                ", rateNumber='" + rateNumber + '\'' +
                ", bookState='" + bookState + '\'' +
                ", publishState='" + publishState + '\'' +
                ", bookInSiteState='" + bookInSiteState + '\'' +
                ", info='" + info + '\'' +
                ", bookId=" + bookId +
                ", volList=" + volList +
                '}';
    }
}
