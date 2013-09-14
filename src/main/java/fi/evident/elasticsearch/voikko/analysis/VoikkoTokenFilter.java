/*
 * Copyright 2013 Evident Solutions Oy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with this program. If not, see <​http://www.gnu.org/licenses/>.
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

    private final Deque<String> alternatives = new ArrayDeque<String>();
    private final LRUCache<String, List<Analysis>> analyzationCache = new LRUCache<String, List<Analysis>>(1024);

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("[a-zA-ZåäöÅÄÖ]+");

    VoikkoTokenFilter(TokenStream input, VoikkoPool pool, VoikkoTokenFilterConfiguration cfg) throws InterruptedException {
        super(input);
        this.pool = pool;
        this.voikko = pool.takeVoikko();
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
        if (!isCandidateForAnalyzation(charTermAttribute))
            return;

        List<Analysis> analyzationResults = analyze(charTermAttribute);
        if (analyzationResults.isEmpty())
            return;

        current = captureState();

        String firstBaseForm = baseForm(analyzationResults.get(0));
        if (firstBaseForm != null)
            charTermAttribute.setEmpty().append(firstBaseForm);

        if (cfg.analyzeAll) {
            for (Analysis analysis : analyzationResults.subList(1, analyzationResults.size())) {
                String baseForm = baseForm(analysis);
                if (baseForm != null)
                    alternatives.add(baseForm);
            }
        }
    }

    private List<Analysis> analyze(CharSequence wordSeq) {
        String word = wordSeq.toString();
        List<Analysis> result = analyzationCache.get(word);
        if (result == null) {
            result = voikko.analyze(word);
            analyzationCache.put(word, result);
        }
        return result;
    }

    private void outputAlternative(String token) {
        restoreState(current);

        positionIncrementAttribute.setPositionIncrement(0);
        charTermAttribute.setEmpty().append(token);
    }

    private boolean isCandidateForAnalyzation(CharSequence word) {
        return word.length() >= cfg.minimumWordSize && word.length() <= cfg.maximumWordSize && VALID_WORD_PATTERN.matcher(word).matches();
    }

    private static String baseForm(Analysis analysis) {
        return analysis.get("BASEFORM");
    }
}
