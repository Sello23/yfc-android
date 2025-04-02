package com.yourfitness.coach.domain.facility

import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.classes
import com.yourfitness.coach.data.entity.distance
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.network.dto.FacilityDto
import com.yourfitness.common.data.TokenStorage
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.toSeconds
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.pt.data.dao.PtDao
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.models.TrainerCard
import com.yourfitness.pt.domain.pt.PtRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class FacilityRepository @Inject constructor(
    private val dao: FacilityDao,
    private val ptDao: PtDao,
    private val locationRepository: LocationRepository,
    private val restApi: YFCRestApi,
    private val ptRepository: PtRepository,
    private val preferencesStorage: PreferencesStorage,
    private val remoteConfig: FirebaseRemoteConfigRepository,
    private val profileRepository: ProfileRepository,
) {
    private val mutex = Mutex()


    suspend fun downloadFacilitiesData(forceUpdate: Boolean = false) {
        mutex.withLock {
//            if (preferencesStorage.facilitiesLoaded != -1 && !forceUpdate) return
            try {
                preferencesStorage.facilitiesLoaded = 0
                val response = restApi.facilities()
                syncFacilitiesWithServer(response.facilities)
                ptRepository.savePersonalTrainers(response.personaTrainers.orEmpty())
                ptRepository.downloadPtBalanceList()
                preferencesStorage.facilitiesLoaded = 1
            } catch (e: Exception) {
                preferencesStorage.facilitiesLoaded = -1
                Timber.e(e)
            }
        }
    }

    private suspend fun syncFacilitiesWithServer(facilitiesFromRemote: List<FacilityDto>?) {
        val localItems = dao.readAllFacilities()
        val serverItemIds = facilitiesFromRemote?.map { it.id }
        val localItemIds = localItems.map { it.id }

        // Find items to delete
        val itemsToDelete = localItemIds.filterNot { serverItemIds?.contains(it) == true }

        // Delete items from Room
        dao.deleteFacilities(itemsToDelete)

        // Insert or update items
        dao.saveFacilities(facilitiesFromRemote.orEmpty().map { it.toEntity() })
    }

    suspend fun fetchFacilities(): FacilityList {
        return dao.readAllFacilities()
    }

    private suspend fun getFacilities(classification: Classification): FacilityList {
        return dao.readAllFacilities().filterByClassification(classification)
    }

    suspend fun loadFacilities(
        classification: Classification,
        lastLocation: LatLng? = null
    ): FacilityList {
        val location = lastLocation ?: locationRepository.lastLocation()
        return getFacilities(classification).sortByLocation(location)
    }

    suspend fun searchFacilities(query: String, classification: Classification,): FacilityList {
        val normalizedQuery = query.trim().lowercase()
        return getFacilities(classification)
            .filter { it.name?.lowercase()?.contains(normalizedQuery) ?: false }
            .filterRestrictedData()
    }

    suspend fun searchPersonalTrainers(query: String): List<PersonalTrainerEntity> {
        return ptRepository.searchPersonalTrainers(query)
    }

    suspend fun getFilteredTrainers(filters: Filters, lastLocation: LatLng, classification: Classification): List<TrainerCard> {
        val facilities = getFacilities(classification)
            .filter { it.classification == Classification.GYM.value }
            .filterByLocation(filters.distance, lastLocation)
            .filterByFacilityName(filters.amenities)
        val trainers = ptRepository.getFilteredTrainers(
            filters.types,
            facilities.mapNotNull { it.personalTrainersIds }.flatten().distinct()
        )
        val data = trainers.map {
            TrainerCard(
                it.data.id.orEmpty(),
                it.data.fullName,
                it.data.mediaId.orEmpty(),
                it.data.focusAreas.orEmpty(),
                facilities.filter { item -> it.data.facilityIDs?.contains(item.id) == true }
                    .map { item -> Pair(item.name.orEmpty(), item.distance(lastLocation)) }
                    .sortedBy { dist -> dist.second },
                it.balance?.amount ?: 0,
                false
            )
        }.sortedByDescending { it.availableSessions }
        return data
    }

    suspend fun configureFilters(classification: Classification, filters: Filters = Filters()): Filters {
        val types = when (classification) {
            Classification.GYM -> FILTER_TYPES_GYM
            Classification.STUDIO -> FILTER_TYPES_STUDIO
            Classification.TRAINER -> ptRepository.getFocusAreas()
        }

        val amenities = when (classification) {
            Classification.GYM , Classification.STUDIO -> FILTER_AMENITIES
            Classification.TRAINER -> getFacilitiesWithPt()
        }
        val images = if (classification == Classification.TRAINER) getFacilityImagesWithPt() else null
        return filters.copy(allTypes = types, allAmenities = amenities, images = images)
    }

    suspend fun getFilteredFacilities(
        classification: Classification,
        filters: Filters,
        lastLocation: LatLng? = null
    ): FacilityList {
        val location = lastLocation ?: locationRepository.lastLocation()
        val types = filters.types.map { it.lowercase() }
        val savedFacilities = loadFacilities(classification, location)
        val facilities = savedFacilities.ifEmpty {
            downloadFacilitiesData(true)
            loadFacilities(classification, location)
        }
        return facilities
            .filter { item -> filters.distance == FILTERS_NO_LIMIT || item.distance(location) < filters.distance }
            .filter { item ->
                val itemTypes = item.types.orEmpty().addIf("Ladies Only") { item.femaleOnly ?: false }
                types.isEmpty() || itemTypes.any { it.lowercase() in types }
            }
            .filter { item -> filters.amenities.isEmpty() || item.amenities.orEmpty().any { it in filters.amenities } }
            .filterRestrictedData()
    }

    suspend fun getFilteredFacilitiesForPt(
        filters: Filters,
        lastLocation: LatLng? = null,
    ): FacilityList {
        val location = lastLocation ?: locationRepository.lastLocation()

        val savedFacilities = getFacilities(Classification.TRAINER)
        val actualFacilities = savedFacilities.ifEmpty {
            downloadFacilitiesData(true)
            getFacilities(Classification.TRAINER)
        }
        val facilities = actualFacilities
            .filterByLocation(filters.distance, location)
            .filterByFacilityName(filters.amenities)
        val ptItems = ptRepository.getFilteredTrainers(
            filters.types,
            facilities.mapNotNull { it.personalTrainersIds }.flatten().distinct()
        )
        val ptIds = ptItems.map { it.data.id }
        return facilities
            .filter { item ->
                ptItems.any { item.personalTrainersIds?.contains(it.data.id) == true }
            }
            .map {
                it.copy(personalTrainersIds = it.personalTrainersIds?.filter { id ->
                    ptIds.contains(id)
                })
            }
            .filterRestrictedData()
    }


    suspend fun getTrainersPerFacility(facility: FacilityEntity): List<TrainerCard> {
        val trainerCards = mutableListOf<TrainerCard>()
        try {
            val userTrainers = facility.personalTrainersIds
            userTrainers?.forEach {
                val ptInfo = ptDao.readPersonalTrainerById(it)
                val ptFacilities = getPtFacilities(it)
                if (ptInfo != null && ptFacilities.isNotEmpty()) {
                    trainerCards.add(TrainerCard(
                        ptInfo.id.orEmpty(),
                        ptInfo.fullName,
                        ptInfo.mediaId.orEmpty(),
                        ptInfo.focusAreas.orEmpty(),
                        ptFacilities.map { item ->
                            Pair(item.name, item.distance(locationRepository.lastLocation()))
                        }.sortedBy {dist -> dist.second },
                        0,
                        false
                    ))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return trainerCards.sortedByDescending { it.availableSessions }
    }


    suspend fun getFacilitiesFilteredByLocation(
        filters: Filters,
        classification: Classification,
    ): FacilityList {
        val location = locationRepository.lastLocation()
        return getFacilities(classification)
            .filterByLocation(filters.distance, location)
            .filterByFacilityName(filters.amenities)
    }

    private suspend fun getFacilitiesWithPt(): List<String> {
        return getFacilities(Classification.TRAINER).filter { it.personalTrainersIds?.isNotEmpty() == true }
            .mapNotNull { it.name }
            .distinct()
    }

    private suspend fun getFacilityImagesWithPt(): List<String> {
        return getFacilities(Classification.TRAINER).filter { it.personalTrainersIds?.isNotEmpty() == true }
            .map { it.gallery?.first().orEmpty() }
            .distinct()
    }

    private fun FacilityList.filterByClassification(classification: Classification): FacilityList {
        return filter {
            it.classification == classification.value ||
                    (classification == Classification.STUDIO && it.classes.isNotEmpty()) ||
                    (classification == Classification.TRAINER && it.classification == Classification.GYM.value)
        }
    }

    private fun FacilityList.sortByLocation(location: LatLng): FacilityList {
        return sortedBy { it.distance(location) }
    }

    private fun FacilityList.filterByLocation(distance: Int, location: LatLng): FacilityList {
        return filter { item -> distance == FILTERS_NO_LIMIT || item.distance(location) < distance }
    }

    private fun FacilityList.filterByFacilityName(facilityNames: List<String>): FacilityList {
        val names = facilityNames.map { it.lowercase() }
        return filter { facilityNames.isEmpty() || names.contains(it.name?.lowercase()) }
    }

    suspend fun getPtFacilities(ptId: String): List<FacilityEntity> {
        val facilityIds = ptRepository.getPtById(ptId)?.facilityIDs.orEmpty()
        return getFacilities(Classification.TRAINER).filter {
            it.classification == Classification.GYM.value && facilityIds.contains(it.id)
        }
    }

    private suspend fun FacilityList.filterRestrictedData(): FacilityList {
        val today = today().time.toSeconds()
        val profileRegistered = profileRepository.getProfile().createdAt ?: today
        val profileRegisteredMax = remoteConfig.restrictions.registeredAfter
        val start = remoteConfig.restrictions.start
        val end = remoteConfig.restrictions.end
        val restrictedIds = remoteConfig.restrictions.gymIds

        return if (profileRegistered >= profileRegisteredMax && today in start until end) {
            filter { !restrictedIds.contains(it.id) }
        } else {
            this
        }
    }

}

private fun <T> Iterable<T>.addIf(item: T, predicate: () -> Boolean): Iterable<T> {
    return if (predicate()) this + item else this
}

val FILTER_AMENITIES = listOf(
    "❄️ Cryo",
    "🌗 24/7",
    "👟 Padel",
    "💨 Steam",
    "📶 Wi-Fi",
    "🧖 Sauna",
    "🎾 Tennis",
    "🔴 Squash",
    "🛋️ Lounge",
    "🅿️ Parking",
    "🔑 Lockers",
    "🛁 Jacuzzi",
    "🤼‍♂️ MMA area",
    "🏀 Basketball",
    "👫 GX classes",
    "🙏 Prayer Room",
    "🤽 Indoor Pool",
    "🏊 Outdoor Pool",
    "🏋️ Spartan Zone",
    "🏃 Running Track",
    "🚴 Cycling Track",
    "🧺 Towel Service",
    "💆‍♀️ Sports Massage",
    "💦 Hydro theraphy",
    "🚪 Changing Rooms",
    "🧑‍🦽 Disabled Access",
    "🔌 Charging Station",
    "🌳 Outdoor workout space",
    "🧼 Complimentary Toiletries",
)

val FILTER_TYPES_GYM = listOf(
    "MMA",
    "CrossFit",
    "Commercial",
    "Functional",
    "BodyBuilding",
)

val FILTER_TYPES_STUDIO = listOf(
    "MMA",
    "Aqua",
    "BARE",
    "HIIT",
    "Yoga",
    "Boxing",
    "Circuit",
    "Cycling",
    "Dancing",
    "Pilates",
    "Bootcamp",
    "Boutique",
    "CrossFit",
    "Mobility",
    "Commercial",
    "Functional",
    "Gymnastics",
    "Ladies Only",
    "Personal Training"
)

typealias FacilityList = List<FacilityEntity>
