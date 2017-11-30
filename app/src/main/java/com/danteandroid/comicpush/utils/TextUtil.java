package com.danteandroid.comicpush.utils;

/**
 * Created by yons on 17/11/27.
 */

public class TextUtil {

    public static String trim(String s) {
        String clean = s.replace("\u00a0", "").replace("&nbsp;", "");
        return clean.trim();
    }
    public static String removeBrace(String s) {
        return s.replace("[", "").replace("]", "");
    }


    public static int retrieveBookId(String link) {
        final String regex = "[^\\d]*";
        String bookId = link.replaceAll(regex, "");
        return Integer.valueOf(bookId);
    }

    public static String retrieveAuthor(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]") + 1;
        return text.substring(start, end);
    }
}
