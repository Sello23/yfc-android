package com.yourfitness.coach.domain.progress.points

import android.os.Parcelable
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.common.data.dao.ProgressLevelDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.fitness_info.BackendFitnessDataRepository
import com.yourfitness.coach.domain.models.VisitedClass
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Bonuses
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.coach.network.dto.Visits
import com.yourfitness.common.network.CommonRestApi
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

class PointsManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val commonRestApi: CommonRestApi,
    private val preferencesStorage: PreferencesStorage,
    private val fitnessDataRepository: BackendFitnessDataRepository,
    private val facilityRepository: FacilityRepository,
    private val settingsManager: SettingsManager,
    private val progressLevelDao: ProgressLevelDao
) {

    suspend fun fetchProgress(): ProgressInfo {
        val totalPoints = fetchPoints()
        var levels = progressLevelDao.readLevels()
        if (levels.isEmpty()) {
            levels = commonRestApi.getProgressLevels()
            progressLevelDao.saveLevels(levels)
        }
        val curLevelIndex = levels.getCurrentLevel(totalPoints)
        val curLevel = levels[curLevelIndex]
        val nextLevel = levels.getNextLevel(totalPoints)
        var pointsToNextLevel: Long? = null
        var progress = 0
        if (nextLevel != null) {
            progress =
                (((totalPoints - curLevel.pointLevel).toDouble() / (nextLevel.pointLevel - curLevel.pointLevel)) * 100).toInt()
            pointsToNextLevel = nextLevel.pointLevel - totalPoints
        }
        return ProgressInfo(
            currentLevel = curLevel.name.orEmpty(),
            nextLevel = nextLevel?.name.orEmpty(),
            nextLevelReward = nextLevel?.coinRewards,
            nextLevelPoints = nextLevel?.pointLevel,
            totalPoints = totalPoints,
            isFirstLevel = curLevelIndex == 0,
            isLastLevel = nextLevel == null,
            progress = progress,
            levelLogoId = curLevel.mediaId.orEmpty(),
            pointsToNextLevel = pointsToNextLevel,
            levels = levels
        )
    }

    suspend fun updateLastPoints() {
        try {
            val totalPoints = fetchPoints()
            preferencesStorage.lastPoints = totalPoints
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

//    suspend fun fetchCreditRewards(visitsData: List<Visits>, bonuses: List<Bonuses>): List<CreditReward> {
//        val lastVisits = preferencesStorage.lastVisits
//        val visits = visitsData.fetchVisits()
//        val facilities = restApi.facilities().facilities ?: emptyList()
//        val rewards = visits.keys.mapNotNull { facilityId ->
//            val oldVisits = lastVisits.getOrDefault(facilityId, 0)
//            val oldLevel = bonuses.findBonusLevel(oldVisits) ?: Bonuses()
//            val newVisits = visits.getOrDefault(facilityId, 0)
//            val newLevel = bonuses.findBonusLevel(newVisits) ?: Bonuses()
//            if (oldLevel.name != newLevel.name && newLevel.name != null) {
//                val facility = facilities.find { it.id == facilityId } ?: FacilityDto()
//                val credits = newLevel.credits?.toIntOrNull() ?: 0
//                val name = newLevel.name ?: "Unknown"
//                val isFirst = newLevel.name == "Bronze"
//                CreditReward(credits, newVisits, name, isFirst, facility.toEntity())
//            } else {
//                null
//            }
//        }
//        preferencesStorage.lastVisits = visits
//        return rewards
//    }

    private fun List<Bonuses>.transformBonuses(): List<Bonuses> {
        var totalAmount = 0
        return this.map {
            val amount = it.amount?.toIntOrNull() ?: 0
            val newAmount = (totalAmount + amount).toString()
            totalAmount += amount
            it.copy(amount = newAmount)
        }
    }

    private fun List<Bonuses>.findBonusLevel(visits: Int): Bonuses? {
        return reversed().find {
            val amount = it.amount?.toIntOrNull() ?: 0
            return@find visits >= amount
        }
    }

    fun List<Visits>.fetchVisits(): Map<String, Int> {
        return groupBy { it.facilityID }
            .filterKeys { it != null }
            .entries.associate { it.key!! to it.value.size }
    }

    suspend fun fetchPoints(): Long = fitnessDataRepository.getPointsSum().toLong()

    suspend fun getVisitedClasses(visits: List<Visits>, settingsGlobal: SettingsEntity?): Pair<List<VisitedClass>, Int> {
        var maxVisits = 0
        val groupVisits = visits.sortedBy { it.createdAt }.reversed().groupBy { it.facilityID }
        val facility = facilityRepository.loadFacilities(Classification.STUDIO)
        val bonusesCredits = restApi.getBonusCredits()
        val visitedFacility = mutableListOf<FacilityEntity>()
        groupVisits.forEach { visit ->
            val filteredFacility = facility.firstOrNull { it.id == visit.key }
            if (filteredFacility != null) {
                visitedFacility.add(filteredFacility)
                if (maxVisits < visit.value.size) maxVisits = visit.value.size
            }
        }
        val visitedClass = mutableListOf<VisitedClass>()
        visitedFacility.forEach { facilityEntity ->
            var visitsCount = 0
            groupVisits.map {
                if (facilityEntity.id == it.key) {
                    visitsCount = it.value.size
                }
            }
            var currentLevel = "Visitor"
            var visitsToNextLevel = 1
            var creditsLevelUp = 0
            var studioNextLevel = ""
            var color = ""
            var studioNextLevelColor = ""
            val bonusCredits = bonusesCredits.firstOrNull { it.facilityID == facilityEntity.id }
            run loop@{
                settingsGlobal?.bonuses?.forEachIndexed { index, bonus ->
                    if (visitsCount < (settingsGlobal.bonuses.first().amount?.toInt() ?: 0)) {
                        currentLevel = "Visitor"
                        visitsToNextLevel = bonus.amount?.toInt() ?: 0
                        creditsLevelUp = bonus.credits?.toInt() ?: 0
                        color = "#FF4C8AFF"
                        studioNextLevel = bonus.name ?: ""
                        studioNextLevelColor = bonus.color ?: ""
                        return@loop
                    }
                    if (visitsCount < (bonus.amount?.toInt() ?: 0)) {
                        currentLevel = settingsGlobal.bonuses[index - 1].name ?: ""
                        visitsToNextLevel = bonus.amount?.toInt() ?: 0
                        creditsLevelUp = bonus.credits?.toInt() ?: 0
                        color = settingsGlobal.bonuses[index - 1].color ?: ""
                        studioNextLevel = bonus.name ?: ""
                        studioNextLevelColor = bonus.color ?: ""
                        return@loop
                    }
                    if (visitsCount >= (settingsGlobal.bonuses.last().amount?.toInt() ?: 0)) {
                        currentLevel = settingsGlobal.bonuses.last().name ?: ""
                        visitsToNextLevel = 1
                        creditsLevelUp = 0
                        color = settingsGlobal.bonuses.last().color ?: ""
                        studioNextLevel = ""
                        studioNextLevelColor = settingsGlobal.bonuses.last().color ?: ""
                        return@loop
                    }
                }
            }
            val availableBonusCredits = if (bonusCredits?.amount == null) {
                0.toString()
            } else {
                bonusCredits.amount.toString()
            }
            val progressbarPosition = (visitsCount * 100) / visitsToNextLevel
            val ringProgressText = visitsCount.toDouble() / visitsToNextLevel.toDouble() * 100
            visitedClass.add(
                VisitedClass(
                    studioIcon = facilityEntity.icon ?: "",
                    studioCurrentLevel = currentLevel,
                    studioName = facilityEntity.name ?: "",
                    currentVisits = visitsCount.toString(),
                    visitsToNextLevel = visitsToNextLevel.toString(),
                    studioNextLevel = studioNextLevel,
                    creditsLevelUpBonus = creditsLevelUp.toString(),
                    availableBonusCredits = availableBonusCredits,
                    progressbarPosition = progressbarPosition,
                    facilityId = facilityEntity.id ?: "",
                    color = color,
                    nextLevelColor = studioNextLevelColor,
                    ringProgress = ringProgressText.toInt().toString(),
                    facilityEntity = facilityEntity,
                )
            )
        }
        return visitedClass to maxVisits
    }

    suspend fun fetchPointsCountData(): Pair<Pair<Double, Double>, Pair<Long, Long>> {
        val settings = settingsManager.getSettings()
        return ((settings?.pointsPerStep ?: 0.0) to (settings?.pointsPerCalorie ?: 0.0)) to
                ((settings?.pointLevel ?: 0L) to (settings?.rewardLevel ?: 0L))
    }

    private fun List<ProgressLevel>.getCurrentLevel(points: Long): Int {
        return indexOfLast { points >= it.pointLevel }
    }

    private fun List<ProgressLevel>.getNextLevel(points: Long): ProgressLevel? {
        return firstOrNull { points < it.pointLevel }
    }
}

@Parcelize
data class ProgressInfo(
    val currentLevel: String,
    val nextLevel: String,
    val nextLevelReward: Long?,
    val nextLevelPoints: Long?,
    val totalPoints: Long,
    val isFirstLevel: Boolean,
    val isLastLevel: Boolean,
    val progress: Int,
    val levelLogoId: String,
    val pointsToNextLevel: Long?,
    val levels: List<ProgressLevel>,
) : Parcelable
