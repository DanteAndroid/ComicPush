package com.danteandroid.comicpush.utils;

import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.model.Comment;

import java.io.IOException;

import hugo.weaving.DebugLog;
import taobe.tec.jcc.JChineseConvertor;

/**
 * Created by yons on 17/11/29.
 */

public class SimpleChineseConvert {
    private static final String TAG = "SimpleChineseConvert";
    public static Book simpleToTraditional(Book book) {
        book.author = simpleToTraditional(book.author);
        book.title = simpleToTraditional(book.title);
        book.bookInSiteState = simpleToTraditional(book.bookInSiteState);
        book.bookState = simpleToTraditional(book.bookState);
        book.publishState = simpleToTraditional(book.bookInSiteState);
        book.desc = simpleToTraditional(book.desc);
        book.rateNumber = simpleToTraditional(book.rateNumber);
        book.vol = simpleToTraditional(book.vol);
        return book;
    }

    public static Book traditionalToSimple(Book book) {
        book.author = traditionalToSimple(book.author);
        book.title = traditionalToSimple(book.title);
        book.bookInSiteState = traditionalToSimple(book.bookInSiteState);
        book.bookState = traditionalToSimple(book.bookState);
        book.publishState = traditionalToSimple(book.publishState);
        book.desc = traditionalToSimple(book.desc);
        book.rateNumber = traditionalToSimple(book.rateNumber);
        book.vol = traditionalToSimple(book.vol);
        return book;
    }

    @DebugLog
    public static Comment traditionalToSimple(Comment comment) {
        comment.author = traditionalToSimple(comment.author);
        comment.title = traditionalToSimple(comment.title);
        comment.bookTitle = traditionalToSimple(comment.bookTitle);
        comment.content = traditionalToSimple(comment.content);
        return comment;
    }


    public static String traditionalToSimple(String s) {
        try {
            s = JChineseConvertor.getInstance().t2s(s);
            s = s.replaceAll("後", "后");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String simpleToTraditional(String s) {
        try {
            s = JChineseConvertor.getInstance().s2t(s);
            s = s.replaceAll("后", "後");
            s = s.replaceAll("曆", "歷");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }


}
