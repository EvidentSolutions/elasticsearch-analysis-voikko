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

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.puimula.libvoikko.Voikko;

import java.io.Closeable;

public final class VoikkoTokenFilterFactory extends AbstractTokenFilterFactory implements Closeable {

    private final VoikkoPool voikkoPool;

    private final AnalysisCache analysisCache;
    private final VoikkoTokenFilterConfiguration cfg = new VoikkoTokenFilterConfiguration();

    public VoikkoTokenFilterFactory(IndexSettings indexSettings,
                                    @SuppressWarnings("unused") Environment environment,
                                    String name,
                                    Settings settings) {
        super(indexSettings,  name, settings);

        cfg.analyzeAll = settings.getAsBoolean("analyzeAll", cfg.analyzeAll);
        cfg.minimumWordSize = settings.getAsInt("minimumWordSize", cfg.minimumWordSize);
        cfg.maximumWordSize = settings.getAsInt("maximumWordSize", cfg.maximumWordSize);

        analysisCache = new AnalysisCache(settings.getAsInt("analysisCacheSize", 1024));

        String language = settings.get("language", "fi_FI");
        String dictionaryPath = settings.get("dictionaryPath");

        for (String dir : settings.getAsList("libraryPath"))
            Voikko.addLibraryPath(dir);

        voikkoPool = new VoikkoPool(language, dictionaryPath);
        voikkoPool.setMaxSize(settings.getAsInt("poolMaxSize", 10));
    }

    @Override
    public void close() {
        voikkoPool.close();
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        try {
            return new VoikkoTokenFilter(tokenStream, voikkoPool, analysisCache, cfg);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
