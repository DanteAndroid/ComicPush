package com.danteandroid.emulatelogin.net;

import android.text.TextUtils;
import android.util.Log;

import com.danteandroid.emulatelogin.main.Book;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yons on 17/11/23.
 */

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private static DataFetcher instance = new DataFetcher();
    public String url; //source url
    public String query; //query type
    private int page;
    private String link;

    public DataFetcher() {

    }

    private DataFetcher(String type, int page) {
        this.query = type;
        this.page = page;
    }

    public static DataFetcher getInstance(String type, int page) {
        instance.query = type;
        instance.page = page;
        return instance;
    }
    public static DataFetcher getInstance(String link) {
        instance.link = link;
        return instance;
    }

    public Observable<List<Book>> fetch() {
        return NetService.request().books(query, page)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, List<Book>>() {
                    @Override
                    public List<Book> call(ResponseBody responseBody) {
                        String data = "";
                        try {
                            data = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return parseMain(data);
                    }
                });
    }

    public Observable<Book> fetchDetail() {
        return NetService.request().bookDetail(link)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, Book>() {
                    @Override
                    public Book call(ResponseBody responseBody) {
                        String data = "";
                        try {
                            data = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return parseBookDetail(data);
                    }
                });
    }

    private List<Book> parseMain(String data) {
        List<Book> books = new ArrayList<>();
        if (!TextUtils.isEmpty(data)) {
            Document document = Jsoup.parse(data);
            Elements elements = document.select("tr.listbg > td");

            final int size = elements.size();
            try {
                for (int i = 0; i < size; i++) {
                    Element b = elements.get(i);
                    String cover = b.select("img[src]").first().attr("src");
                    Elements a = b.select("a[href]");
                    String title = a.get(1).text();
                    String link = a.get(1).attr("href");
                    link = retrieveLink(link);
                    String vol = b.select("font.pagefoot").first().text();
                    String author = retrieveAuthor(b.text());
                    books.add(new Book(title, author, vol, cover));
                    Log.d(TAG, "parseMain: " + link);
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "parseMain: " + e.getMessage());
            }

        }
        return books;
    }
    private Book parseBookDetail(String data) {
        // TODO: 17/11/23
        Book book = new Book();
        if (!TextUtils.isEmpty(data)) {
            Document document = Jsoup.parse(data);
            Elements elements = document.select("tr.listbg > td");

            final int size = elements.size();
            try {
                    Element b = elements.get(1);
                    String cover = b.select("img[src]").first().attr("src");
                    Elements a = b.select("a[href]");
                    String title = a.get(1).text();
                    String link = a.get(1).attr("href");
                    link = retrieveLink(link);
                    String vol = b.select("font.pagefoot").first().text();
                    String author = retrieveAuthor(b.text());
                    Log.d(TAG, "parseMain: " + link);
            } catch (NullPointerException e) {
                Log.d(TAG, "parseMain: " + e.getMessage());
            }

        }
        return book;
    }

    private String retrieveLink(String link) {
        return link.replace(API.BASE_URL, "");
    }

    private String retrieveAuthor(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]") + 1;
        return text.substring(start, end);
    }

}
