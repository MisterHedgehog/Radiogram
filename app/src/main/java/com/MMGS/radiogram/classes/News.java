package com.MMGS.radiogram.classes;

public class News {

    private String uri;
    private String title;
    private String srcUri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSrcUri() {
        return srcUri;
    }

    public void setSrcUri(String srcUri) {
        this.srcUri = srcUri;
    }

    public News(String uri, String title, String srcUri){
        this.uri = uri;
        this.title = title;
        this.srcUri = srcUri;

    }
}
