package com.crysis.bookshelf;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.UUID;

class Bookitem implements Serializable{
    public static int UNREAD = 0;
    public static int READING = 1;
    public static int FINISH = 2;

    private int ImageId = 0;
    private String Title = "";
    private String Author = "";
    private String Translator = "";
    private String Publisher = "";
    private String PubDate = "";
    private String PubYear = "";
    private String PubMonth = "";
    private String ISBN = "";

    private String bookShelfName = "";
    // 默认设置当前阅读状态为未读
    private String ReadingStatus_Text = "";
    private int ReadingStatus = Bookitem.UNREAD;

    private String Notes = "";
    private String Labels = "";
    private String Website = "";
    private String ImageURL = "";
    private Bitmap bitmap = null;
    private byte[] bitmap_byte = null;
    private String Author_Publisher = "";


    public String getPubDate() {
        return PubDate;
    }

    public void setPubDate(String pubDate) {
        PubDate = pubDate;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getImageId() {
        return ImageId;
    }

    public void setImageId(int imageId) {
        ImageId = imageId;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getTranslator() {
        return Translator;
    }

    public void setTranslator(String translator) {
        Translator = translator;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getBookShelfName() {
        return bookShelfName;
    }

    public void setBookShelfName(String bookShelfName) {
        this.bookShelfName = bookShelfName;
    }

    public int getReadingStatus() {
        return ReadingStatus;
    }

    public void setReadingStatus(int readingStatus) {
        ReadingStatus = readingStatus;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getLabels() {
        return Labels;
    }

    public void setLabels(String labels) {
        Labels = labels;
    }

    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    public String getPubYear() {
        return PubYear;
    }

    public void setPubYear(String pubYear) {
        PubYear = pubYear;
    }

    public String getPubMonth() {
        return PubMonth;
    }

    public void setPubMonth(String pubMonth) {
        PubMonth = pubMonth;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public byte[] getBitmap_byte() {
        return bitmap_byte;
    }

    public void setBitmap_byte(byte[] bitmap_byte) {
        this.bitmap_byte = bitmap_byte;
    }

    public String getReadingStatus_Text() {
        return ReadingStatus_Text;
    }

    public void setReadingStatus_Text(String readingStatus_Text) {
        ReadingStatus_Text = readingStatus_Text;
    }

    public String getAuthor_Publisher() {
        return Author_Publisher;
    }

    public void setAuthor_Publisher(String author_Publisher) {
        Author_Publisher = author_Publisher;
    }
}
