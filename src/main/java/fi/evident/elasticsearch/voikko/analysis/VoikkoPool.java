/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fi.evident.elasticsearch.voikko.analysis;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.SuppressForbidden;
import org.puimula.libvoikko.Voikko;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

@SuppressForbidden(reason = "Migrating old code")
final class VoikkoPool {

    private final String language;
    private final String dictionaryPath;
    private int maxSize = 10;
    private int size = 0;
    private final List<Voikko> freeInstances = new ArrayList<>();
    private boolean closed = false;

    VoikkoPool(String language, String dictionaryPath) {
        this.language = language;
        this.dictionaryPath = dictionaryPath;
    }

    synchronized Voikko takeVoikko() throws InterruptedException {
        while (true) {
            if (closed)
                throw new IllegalStateException("Can't acquire Voikko from closed pool.");

            if (!freeInstances.isEmpty())
                return freeInstances.remove(freeInstances.size() - 1);

            if (size < maxSize) {
                Voikko voikko = createNewInstance();
                size++;
                return voikko;
            }

            wait();
        }
    }

    synchronized void release(Voikko voikko) {
        if (voikko == null) throw new IllegalArgumentException("null voikko");

        if (closed) {
            voikko.terminate();
        } else {
            freeInstances.add(voikko);
            notify();
        }
    }

    synchronized void close() {
        if (closed)
            return;

        closed = true;

        for (Voikko voikko : freeInstances)
            voikko.terminate();

        freeInstances.clear();

        notifyAll();
    }

    void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    private Voikko createNewInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new SpecialPermission());

        return AccessController.doPrivileged((PrivilegedAction<Voikko>) () -> {
            try {
                return new Voikko(language, dictionaryPath);
            } catch (UnsatisfiedLinkError e) {
                throw new VoikkoNativeLibraryNotFoundException(e);
            }
        });
    }
}
