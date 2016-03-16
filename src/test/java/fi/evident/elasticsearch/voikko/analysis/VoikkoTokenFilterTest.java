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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VoikkoTokenFilterTest {

    private final ImmutableSettings.Builder settings = ImmutableSettings.builder();

    @Before
    public void initializeLibraryAndDictionaryPaths() {
        String voikkoPath = System.getProperty("voikko.path");

        File dictDirectory = null;
        String dictPath = System.getProperty("voikko.dict.path");
        if (dictPath != null) {
            dictDirectory = new File(dictPath);
        } else if (voikkoPath != null) {
            dictDirectory = new File(voikkoPath, "dicts");
        }

        if (dictDirectory == null)
            throw new AssumptionViolatedException("System property 'voikko.path' is not defined, add '-Dvoikko.path=/path/to/voikko'");

        File morphology = new File(dictDirectory, "2/mor-morpho/voikko-fi_FI.pro");
        if (!morphology.isFile())
            fail("morphology file " + morphology + " does not exist");

        settings.put("index.analysis.filter.myFilter.type", "voikko");

        if (voikkoPath != null)
            settings.put("index.analysis.filter.myFilter.libraryPath", voikkoPath);

        settings.put("index.analysis.filter.myFilter.dictionaryPath", dictDirectory.getAbsolutePath());
    }

    @Test
    public void defaultSettings() throws Exception {
        assertTokens("Testaan voikon taivutusta tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("taivutusta", "taivutus", 1),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1));
    }

    @Test
    public void unknownWord() throws Exception {
        assertTokens("Mitenkä foobarbaz edellinen sana tunnistetaan?",
                token("Mitenkä", "miten", 1),
                token("foobarbaz", "foobarbaz", 1),
                token("edellinen", "edellinen", 1),
                token("sana", "sana", 1),
                token("tunnistetaan", "tunnistaa", 1));
    }

    @Test
    public void allVariations() throws Exception {
        settings.put("index.analysis.filter.myFilter.analyzeAll", true);

        assertTokens("Testaan voikon taivutusta tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("voikon", "Voikko", 0),
                token("taivutusta", "taivutus", 1),
                token("taivutusta", "taivuttu", 0),
                token("taivutusta", "taivutus", 0),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1),
                token("yksinkertaisesti", "yksinkertainen", 0));
    }

    @Test
    public void nonSeparatedTokens() throws Exception {
        settings.put("index.analysis.filter.myFilter.analyzeAll", true);
        settings.put("index.analysis.filter.myFilter.separateTokens", false);

        assertTokens("Testaan voikon taivutusta tällä tavalla yksinkertaisesti.",
                token("Testaan", "testata", 1),
                token("voikon", "voikko", 1),
                token("voikon", "Voikko", 0),
                token("taivutusta", "taivutus", 1),
                token("taivutusta", "taivuttu", 0),
                token("taivutusta", "taivutus", 0),
                token("tällä", "tämä", 1),
                token("tavalla", "tapa", 1),
                token("yksinkertaisesti", "yksinkertainen", 1),
                token("yksinkertaisesti", "yksinkertainen", 0));
    }

    @Test
    public void compoundWords() {
        assertTokens("isoisälle", token("isoisälle", "isoisä", 1));
        assertTokens("tekokuusta keinokuuhun",
                token("tekokuusta", "tekokuusi", 1),
                token("keinokuuhun", "keinokuu", 1));
    }

    @Test
    public void compoundWordsWithHyphens() {
        assertTokens("rippi-isälle", token("rippi-isälle", "rippi-isä", 1));
    }

    private static TokenData token(String original, String token, int positionIncrement) {
        return new TokenData(original, token, positionIncrement);
    }

    private void assertTokens(String text, TokenData... expected) {
        List<TokenData> tokens = parse(text);
        assertEquals(asList(expected), tokens);
    }

    private List<TokenData> parse(String text) {
        try {
            TokenStream ts = createTokenStream(text);
            try {
                List<TokenData> result = new ArrayList<TokenData>();
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
            } finally {
                ts.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TokenStream createTokenStream(String text) {
        settings.put("index.version.created", "1");
        TokenFilterFactory filterFactory = createFilterFactory(settings.build());
        return filterFactory.create(new FinnishTokenizer(Version.LUCENE_4_10_4, new StringReader(text)));
    }

    private static TokenFilterFactory createFilterFactory(Settings settings) {
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings), new EnvironmentModule(new Environment(settings)), new IndicesAnalysisModule()).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class)).addProcessor(new VoikkoAnalysisBinderProcessor()))
                        .createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class).tokenFilter("myFilter");
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
