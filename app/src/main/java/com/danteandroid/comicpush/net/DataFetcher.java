package com.danteandroid.comicpush.net;

import android.util.Log;

import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.model.CollectionItem;
import com.danteandroid.comicpush.model.Comment;
import com.danteandroid.comicpush.model.Volume;
import com.danteandroid.comicpush.utils.Database;
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

import io.realm.Realm;
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

    public static DataFetcher getInstance(int bookId, int page) {
        instance.bookId = bookId;
        instance.page = page;
        return instance;
    }

    public static Observable<List<CollectionItem>> fetchMy() {
        return NetService.request().myFollowings()
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    String data = "";
                    try {
                        data = responseBody.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return parseMyFollowings(data);
                });
    }

    private static List<CollectionItem> parseMyFollowings(String data) {
        List<CollectionItem> items = new ArrayList<>();
        try {
            Document document = Jsoup.parse(data);
            Elements elements = document.select("table.book_list tr");
            final int size = elements.size();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                checkListTitle(items, element);
                checkBookItem(items, element);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return items;
    }

    private static void checkBookItem(List<CollectionItem> items, Element element) {
        if (element.className().contains("listbg")) {
            Elements tds = element.getElementsByTag("td");
            String first = TextUtil.trim(tds.get(0).text());
            if (tds.size() == 5 && !first.isEmpty()) {
                if (first.contains("成功") || first.contains("推送") || first.contains("失") || first.contains("中") || first.contains("排") || first.contains("處理")) {
                    CollectionItem item = new CollectionItem(CollectionItem.ITEM_PUSH_CONTENT);
                    item.info = first;
                    String link = tds.get(1).select("a[href]").first().attr("href");
                    item.bookId = retrieveBookId(link);
                    item.lastUpdate = TextUtil.trim(tds.get(3).text());
                    String pushTitle = TextUtil.trim(tds.get(1).text());
                    String shortTitle = pushTitle.replace("[Mobs]", "")
                            .replace(Constants.VOL_PREFIX, "").replace("[Mobi]", "");
                    if (shortTitle.length() > 1) {
                        pushTitle = shortTitle;
                    }
                    item.title = pushTitle;
                    Book book = Database.getInstance(Realm.getDefaultInstance()).findBook(item.bookId);
                    if (book != null) {
                        item.title = String.format("%s %s", pushTitle, book.title);
                        item.author = book.author;
                    }
                    if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
                        item.title = SimpleChineseConvert.traditionalToSimple(item.title);
                        item.author = SimpleChineseConvert.traditionalToSimple(item.author);
                    }
                    items.add(item);

                } else {
                    CollectionItem item = new CollectionItem(CollectionItem.ITEM_BOOK_CONTENT);
                    item.bookId = Integer.parseInt(first);
                    Book book = Database.getInstance(Realm.getDefaultInstance()).findBook(item.bookId);
                    if (book != null) {
                        item.cover = book.cover;
                    }
                    item.title = TextUtil.trim(tds.get(1).text());
                    item.author = TextUtil.trim(tds.get(2).text());
                    item.lastUpdate = TextUtil.trim(tds.get(3).text());
                    if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
                        item.title = SimpleChineseConvert.traditionalToSimple(item.title);
                        item.author = SimpleChineseConvert.traditionalToSimple(item.author);
                    }
                    items.add(item);
                }
            }
        }
    }

    private static void checkListTitle(List<CollectionItem> items, Element element) {
        Elements listtitles = element.select("td.listtitle");
        for (Element e : listtitles) {
            String text = TextUtil.trim(e.text());
            if (!text.isEmpty()) {
                CollectionItem item = new CollectionItem(CollectionItem.ITEM_TITLE);
                item.title = text;
                if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
                    item.title = SimpleChineseConvert.traditionalToSimple(item.title);
                }
                items.add(item);
            }
        }
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

    public Observable<RealmList<Comment>> fetchComment(){
       return NetService.request().commentList(bookId, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    String data = "";
                    try {
                        data = responseBody.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return parseComments(data);
                });
    }


    private RealmList<Comment> parseComments(String doc) {
        RealmList<Comment> comments = new RealmList<>();
        Document document = Jsoup.parse(doc);
        Elements elements = document.select("script");
        Log.d(TAG, "parseComments: " + elements.size());
        try {
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String script = element.toString();
                int start = script.indexOf("(") + 1;
                int end = script.indexOf(")");
                String content = script.substring(start, end);
                String[] data = content.split(",");
                Comment comment = new Comment();
                comment.author = data[2];
                comment.bookId = Integer.valueOf(data[3].trim().replace("\"", ""));
                comment.content = data[data.length - 3];
                comment.title = data[7];
                comment.date = data[8];
                comment.bookTitle = data[4];
                if (!SpUtil.getBoolean(Constants.IS_TRADITIONAL)) {
                    comment = SimpleChineseConvert.traditionalToSimple(comment);
                }
                comments.add(comment);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "parseComments: " + e.getMessage());
        }
        return comments;
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
                    title = SimpleChineseConvert.traditionalToSimple(title);
                    author = SimpleChineseConvert.traditionalToSimple(author);
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

            String cover = element.select("img#img_book").first().attr("src");
            detailBook.cover = cover;
            Element divAuthor = element.select("div#author").first();
            Elements statuses = divAuthor.select("font#status");
            int start = divAuthor.text().lastIndexOf("]");
            String detailTitle = divAuthor.text().substring(start + 1).split("\u00a0")[0].trim();
            int braceStart = detailTitle.lastIndexOf("(");
            int end = detailTitle.lastIndexOf(")") + 1;
            detailBook.title = detailTitle.replace(detailTitle.substring(braceStart, end), "");
            detailBook.author = statuses.get(0).text();
            detailBook.publishState = statuses.get(1).text();
            detailBook.bookInSiteState = statuses.get(2).text();
            detailBook.bookState = TextUtil.trim(statuses.get(3).text());

            Element tdScore = element.select("td#book_score").first();
            Elements elements = tdScore.select("tr > td > font");
            detailBook.rate = elements.get(0).text();
            detailBook.rateNumber = elements.get(3).text();

            Element divDesc = document.select("div.book_desc").first();
            String s = divDesc.select("div#desc_text").text().replaceAll("&nbsp;", "");
            detailBook.desc = TextUtil.trim(s);

            Element divVolume = document.select("div#div_mobi").first();
            Element divEpub = document.select("div#div_cbz").first();
            Elements volumes = divVolume.select("tr[class] > td");
            Elements epubVolumes = divEpub.select("tr[class] > td");

            for (int i = 0; i < volumes.size(); i += 3) {
                String title = volumes.get(i).text().replaceAll("&nbsp;", "").trim();
                String value = volumes.get(i + 1).select("input[name=checkbox_push]").first().val();
                String fs = volumes.get(i).select("font.filesize").first().text();
                String size = volumes.get(i + 1).select("input[name=size_push_" + value + "]").first().val();
                String downUrl = epubVolumes.get(i + 1).select("a[href]").first().attr("href");
                Volume volume = new Volume(TextUtil.trim(title).replace(fs, ""), value, size, downUrl);
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
