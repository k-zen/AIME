package io.aime.parse;

// AIME
import io.aime.protocol.Content;

// Util
import java.util.concurrent.Callable;

class ParseCallable implements Callable<ParseResult> {

    private Parser p;
    private Content content;

    public ParseCallable(Parser p, Content content) {
        this.p = p;
        this.content = content;
    }

    @Override
    public ParseResult call() throws Exception {
        return p.getParseResult(content);
    }
}
