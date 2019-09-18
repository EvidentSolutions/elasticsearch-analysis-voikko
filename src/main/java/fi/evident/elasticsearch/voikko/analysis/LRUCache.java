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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A bounded cache that evicts least recently used items when it becomes full.
 */
final class LRUCache<K,V> extends LinkedHashMap<K,V> {

    private final int maxSize;
    private static final float LOAD_FACTOR = 0.75F;

    LRUCache(int maxSize) {
        super(maxSize + 1, LOAD_FACTOR, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxSize;
    }
}
