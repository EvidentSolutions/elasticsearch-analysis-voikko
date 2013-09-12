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

import org.puimula.libvoikko.Voikko;

import java.util.ArrayList;
import java.util.List;

final class VoikkoPool {

    private final String language;
    private final String dictionaryPath;
    private int maxSize = 10;
    private final List<Voikko> allInstances = new ArrayList<Voikko>();
    private final List<Voikko> freeInstances = new ArrayList<Voikko>();
    private boolean closed = false;

    public VoikkoPool(String language, String dictionaryPath) {
        this.language = language;
        this.dictionaryPath = dictionaryPath;
    }

    public synchronized Voikko takeVoikko() throws InterruptedException {
        while (true) {
            if (closed)
                throw new IllegalStateException("Can't acquire Voikko from closed pool.");

            if (!freeInstances.isEmpty())
                return freeInstances.remove(freeInstances.size() - 1);

            if (allInstances.size() < maxSize) {
                Voikko voikko = createNewInstance();
                allInstances.add(voikko);
                return voikko;
            }

            wait();
        }
    }

    public synchronized void release(Voikko voikko) {
        if (voikko == null) throw new IllegalArgumentException("null voikko");

        freeInstances.add(voikko);

        notifyAll();
    }

    public synchronized void close() {
        closed = true;

        for (Voikko voikko : allInstances)
            voikko.terminate();

        notifyAll();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    private Voikko createNewInstance() {
        try {
            return new Voikko(language, dictionaryPath);
        } catch (UnsatisfiedLinkError e) {
            throw new VoikkoNativeLibraryNotFoundException(e);
        }
    }
}
