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
