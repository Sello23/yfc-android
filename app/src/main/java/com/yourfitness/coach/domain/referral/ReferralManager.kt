package com.yourfitness.coach.domain.referral

import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.AccessCode
import com.yourfitness.coach.network.dto.CodeType
import com.yourfitness.coach.network.dto.CorporationVoucherBody
import com.yourfitness.coach.network.dto.ReferralVoucher
import com.yourfitness.coach.network.dto.ReferralVoucherBody
import com.yourfitness.coach.network.dto.SubscriptionVoucherBody
import com.yourfitness.coach.network.dto.Voucher
import com.yourfitness.coach.network.dto.toErrorResponse
import com.yourfitness.common.domain.ProfileRepository
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class ReferralManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val preferencesStorage: PreferencesStorage,
    private val profileRepository: ProfileRepository,
    private val tokenManager: ProfileManager
) {

    suspend fun fetchAppliedVoucher(): Voucher? {
        return restApi.getSubscriptionPrice().voucher
    }

    suspend fun fetchVoucher(): Voucher {
        val code = profileRepository.voucher()
        return restApi.getVoucher(code)
    }

    fun isVoucherReward(voucher: Voucher): Boolean {
        val lastVoucherRewards = preferencesStorage.lastVoucherRewards
        val userRewardAmount = voucher.userRewardAmount ?: 0
        preferencesStorage.lastVoucherRewards = userRewardAmount
        return userRewardAmount > lastVoucherRewards
    }

    suspend fun checkVoucherCode(code: String): Pair<VoucherResult, ReferralVoucher?> {
        return try {
            val response = restApi.checkCorporationCode(code)
            val resultCode = applyCode(response, code, true)
            resultCode to response.referral
        } catch (e: Exception) {
            Timber.e(e)
            VoucherResult.ERROR to null
        }
    }

    suspend fun checkCorporateCode(code: String, voucherOnly: Boolean = false): Pair<VoucherResult, List<String>> {
        val resultList = mutableListOf<String>()
        return try {
            val response = restApi.checkCorporationCode(code)
            val result = applyCode(response, code, voucherOnly)
            when (result) {
                VoucherResult.CHALLENGE_SUCCESS -> {
                    resultList.add(response.challenge?.name.orEmpty())
                }

                VoucherResult.CORPORATE_NEW_RATE_SUCCESS, VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS -> {
                    resultList.add(response.corporation?.name.orEmpty())
                    resultList.add(response.corporation?.subscriptionCost.orEmpty())
                    resultList.add(response.corporation?.subscriptionCurrency.orEmpty())
                }

                VoucherResult.CORPORATE_COMPLIMENTARY_SUCCESS -> {
                    resultList.add(response.corporation?.name.orEmpty())
                }

                VoucherResult.REFERRAL_SUCCESS -> {
                    resultList.add((response.referral?.userRewardAmount ?: 0).toString())
                }

                VoucherResult.INFLUENCER_DISCOUNT -> {
                    resultList.add((response.referral?.subscriptionCost ?: 0).toString())
                    resultList.add((response.referral?.subscriptionCurrency ?: 0).toString())
                }

                VoucherResult.SUCCESS -> {
                    resultList.add((response.referral?.userRewardAmount ?: 0).toString())
                }

                else -> {}
            }

            result to resultList
        } catch (e: Exception) {
            Timber.e(e)
            VoucherResult.ERROR to resultList
        }
    }

    private suspend fun applyCode(
        response: CodeType,
        code: String,
        voucherOnly: Boolean = false
    ): VoucherResult {
        return when (response.type) {
            VoucherType.CORPORATION.value -> {
//                if (voucherOnly) return VoucherResult.ERROR
                val corpId = response.corporation?.id ?: return VoucherResult.ERROR
                return try {
                    restApi.applyCorporationVoucher(CorporationVoucherBody(corpId))
                    tokenManager.refreshToken()
                    when (response.corporation.subscriptionType) {
                        VoucherResult.CORPORATE_NEW_RATE_SUCCESS.value -> {
                            VoucherResult.CORPORATE_NEW_RATE_SUCCESS
                        }
                        VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS.value -> {
                            VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS
                        }
                        else -> {
                            VoucherResult.CORPORATE_COMPLIMENTARY_SUCCESS
                        }
                    }
                } catch (e: Exception) {
                    VoucherResult.ERROR
                }
            }

            VoucherType.CHALLENGE.value -> {
                if (voucherOnly) return VoucherResult.ERROR
                val challengeId = response.challenge?.id ?: return VoucherResult.ERROR
                return try {
                    restApi.joinPrivateChallenge(challengeId, AccessCode(code))
                    tokenManager.refreshToken()
                    VoucherResult.CHALLENGE_SUCCESS
                } catch (error: HttpException) {
                    val errorResponse = error.toErrorResponse()
                    if (errorResponse.code == 20523003) VoucherResult.GENDER_ERROR
                    else VoucherResult.ERROR
                } catch (e: Exception) {
                    VoucherResult.ERROR
                }
            }

            VoucherType.REFERRAL.value -> {
                return try {
                    restApi.applyReferralVoucher(ReferralVoucherBody(code))
                    VoucherResult.REFERRAL_SUCCESS
                } catch (e: Exception) {
                    VoucherResult.ERROR
                }
            }

            VoucherType.INFLUENCER_DISCOUNT.value -> {
                val voucherId = response.referral?.id ?: return VoucherResult.ERROR
                return try {
                    restApi.applySubscriptionVoucher(SubscriptionVoucherBody(voucherId))
                    VoucherResult.INFLUENCER_DISCOUNT
                } catch (e: Exception) {
                    VoucherResult.ERROR
                }
            }
            VoucherType.INFLUENCER_PROMOTION.value -> {
                val voucherId = response.referral?.id ?: return VoucherResult.ERROR
                return try {
                    restApi.applySubscriptionVoucher(SubscriptionVoucherBody(voucherId))
                    VoucherResult.SUCCESS
                } catch (e: Exception) {
                    VoucherResult.ERROR
                }
            }
            else -> VoucherResult.ERROR
        }
    }
}

enum class CodeErrorType(val value: String) {
    CORPORATION("corporation"),
    REFERRAL("referral"),
    CHALLENGE("challenge")
}

enum class VoucherType(val value: String) {
    CORPORATION("corporation"),
    REFERRAL("referral"),
    CHALLENGE("challenge"),
    INFLUENCER_DISCOUNT("influencerDiscount"),
    INFLUENCER_PROMOTION("influencerPromotion")
}

enum class VoucherResult(val value: String) {
    GENDER_ERROR("0"),
    CHALLENGE_SUCCESS("1"),
    CORPORATE_NEW_RATE_SUCCESS("new rate"),
    CORPORATE_REGIONAL_RATE_SUCCESS("regional rate"),
    CORPORATE_COMPLIMENTARY_SUCCESS("3"),
    REFERRAL_SUCCESS("referral success"),
    ERROR("4"),
    SUCCESS("5"),
    INFLUENCER_DISCOUNT("6"),
    INFLUENCER_DISCOUNT2("7"),
    INFLUENCER_PROMOTION("8"),
    CORPORATE_COMMON("9"),
    ERROR2("10"),
}