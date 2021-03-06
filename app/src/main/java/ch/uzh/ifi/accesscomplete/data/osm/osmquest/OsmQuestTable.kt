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

package ch.uzh.ifi.accesscomplete.data.osm.osmquest

import ch.uzh.ifi.accesscomplete.data.osm.elementgeometry.ElementGeometryTable

object OsmQuestTable {
    const val NAME = "osm_quests"
    const val NAME_MERGED_VIEW = "osm_quests_full"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val QUEST_STATUS = "quest_status"
        const val TAG_CHANGES = "tag_changes"
        const val CHANGES_SOURCE = "changes_source"
        const val LAST_UPDATE = "last_update"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.QUEST_STATUS} varchar(255) NOT NULL,
            ${Columns.TAG_CHANGES} blob,
            ${Columns.CHANGES_SOURCE} varchar(255),
            ${Columns.LAST_UPDATE} int NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            CONSTRAINT same_osm_quest UNIQUE (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            ),
            CONSTRAINT element_key FOREIGN KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            ) REFERENCES ${ElementGeometryTable.NAME} (
                ${ElementGeometryTable.Columns.ELEMENT_TYPE},
                ${ElementGeometryTable.Columns.ELEMENT_ID}
            )
        );"""

    const val CREATE_VIEW = """
        CREATE VIEW $NAME_MERGED_VIEW
        AS SELECT * FROM $NAME
        INNER JOIN ${ElementGeometryTable.NAME}
        USING (
            ${ElementGeometryTable.Columns.ELEMENT_TYPE},
            ${ElementGeometryTable.Columns.ELEMENT_ID}
        );"""

    val ALL_COLUMNS_DB_VERSION_3 = listOf(
            Columns.QUEST_ID,
            Columns.QUEST_TYPE,
            Columns.ELEMENT_ID,
            Columns.ELEMENT_TYPE,
            Columns.QUEST_STATUS,
            Columns.TAG_CHANGES,
            Columns.LAST_UPDATE
    )

    const val CREATE_DB_VERSION_3 = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.QUEST_STATUS} varchar(255) NOT NULL,
            ${Columns.TAG_CHANGES} blob,
            ${Columns.LAST_UPDATE} int NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            CONSTRAINT same_osm_quest UNIQUE (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            ),
            CONSTRAINT element_key FOREIGN KEY (
                ${Columns.ELEMENT_TYPE}, ${Columns.ELEMENT_ID}
            ) REFERENCES ${ElementGeometryTable.NAME} (
                ${ElementGeometryTable.Columns.ELEMENT_TYPE},
                ${ElementGeometryTable.Columns.ELEMENT_ID}
            )
        );"""
}
