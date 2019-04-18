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

final class VoikkoTokenFilterConfiguration {

    /** If true, use analysis candidates returned by Voikko, otherwise use only the first result. */
    boolean analyzeAll = false;

    /** Words shorter than this threshold are ignored */
    int minimumWordSize = 3;

    /** Words longer than this threshold are ignored */
    int maximumWordSize = 100;

    /** If true, include parts of compound words as alternatives to the whole word */
    boolean expandCompounds = false;

    /** Subwords (parts of compound words) shorter than this treshold are ignored  */
    int minimumSubwordSize = 2;

    /** Subwords longer than this treshold are ignored */
    int maximumSubwordSize = 30;

}
