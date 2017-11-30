package com.danteandroid.comicpush.net;

import android.util.Log;

import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.model.Volume;
import com.danteandroid.comicpush.utils.SimpleChineseConvert;
import com.danteandroid.comicpush.utils.SpUtil;
import com.danteandroid.comicpush.utils.TextUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.danteandroid.comicpush.utils.TextUtil.retrieveAuthor;
import static com.danteandroid.comicpush.utils.TextUtil.retrieveBookId;

/**
 * Created by yons on 17/11/23.
 */

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private static DataFetcher instance = new DataFetcher();
    public String url; //source url
    public String query; //query type
    private int page;
    private int bookId;

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

    public static DataFetcher getInstance(int bookId) {
        instance.bookId = bookId;
        return instance;
    }

    public Observable<List<Book>> fetch() {
        return NetService.request().books(query, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    String data = "";
                    try {
                        data = responseBody.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return parseMain(data);
                });
    }

    public Observable<Book> fetchDetail() {
        return NetService.request().bookDetail(bookId)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    String data = "";
                    try {
                        data = responseBody.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return parseBookDetail(data);
                });
    }

    private List<Book> parseMain(String data) {
        List<Book> books = new ArrayList<>();
        try {
            Document document = Jsoup.parse(data);
            Elements elements = document.select("tr.listbg > td");
            final int size = elements.size();
            for (int i = 0; i < size; i++) {
                Element b = elements.get(i);
                String cover = b.select("img[src]").first().attr("src");
                Elements a = b.select("a[href]");
                String title = a.get(1).text();
                String link = a.get(1).attr("href");
                int bookId = retrieveBookId(link);
                String vol = b.select("font.pagefoot").first().text();
                String author = retrieveAuthor(b.text());

                if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
                    title =SimpleChineseConvert.traditionalToSimple(title);
                    author =SimpleChineseConvert.traditionalToSimple(author);
                }
                Book book = new Book(title, author, vol, cover, bookId);
                books.add(book);
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "parseMain: " + e.getMessage());
        }
        return books;
    }

    private Book parseBookDetail(String data) {
        Book detailBook = new Book();
        RealmList<Volume> list = new RealmList<>();
        try {
            Document document = Jsoup.parse(data);
            Element element = document.select("div.bookinfo").first();

            Element divAuthor = element.select("div#author").first();
            Elements statuses = divAuthor.select("font#status");
            detailBook.bookState = TextUtil.trim(statuses.get(3).text());
            detailBook.publishState = statuses.get(1).text();
            detailBook.bookInSiteState = statuses.get(2).text();

            Log.d(TAG, "parseBookDetail: " + detailBook.toString());

            Element tdScore = element.select("td#book_score").first();
            Elements elements = tdScore.select("tr > td > font");
            detailBook.rate = elements.get(0).text();
            detailBook.rateNumber = elements.get(3).text();

            Element divDesc = document.select("div.book_desc").first();
            String s = divDesc.select("div#desc_text").text().replaceAll("&nbsp;", "");
            detailBook.desc = TextUtil.trim(s);

            Element divVolume = document.select("div#div_mobi").first();
            Elements volumes = divVolume.select("tr[class] > td");

            for (int i = 0; i < volumes.size(); i += 3) {
                String title = volumes.get(i).text().replaceAll("&nbsp;", "").trim();
                String value = volumes.get(i + 1).select("input[name=checkbox_push]").first().val();
                String fs = volumes.get(i).select("font.filesize").first().text();
                String size = volumes.get(i + 1).select("input[name=size_push_" + value + "]").first().val();
                Volume volume = new Volume(TextUtil.trim(title).replace(fs, ""), value, size);
                list.add(volume);
            }
            detailBook.volList = list;

        } catch (NullPointerException e) {
            detailBook.volList = list;
            e.printStackTrace();
        }
        if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
            detailBook = SimpleChineseConvert.traditionalToSimple(detailBook);
        }
        return detailBook;
    }


}
