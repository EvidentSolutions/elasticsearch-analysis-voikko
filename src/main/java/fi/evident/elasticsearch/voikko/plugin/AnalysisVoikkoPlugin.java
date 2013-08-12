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

package fi.evident.elasticsearch.voikko.plugin;

import fi.evident.elasticsearch.voikko.analysis.VoikkoAnalysisBinderProcessor;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisVoikkoPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-voikko";
    }

    @Override
    public String description() {
        return "Voikko analysis support";
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onModule(AnalysisModule module) {
        module.addProcessor(new VoikkoAnalysisBinderProcessor());
    }
}
