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

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

final class AnalysisCache {

    private final LRUCache<String, List<String>> cache;
    private final ReentrantLock lock = new ReentrantLock(true);

    public AnalysisCache(int cacheSize) {
        cache = new LRUCache<String, List<String>>(cacheSize);
    }

    public List<String> get(String word) {
        // Note that it seems that we could use a read/write -lock here and grab only the read-lock
        // when retrieving stuff from cache, but this will not work because the cache uses access-order,
        // meaning that every read will actually mutate the cache.
        lock.lock();
        try {
            return cache.get(word);
        } finally {
            lock.unlock();
        }
    }

    public void put(String word, List<String> result) {
        lock.lock();
        try {
            cache.put(word, result);
        } finally {
            lock.unlock();
        }
    }
}
