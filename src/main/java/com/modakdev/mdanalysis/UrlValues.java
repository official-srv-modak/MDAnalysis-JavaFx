package com.modakdev.mdanalysis;

public enum UrlValues {
    HTTP("http://"),
    IP("10.0.0.47"),
    LOCALHOST("localhost"),
    FLASK_PORT("7654"),
    PORT("1234"),
    IMAGE_URL(HTTP.getUrl()+LOCALHOST.getUrl()+":"+FLASK_PORT.getUrl()+"/api/get-correlation-matrix-image-df"),
    ANALYSIS_CHAT_URL(HTTP.getUrl()+IP.getUrl()+":8180/analysis-wrapper-module/chat-single-stream"),
    ANALYSIS_CHAT_URL_SAMPLE(HTTP.getUrl()+LOCALHOST.getUrl()+":8180/analysis-wrapper-module/chat-single-stream-sample"),
    ANALYSIS_CHAT_URL_FLASK(HTTP.getUrl()+LOCALHOST.getUrl()+":9876/chat-single-stream"),
    UPLOAD_FILE(HTTP.getUrl()+LOCALHOST.getUrl()+":"+PORT.getUrl()+"/product-catalog-module/product/upload-files");

    private final String url;

    UrlValues(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
