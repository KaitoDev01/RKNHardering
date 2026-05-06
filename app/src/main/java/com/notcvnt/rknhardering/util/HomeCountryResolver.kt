package com.notcvnt.rknhardering.util

import android.content.Context
import android.telephony.TelephonyManager
import com.notcvnt.rknhardering.model.GeoIpFacts

internal object HomeCountryResolver {

    data class Resolved(
        val country: String?,
        val sources: List<String>,
        val conflict: Boolean,
    )

    fun resolve(context: Context, geoFacts: GeoIpFacts?): Resolved {
        val geo = geoFacts?.countryCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        if (geo != null) {
            return Resolved(geo, listOf("geoip"), conflict = false)
        }

        val telephony = linkedMapOf<String, String>()
        runCatching {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            tm?.simCountryIso?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                ?.let { telephony["sim"] = it }
            tm?.networkCountryIso?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                ?.let { telephony["network"] = it }
        }

        if (telephony.isEmpty()) return Resolved(null, emptyList(), false)

        val unique = telephony.values.toSet()
        return if (unique.size == 1) {
            Resolved(unique.single(), telephony.keys.toList(), conflict = false)
        } else {
            Resolved(null, telephony.keys.toList(), conflict = true)
        }
    }
}
