/*
 * Copyright 2014-2017 Evident Solutions Oy
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

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

/**
 * Factory that creates {@link FinnishTokenizer}.
 */
public class FinnishTokenizerFactory extends AbstractTokenizerFactory {

    public FinnishTokenizerFactory(IndexSettings indexSettings,
                                   @SuppressWarnings("unused") Environment environment,
                                   @SuppressWarnings("unused") String name, // TODO do we need name somewhere?
                                   Settings settings) {
        super(indexSettings, settings, name);
    }

    @Override
    public Tokenizer create() {
        return new FinnishTokenizer();
    }
}
