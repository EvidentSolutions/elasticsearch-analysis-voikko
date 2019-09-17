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

import fi.evident.elasticsearch.voikko.plugin.AnalysisVoikkoPlugin;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class VoikkoTokenFilterTests extends ESTestCase {

    private final Settings.Builder settings = Settings.builder();

    @SuppressWarnings("Convert2Lambda")
    @Before
    public void initializeLibraryAndDictionaryPaths() {
        String voikkoPath = System.getProperty("voikko.path");

        Path dictDirectory;
        String dictPath = System.getProperty("voikko.dict.path");
        if (isDefined(dictPath)) {
            dictDirectory = PathUtils.get(dictPath);
        } else if (isDefined(voikkoPath)) {
            dictDirectory = PathUtils.get(voikkoPath, "dicts");
        } else {
            assumeTrue("System property 'voikko.path' is not defined, add '-Dvoikko.path=/path/to/voikko'", false);
            return;
        }

        Path morphology = dictDirectory.resolve("2/mor-morpho/voikko-fi_FI.pro");

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            @SuppressForbidden(reason = "We really want to access real file system")
            public Object run() {
                if (!morphology.toFile().isFile())
                    fail("morphology file " + morphology + " does not exist");
                return null;
            }
        });

        settings.put("index.analysis.filter.myFilter.type", "voikko");

        if (voikkoPath != null)
            settings.put("index.analysis.filter.myFilter.libraryPath", voikkoPath);

        settings.put("index.analysis.filter.myFilter.dictionaryPath", dictDirectory.toAbsolutePath());
    }

    public void testDefaultSettings() throws Exception {
        assertTokens("Testaan voikon analyysiä tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("analyysiä", "analyysi", 1),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1));
    }

    public void testUnknownWord() throws Exception {
        assertTokens("Mitenkä foobarbaz edellinen sana tunnistetaan?",
                token("Mitenkä", "miten", 1),
                token("foobarbaz", "foobarbaz", 1),
                token("edellinen", "edellinen", 1),
                token("sana", "sana", 1),
                token("tunnistetaan", "tunnistaa", 1));
    }

    public void testAllVariations() throws Exception {
        settings.put("index.analysis.filter.myFilter.analyzeAll", true);

        assertTokens("Testaan voikon analyysiä tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("voikon", "Voikko", 0),
                token("analyysiä", "analyysi", 1),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1),
                token("yksinkertaisesti", "yksinkertainen", 0));
    }

    public void testNonSeparatedTokens() throws Exception {
        settings.put("index.analysis.filter.myFilter.analyzeAll", true);
        settings.put("index.analysis.filter.myFilter.separateTokens", false);

        assertTokens("Testaan voikon analyysiä tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("voikon", "Voikko", 0),
                token("analyysiä", "analyysi", 1),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1),
                token("yksinkertaisesti", "yksinkertainen", 0));
    }

    public void testCompoundWords() {
        assertTokens("isoisälle", token("isoisälle", "isoisä", 1));
        assertTokens("tekokuulla keinokuuhun",
                token("tekokuulla", "tekokuu", 1),
                token("keinokuuhun", "keinokuu", 1));
    }

    public void testCompoundWordsWithHyphens() {
        assertTokens("rippi-isälle", token("rippi-isälle", "rippi-isä", 1));
    }

    private static TokenData token(String original, String token, int positionIncrement) {
        return new TokenData(original, token, positionIncrement);
    }

    private void assertTokens(String text, TokenData... expected) {
        List<TokenData> tokens = parse(text);
        assertEquals(asList(expected), tokens);
    }

    private static boolean isDefined(String value) {
        return value != null && value.length() != 0;
    }

    private List<TokenData> parse(String text) {
        NamedAnalyzer analyzer = getAnalysisService().indexAnalyzers.get("test");

        try {
            try (TokenStream ts = analyzer.tokenStream("test", new StringReader(text))) {
                List<TokenData> result = new ArrayList<>();
                CharTermAttribute charTerm = ts.addAttribute(CharTermAttribute.class);
                OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
                PositionIncrementAttribute position = ts.addAttribute(PositionIncrementAttribute.class);
                ts.reset();
                while (ts.incrementToken()) {
                    String original = text.substring(offset.startOffset(), offset.endOffset());
                    result.add(token(original, charTerm.toString(), position.getPositionIncrement()));
                }
                ts.end();

                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TestAnalysis getAnalysisService() {
        try {
            Settings indexSettings = Settings.builder()
                    .put(settings.build())
                    .put("index.analysis.analyzer.test.type", "custom")
                    .put("index.analysis.analyzer.test.tokenizer", "finnish")
                    .putList("index.analysis.analyzer.test.filter", "lowercase", "myFilter")
                    .build();

            return createTestAnalysis(new Index("test", "_na_"), indexSettings, new AnalysisVoikkoPlugin());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class TokenData {

        private final String original;
        private final String token;
        private final int positionIncrement;

        public TokenData(String original, String token, int positionIncrement) {
            this.original = original;
            this.token = token;
            this.positionIncrement = positionIncrement;
        }

        @Override
        public String toString() {
            return original + " -> " + token + " (+" + positionIncrement + ')';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            TokenData tokenData = (TokenData) obj;

            return positionIncrement == tokenData.positionIncrement
                && original.equals(tokenData.original)
                && token.equals(tokenData.token);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * original.hashCode() + token.hashCode()) + positionIncrement;
        }
    }
}
