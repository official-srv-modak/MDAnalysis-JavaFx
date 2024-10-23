package com.modakdev.mdanalysis.values;

public enum UrlValues {
    HTTP("http://"),
    IP("10.0.0.47"),
    LOCALHOST("localhost"),
    FLASK_PORT("7654"),
    PORT("1234"),
    IMAGE_URL(HTTP.getUrl()+IP.getUrl()+":"+FLASK_PORT.getUrl()+"/api/get-correlation-matrix-image-df"),
    ANALYSIS_CHAT_URL(HTTP.getUrl()+IP.getUrl()+":8180/analysis-wrapper-module/chat-single-stream"),
    ANALYSIS_CHAT_URL_SAMPLE(HTTP.getUrl()+IP.getUrl()+":8180/analysis-wrapper-module/chat-single-stream-sample"),
    ANALYSIS_CHAT_URL_FLASK(HTTP.getUrl()+IP.getUrl()+":9876/chat-single-stream"),
    GET_ALL_PRODUCTS(HTTP.getUrl()+LOCALHOST.getUrl()+":1234/product-catalog-module/product/get-all-products"),
    TRAIN_MODEL(HTTP.getUrl()+LOCALHOST.getUrl()+":7654/api/train-model"),
    TEST_MODEL(HTTP.getUrl()+LOCALHOST.getUrl()+":7654/api/test-model"),
    CORR_MAT_IMG("http://10.0.0.47:7654/api/get-correlation-matrix-image?id="),
    GET_PRODUCT(HTTP.getUrl()+LOCALHOST.getUrl()+":1234/product-catalog-module/product/get-product/"),
    UPLOAD_FILE(HTTP.getUrl()+LOCALHOST.getUrl()+":"+PORT.getUrl()+"/product-catalog-module/product/upload-files");

    private final String url;

    UrlValues(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
