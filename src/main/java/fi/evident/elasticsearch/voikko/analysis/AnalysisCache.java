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

import org.puimula.libvoikko.Analysis;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

final class AnalysisCache {

    private final LRUCache<String, List<Analysis>> cache;
    private final ReentrantLock lock = new ReentrantLock(true);

    public AnalysisCache(int cacheSize) {
        cache = new LRUCache<String, List<Analysis>>(cacheSize);
    }

    public List<Analysis> get(String word) {
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

    public void put(String word, List<Analysis> result) {
        lock.lock();
        try {
            cache.put(word, result);
        } finally {
            lock.unlock();
        }
    }
}
