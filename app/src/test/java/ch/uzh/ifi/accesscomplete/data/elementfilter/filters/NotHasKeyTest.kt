/*
 * AccessComplete, an easy to use editor of accessibility related
 * OpenStreetMap data for Android.  This program is a fork of
 * StreetComplete (https://github.com/westnordost/StreetComplete).
 *
 * Copyright (C) 2016-2020 Tobias Zwick and contributors (StreetComplete authors)
 * Copyright (C) 2020 Sven Stoll (AccessComplete author)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.uzh.ifi.accesscomplete.data.elementfilter.filters

import ch.uzh.ifi.accesscomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class NotHasKeyTest {

    @Test fun matches() {
        val f = NotHasKey("name")

        assertFalse(f.matches(mapOf("name" to "yes")))
        assertFalse(f.matches(mapOf("name" to "no")))
        assertTrue(f.matches(mapOf("neme" to "no")))
        assertTrue(f.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("[!name]", NotHasKey("name").toOverpassQLString())
        assertEquals("[!'name:old']", NotHasKey("name:old").toOverpassQLString())
    }
}
