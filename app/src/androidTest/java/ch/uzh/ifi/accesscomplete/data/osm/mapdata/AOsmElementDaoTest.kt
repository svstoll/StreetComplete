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

package ch.uzh.ifi.accesscomplete.data.osm.mapdata

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.uzh.ifi.accesscomplete.data.ObjectRelationalMapping
import ch.uzh.ifi.accesscomplete.data.osm.osmquest.OsmQuestTable
import de.westnordost.osmapi.map.data.Element
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class AOsmElementDaoTest {

    private lateinit var dao: TestOsmElementDao
    private lateinit var dbHelper: SQLiteOpenHelper

    @Before fun setUpHelper() {
        dbHelper = TestDbHelper(getInstrumentation().targetContext)
        dao = TestOsmElementDao(dbHelper)
    }

    @After fun tearDownHelper() {
        dbHelper.close()
        getInstrumentation().targetContext.deleteDatabase(TESTDB)
    }

    @Test fun putGet() {
        dao.put(createElement(6, 1))
        assertEquals(6, dao.get(6)!!.id)
        assertEquals(1, dao.get(6)!!.version)
    }

    @Test fun putAll() {
        dao.putAll(listOf(createElement(1, 2), createElement(2, 2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun putOverwrite() {
        dao.put(createElement(6, 0))
        dao.put(createElement(6, 5))
        assertEquals(5, dao.get(6)!!.version)
    }

    @Test fun getNull() {
        assertNull(dao.get(6))
    }

    @Test fun delete() {
        dao.put(createElement(6, 0))
        dao.delete(6)
        assertNull(dao.get(6))
    }
}

private fun createElement(id: Long, version: Int): Element {
    val element = mock(Element::class.java)
    `when`(element.id).thenReturn(id)
    `when`(element.version).thenReturn(version)
    return element
}

private const val TABLE_NAME = "test"
private const val ID_COL = "id"
private const val VERSION_COL = "version"

private const val TESTDB = "testdb.db"

private class TestOsmElementDao(dbHelper: SQLiteOpenHelper) : AOsmElementDao<Element>(dbHelper) {

    override val elementTypeName = Element.Type.NODE.name
    override val tableName = TABLE_NAME
    override val idColumnName = ID_COL
    override val mapping = object : ObjectRelationalMapping<Element> {
        override fun toContentValues(obj: Element) = contentValuesOf(
            ID_COL to obj.id,
            VERSION_COL to obj.version
        )

        override fun toObject(cursor: Cursor) = createElement(cursor.getLong(0), cursor.getInt(1))
    }
}

private class TestDbHelper(context: Context) : SQLiteOpenHelper(context, TESTDB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // the AOsmElementDao is tied to the quest table... but we only need the id and type
        db.execSQL(
            "CREATE TABLE " + OsmQuestTable.NAME + " (" +
                OsmQuestTable.Columns.ELEMENT_ID + " int            NOT NULL, " +
                OsmQuestTable.Columns.ELEMENT_TYPE + " varchar(255)    NOT NULL " +
                ");"
        )
        db.execSQL(
            "INSERT INTO " + OsmQuestTable.NAME + " (" +
                OsmQuestTable.Columns.ELEMENT_ID + ", " +
                OsmQuestTable.Columns.ELEMENT_TYPE + ") VALUES " +
                "(1, \"" + Element.Type.NODE.name + "\");"
        )

        db.execSQL(
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    ID_COL + " int PRIMARY KEY, " +
                    VERSION_COL + " int);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}
