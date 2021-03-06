package io.aime.parse;

public class ParserNotFound extends ParseException {

    private String url;
    private String contentType;

    public ParserNotFound(String message) {
        super(message);
    }

    public ParserNotFound(String url, String contentType) {
        this(url, contentType, "parser not found for contentType=" + contentType + " url=" + url);
    }

    public ParserNotFound(String url, String contentType, String message) {
        super(message);
        this.url = url;
        this.contentType = contentType;
    }

    public String getUrl() {
        return this.url;
    }

    public String getContentType() {
        return this.contentType;
    }
}
