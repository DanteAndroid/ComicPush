package com.danteandroid.comicpush.utils;

import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.model.Book;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by yons on 17/11/27.
 */

public class Database {

    public final Realm realm;

    private Database(Realm realm) {
        this.realm = realm;
    }

    public static Database getInstance(Realm realm) {
        return new Database(realm);
    }

    public Book findBook(int bookId) {
        return realm.where(Book.class).equalTo(Constants.BOOK_ID, bookId).findFirst();
    }

    public void save(RealmObject object){
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(object);
        realm.commitTransaction();
    }

    public void clear() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
