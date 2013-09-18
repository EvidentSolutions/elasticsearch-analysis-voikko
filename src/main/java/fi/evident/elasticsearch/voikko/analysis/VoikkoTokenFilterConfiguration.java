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

final class VoikkoTokenFilterConfiguration {

    /** If true, use analysis candidates returned by Voikko, otherwise use only the first result. */
    boolean analyzeAll = false;

    /** Words shorter than this threshold are ignored */
    int minimumWordSize = 3;

    /** Words longer than this threshold are ignored */
    int maximumWordSize = 100;

}
