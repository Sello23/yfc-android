package com.yourfitness.common.ui.utils

import android.net.Uri
import com.yourfitness.common.network.YFCNetworkConfig
import java.util.*

fun String.toImageUri(): Uri {
    return Uri.parse(YFCNetworkConfig.IMAGE_URL.format(Locale.US, this))
}