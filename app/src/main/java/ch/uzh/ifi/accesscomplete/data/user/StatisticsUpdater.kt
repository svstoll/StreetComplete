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

package ch.uzh.ifi.accesscomplete.data.user

import android.util.Log
import ch.uzh.ifi.countryboundaries.getIds
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.data.LatLon
import ch.uzh.ifi.accesscomplete.data.user.achievements.AchievementGiver
import java.util.*
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Named

/** Manages the updating of statistics, locally and pulling a complete update from backend  */
class StatisticsUpdater @Inject constructor(
    private val questStatisticsDao: QuestStatisticsDao,
    private val countryStatisticsDao: CountryStatisticsDao,
    private val achievementGiver: AchievementGiver,
    private val userStore: UserStore,
    private val statisticsDownloader: StatisticsDownloader,
    private val countryBoundaries: FutureTask<CountryBoundaries>,
    @Named("QuestAliases") private val questAliases: List<Pair<String, String>>
){
    fun addOne(questType: String, position: LatLon) {
        updateDaysActive()

        questStatisticsDao.addOne(questType)
        getRealCountryCode(position)?.let { countryStatisticsDao.addOne(it) }

        achievementGiver.updateQuestTypeAchievements(questType)
    }

    fun subtractOne(questType: String, position: LatLon) {
        updateDaysActive()
        questStatisticsDao.subtractOne(questType)
        getRealCountryCode(position)?.let { countryStatisticsDao.subtractOne(it) }
    }

    private fun getRealCountryCode(position: LatLon): String? =
        countryBoundaries.get().getIds(position).firstOrNull {
            // skip non-countries
            it != "FX" && it != "EU" &&
            // skip country subdivisions (f.e. US-TX)
            !it.contains('-')
        }

    private fun updateDaysActive() {
        val now = Date()
        val lastUpdateDate = Date(userStore.lastStatisticsUpdate)

        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal1.time = lastUpdateDate
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal2.time = now

        userStore.lastStatisticsUpdate = now.time
        if (!cal1.isSameDay(cal2)) {
            userStore.daysActive++
            achievementGiver.updateDaysActiveAchievements()
        }
    }

    fun updateFromBackend(userId: Long) {
        try {
            val statistics = statisticsDownloader.download(userId)
            val backendIsStillAnalyzing = statistics.isAnalyzing
            userStore.isSynchronizingStatistics = backendIsStillAnalyzing
            if (backendIsStillAnalyzing) {
                Log.i(TAG, "Backend is still analyzing changeset history")
                return
            }

            val backendDataIsUpToDate = statistics.lastUpdate / 1000 >= userStore.lastStatisticsUpdate / 1000
            if (!backendDataIsUpToDate) {
                Log.i(TAG, "Backend data is not up-to-date")
                return
            }

            val newQuestTypeStatistics = statistics.questTypes.toMutableMap()
            mergeQuestAliases(newQuestTypeStatistics)
            questStatisticsDao.replaceAll(newQuestTypeStatistics)
            countryStatisticsDao.replaceAll(statistics.countries)
            userStore.rank = statistics.rank
            userStore.daysActive = statistics.daysActive
            userStore.lastStatisticsUpdate = statistics.lastUpdate
            // when syncing statistics from server, any granted achievements should be
            // granted silently (without notification) because no user action was involved
            achievementGiver.updateAllAchievements(silent = true)
            achievementGiver.updateAchievementLinks()
        }  catch (e: Exception) {
            Log.w(TAG, "Unable to download statistics", e)
        }
    }

    private fun mergeQuestAliases(map: MutableMap<String, Int>)  {
        for ((oldName, newName) in questAliases) {
            val count = map[oldName]
            if (count != null) {
                map.remove(oldName)
                map[newName] = (map[newName] ?: 0) + count
            }
        }
    }

    companion object {
        private const val TAG = "StatisticsUpdater"
    }
}

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR) &&
    get(Calendar.YEAR) == other.get(Calendar.YEAR)
