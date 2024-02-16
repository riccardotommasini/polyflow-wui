package org.streamreasoning.gsp.seraph.engine;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.streamreasoning.gsp.seraph.syntax.SeraphQL;
import org.streamreasoning.gsp.seraph.syntax.SeraphVisitorImpl;
import org.streamreasoning.gsp.seraph.syntax.ThrowingErrorListener;
import org.streamreasoning.gsp.seraph.syntax.parser.SeraphLexer;
import org.streamreasoning.gsp.seraph.syntax.parser.SeraphParser;
import org.streamreasoning.rsp4j.api.querying.syntax.CaseChangingCharStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QueryFactory {

    static ThrowingErrorListener listener = ThrowingErrorListener.INSTANCE;

    public static SeraphQL parse(String queryString) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(queryString.getBytes());
        return parse(inputStream);
    }

    public static SeraphQL parse(InputStream inputStream) throws IOException {
        // Ignore case for keywords
        CaseChangingCharStream charStream = new CaseChangingCharStream(CharStreams.fromStream(inputStream), true);
        SeraphLexer lexer = new SeraphLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SeraphParser parser = new SeraphParser(tokens);
        parser.setErrorHandler(new DefaultErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        SeraphVisitorImpl visitor = new SeraphVisitorImpl();

        visitor.visit(parser.oC_Seraph());

        return visitor.getQuery();
    }
}
