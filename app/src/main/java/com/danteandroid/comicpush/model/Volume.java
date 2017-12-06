package com.danteandroid.comicpush.model;

import io.realm.RealmObject;

/**
 * Created by yons on 17/11/27.
 */

public class Volume  extends RealmObject{
    public String title;
    public String value;
    public String size;
    public String downUrl;

    public Volume(){

    }

    public Volume(String title, String value, String size, String downUrl) {
        this.title = title;
        this.value = value;
        this.size = size;
        this.downUrl = downUrl;
    }

    @Override
    public String toString() {
        return "Volume{" +
                "title='" + title + '\'' +
                ", value='" + value + '\'' +
                ", size='" + size + '\'' +
                ", downUrl='" + downUrl + '\'' +
                '}';
    }
}
