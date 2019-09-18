/*
 * Copyright 2013-2017 Evident Solutions Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package fi.evident.elasticsearch.voikko.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.puimula.libvoikko.Analysis;
import org.puimula.libvoikko.Voikko;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

final class VoikkoTokenFilter extends TokenFilter {

    private State current;
    private final VoikkoPool pool;
    private final Voikko voikko;
    private final VoikkoTokenFilterConfiguration cfg;

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

    private final Deque<String> alternatives = new ArrayDeque<>();
    private final AnalysisCache analysisCache;

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("[a-zA-ZåäöÅÄÖ-]+");

    VoikkoTokenFilter(TokenStream input,
                      VoikkoPool pool,
                      AnalysisCache analysisCache,
                      VoikkoTokenFilterConfiguration cfg) throws InterruptedException {
        super(input);
        this.pool = pool;
        this.voikko = pool.takeVoikko();
        this.analysisCache = analysisCache;
        this.cfg = cfg;
    }

    @Override
    public void close() throws IOException {
        super.close();
        pool.release(voikko);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!alternatives.isEmpty()) {
            outputAlternative(alternatives.removeFirst());
            return true;
        }

        if (input.incrementToken()) {
            analyzeToken();
            return true;
        }

        return false;
    }

    private void analyzeToken() {
        if (!isCandidateForAnalysis(charTermAttribute))
            return;

        List<String> baseForms = analyze(charTermAttribute);
        if (baseForms.isEmpty())
            return;

        charTermAttribute.setEmpty().append(baseForms.get(0));

        if (cfg.analyzeAll && baseForms.size() > 1) {
            current = captureState();

            alternatives.addAll(baseForms.subList(1, baseForms.size()));
        }
    }

    private List<String> analyze(CharSequence wordSeq) {
        String word = wordSeq.toString();
        List<String> result = analysisCache.get(word);
        if (result == null) {
            result = analyzeUncached(word);
            analysisCache.put(word, result);
        }
        return result;
    }

    private List<String> analyzeUncached(String word) {
        List<Analysis> results = voikko.analyze(word);
        List<String> baseForms = new ArrayList<>(results.size());

        for (Analysis result : results) {
            String baseForm = result.get("BASEFORM");
            if (baseForm != null)
                baseForms.add(baseForm);
        }
        return baseForms;
    }

    private void outputAlternative(String token) {
        restoreState(current);

        positionIncrementAttribute.setPositionIncrement(0);
        charTermAttribute.setEmpty().append(token);
    }

    private boolean isCandidateForAnalysis(CharSequence word) {
        return word.length() >= cfg.minimumWordSize && word.length() <= cfg.maximumWordSize && VALID_WORD_PATTERN.matcher(word).matches();
    }
}
