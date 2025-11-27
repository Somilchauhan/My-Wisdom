package com.somil.dailywisdom.models;

import java.util.ArrayList;
import java.util.List;

public class QuoteModel {
    private String quoteId;
    private String text;
    private String author;
    private String imageUrl;
    private long likeCount = 0;
    private String authorUid;
    private long timestamp;
    private List<String> likedBy = new ArrayList<>();

    public QuoteModel() {
    }

    public QuoteModel(String text, String author, String imageUrl, String authorUid, long timestamp) {
        this.text = text;
        this.author = author;
        this.imageUrl = imageUrl;
        this.authorUid = authorUid;
        this.timestamp = timestamp;
    }


    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }
}