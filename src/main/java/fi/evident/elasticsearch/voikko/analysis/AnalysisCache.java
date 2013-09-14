package fi.evident.elasticsearch.voikko.analysis;

import org.puimula.libvoikko.Analysis;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class AnalysisCache {

    private final LRUCache<String, List<Analysis>> cache = new LRUCache<String, List<Analysis>>(1024);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public List<Analysis> get(String word) {
        Lock l = lock.readLock();
        l.lock();
        try {
            return cache.get(word);
        } finally {
            l.unlock();
        }
    }

    public void put(String word, List<Analysis> result) {
        Lock l = lock.writeLock();
        l.lock();
        try {
            cache.put(word, result);
        } finally {
            l.unlock();
        }
    }
}
