// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.lucene;

import com.github.fommil.utils.GuruMeditationFailure;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

/**
 * Convenience methods for using the Lucene
 * <del>Labyrinth</del> <ins>libraries</ins> for the simplest of use cases.
 *
 * @author Samuel Halliday
 * @see <a href="http://lucene.apache.org">Lucene</a>
 */
public final class Lucene {

    private static final ThreadLocal<SnowballProgram> stemmers = new ThreadLocal<SnowballProgram>() {
        @Override
        protected SnowballProgram initialValue() {
            return new EnglishStemmer();
        }
    };

    private static final List<String> COMMON_ENGLISH_WORDS = Lists.newArrayList("a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and",
            "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear",
            "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has",
            "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it",
            "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my",
            "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own",
            "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the",
            "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us",
            "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why",
            "will", "with", "would", "yet", "you", "your");

    private static final Set<?> ENGLISH_STOP_WORDS;

    private static final Set<?> ENGLISH_STEMMED_STOP_WORDS;

    static {
        ENGLISH_STOP_WORDS = StopFilter.makeStopSet(Version.LUCENE_36, COMMON_ENGLISH_WORDS, true);
        List<String> stemmed = Lists.newArrayList();
        for (String word : COMMON_ENGLISH_WORDS) {
            stemmed.add(stem(word));
        }
        ENGLISH_STEMMED_STOP_WORDS = StopFilter.makeStopSet(Version.LUCENE_36, stemmed, true);
    }

    /**
     * Uses a hard-coded stopword list and a standard {@link Tokenizer} to break
     * up the input and remove commonly used words (tokens are stemmed before
     * being compared to the stopwords).
     * 
     * @param text
     * @return
     */
    public static String removeStopWords(String text) {
        StringReader reader = new StringReader(Preconditions.checkNotNull(text));
        Tokenizer tokeniser = new StandardTokenizer(Version.LUCENE_36, reader);

        FilteringTokenFilter filter = new FilteringTokenFilter(true, tokeniser) {
            private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

            @Override
            protected boolean accept() throws IOException {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                String stemmed = stem(word);
                return !ENGLISH_STEMMED_STOP_WORDS.contains(stemmed);
            }
        };
        List<String> words = Lists.newArrayList();
        try {
            while (filter.incrementToken()) {
                String word = filter.getAttribute(CharTermAttribute.class).toString();
                words.add(word);
            }
        } catch (IOException ex) {
            throw new GuruMeditationFailure();
        }
        return Joiner.on(" ").join(words);
    }

    /**
     * @param word
     * @return
     */
    public static String stem(String word) {
        SnowballProgram stemmer = stemmers.get();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
