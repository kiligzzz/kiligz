package com.kiligz.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * lucene中实现同一位置放多个term的功能
 */
public class PositionTokenFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private String nextTerm = null;

    public PositionTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
//        if (nextTerm != null) {
//            termAtt.setEmpty().append(nextTerm);
//            posIncrAtt.setPositionIncrement(0);
//            nextTerm = null;
//            return true;
//        }
//
//        if (!input.incrementToken()) {
//            return false;
//        }
//
//        String term = termAtt.toString();
//
//        if (term.length() > 1) {
//            nextTerm = term.substring(1);
//            termAtt.setLength(1);
//            posIncrAtt.setPositionIncrement(0);
//        }
//        return true;
        return false;
    }

    /**
     * 注册PositionTokenFilter
     */
    public static class MyAnalyzer extends Analyzer {
        @Override
        protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
            Tokenizer tokenizer = new StandardTokenizer();
            TokenStream tokenStream = new PositionTokenFilter(tokenizer);
            return new TokenStreamComponents(tokenizer, tokenStream);
        }
    }
}