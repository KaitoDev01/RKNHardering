package com.notcvnt.rknhardering.checker

/**
 * Static catalog of home networks for SIM operators we want to recognize as
 * "expected roaming exit" — the operator's home APN/PGW always terminates the
 * data session, so when the user is roaming the public IP belongs to the home
 * network instead of the visited country. Without this knowledge such legit
 * roaming is indistinguishable from a real bypass.
 *
 * Keys are `MCC` ("250") or `MCC-MNC` ("208-15"). MCC-only entries match any
 * MNC of that country (used as a fallback when MNC is unknown).
 *
 * The entry stores ASN codes (e.g. 51207) and substring keywords that we look
 * for in the GeoIP `isp` / `org` / `asn` strings. ASN match is the strong
 * signal; keywords are a fallback when the ASN field isn't structured.
 */
internal data class HomeNetworkProfile(
    val mcc: String,
    val mnc: String?,
    val country: String,
    val operator: String,
    val asnCodes: Set<String>,
    val keywords: Set<String>,
)

internal object HomeNetworkCatalog {

    private val PROFILES: List<HomeNetworkProfile> = listOf(
        // France — Free Mobile (issue #63)
        HomeNetworkProfile(
            mcc = "208",
            mnc = "15",
            country = "FR",
            operator = "Free Mobile",
            asnCodes = setOf("51207"),
            keywords = setOf("free mobile", "free sas"),
        ),
        HomeNetworkProfile(
            mcc = "208",
            mnc = "16",
            country = "FR",
            operator = "Free Mobile",
            asnCodes = setOf("51207"),
            keywords = setOf("free mobile", "free sas"),
        ),
        // France — Orange
        HomeNetworkProfile(
            mcc = "208",
            mnc = "01",
            country = "FR",
            operator = "Orange",
            asnCodes = setOf("3215"),
            keywords = setOf("orange s.a.", "france telecom", "orange france"),
        ),
        HomeNetworkProfile(
            mcc = "208",
            mnc = "02",
            country = "FR",
            operator = "Orange",
            asnCodes = setOf("3215"),
            keywords = setOf("orange s.a.", "france telecom", "orange france"),
        ),
        // France — SFR
        HomeNetworkProfile(
            mcc = "208",
            mnc = "10",
            country = "FR",
            operator = "SFR",
            asnCodes = setOf("15557"),
            keywords = setOf("sfr", "societe francaise du radiotelephone"),
        ),
        // France — Bouygues Telecom
        HomeNetworkProfile(
            mcc = "208",
            mnc = "20",
            country = "FR",
            operator = "Bouygues Telecom",
            asnCodes = setOf("5410"),
            keywords = setOf("bouygues"),
        ),
        // Germany — Deutsche Telekom
        HomeNetworkProfile(
            mcc = "262",
            mnc = "01",
            country = "DE",
            operator = "Telekom Deutschland",
            asnCodes = setOf("3320"),
            keywords = setOf("deutsche telekom", "telekom deutschland"),
        ),
        // Germany — Vodafone
        HomeNetworkProfile(
            mcc = "262",
            mnc = "02",
            country = "DE",
            operator = "Vodafone",
            asnCodes = setOf("3209", "1273"),
            keywords = setOf("vodafone gmbh", "vodafone germany"),
        ),
        // Germany — Telefonica O2
        HomeNetworkProfile(
            mcc = "262",
            mnc = "07",
            country = "DE",
            operator = "Telefonica O2",
            asnCodes = setOf("6805", "13184"),
            keywords = setOf("telefonica germany", "o2 deutschland"),
        ),
        // United Kingdom — EE / BT
        HomeNetworkProfile(
            mcc = "234",
            mnc = "30",
            country = "GB",
            operator = "EE",
            asnCodes = setOf("12576", "5378"),
            keywords = setOf("ee limited", "everything everywhere"),
        ),
        // United Kingdom — Vodafone
        HomeNetworkProfile(
            mcc = "234",
            mnc = "15",
            country = "GB",
            operator = "Vodafone UK",
            asnCodes = setOf("1273"),
            keywords = setOf("vodafone limited", "vodafone uk"),
        ),
        // United Kingdom — O2
        HomeNetworkProfile(
            mcc = "234",
            mnc = "10",
            country = "GB",
            operator = "O2 UK",
            asnCodes = setOf("5607"),
            keywords = setOf("telefonica uk", "o2 uk"),
        ),
        // Italy — TIM
        HomeNetworkProfile(
            mcc = "222",
            mnc = "01",
            country = "IT",
            operator = "TIM",
            asnCodes = setOf("3269"),
            keywords = setOf("telecom italia", "tim s.p.a."),
        ),
        // Italy — Vodafone
        HomeNetworkProfile(
            mcc = "222",
            mnc = "10",
            country = "IT",
            operator = "Vodafone Italia",
            asnCodes = setOf("30722"),
            keywords = setOf("vodafone italia"),
        ),
        // Spain — Movistar
        HomeNetworkProfile(
            mcc = "214",
            mnc = "07",
            country = "ES",
            operator = "Movistar",
            asnCodes = setOf("3352"),
            keywords = setOf("telefonica de espana", "movistar"),
        ),
        // Netherlands — KPN
        HomeNetworkProfile(
            mcc = "204",
            mnc = "08",
            country = "NL",
            operator = "KPN",
            asnCodes = setOf("1136"),
            keywords = setOf("kpn b.v.", "kpn mobile"),
        ),
        // Poland — Orange / Play / Plus / T-Mobile
        HomeNetworkProfile(
            mcc = "260",
            mnc = "03",
            country = "PL",
            operator = "Orange Polska",
            asnCodes = setOf("5617"),
            keywords = setOf("orange polska"),
        ),
        // United States — T-Mobile
        HomeNetworkProfile(
            mcc = "310",
            mnc = "260",
            country = "US",
            operator = "T-Mobile USA",
            asnCodes = setOf("21928"),
            keywords = setOf("t-mobile usa"),
        ),
        // United States — AT&T
        HomeNetworkProfile(
            mcc = "310",
            mnc = "410",
            country = "US",
            operator = "AT&T Mobility",
            asnCodes = setOf("20057", "7018"),
            keywords = setOf("at&t mobility", "cellco partnership", "at&t services"),
        ),
        // United States — Verizon
        HomeNetworkProfile(
            mcc = "311",
            mnc = "480",
            country = "US",
            operator = "Verizon Wireless",
            asnCodes = setOf("22394", "6167"),
            keywords = setOf("verizon wireless", "cellco partnership"),
        ),
        // Belarus — A1 / MTS / Life
        HomeNetworkProfile(
            mcc = "257",
            mnc = null,
            country = "BY",
            operator = "Belarus mobile carrier",
            asnCodes = setOf("6697", "25106", "44087"),
            keywords = setOf("a1 belarus", "mobile telesystems llc", "best ltd"),
        ),
        // Kazakhstan — Beeline / Kcell / Tele2
        HomeNetworkProfile(
            mcc = "401",
            mnc = null,
            country = "KZ",
            operator = "Kazakhstan mobile carrier",
            asnCodes = setOf("21299", "29355", "35168"),
            keywords = setOf("kar-tel", "kcell", "mobile telecom-service"),
        ),
        // Ukraine — Kyivstar / Vodafone UA / lifecell
        HomeNetworkProfile(
            mcc = "255",
            mnc = null,
            country = "UA",
            operator = "Ukraine mobile carrier",
            asnCodes = setOf("15895", "21497", "34058"),
            keywords = setOf("kyivstar", "pjsc vf ukraine", "lifecell"),
        ),
        // Turkey — Turkcell / Vodafone TR
        HomeNetworkProfile(
            mcc = "286",
            mnc = "01",
            country = "TR",
            operator = "Turkcell",
            asnCodes = setOf("16135"),
            keywords = setOf("turkcell"),
        ),
    )

    fun lookup(mcc: String?, mnc: String?): HomeNetworkProfile? {
        if (mcc.isNullOrBlank()) return null
        if (!mnc.isNullOrBlank()) {
            val exact = PROFILES.firstOrNull { it.mcc == mcc && it.mnc == mnc }
            if (exact != null) return exact
        }
        return PROFILES.firstOrNull { it.mcc == mcc && it.mnc == null }
    }

    /**
     * Try to match `geoFacts` ASN/ISP strings against the SIM home network.
     * Returns reason string (for debugging/UI) on match, null otherwise.
     */
    fun matchExpectedExit(profile: HomeNetworkProfile, asn: String?, isp: String?, org: String?): String? {
        val asnCode = extractAsnCode(asn)
        if (asnCode != null && asnCode in profile.asnCodes) {
            return "ASN AS$asnCode matches ${profile.operator} home network (${profile.country})"
        }
        val haystack = listOf(asn, isp, org)
            .filterNotNull()
            .joinToString(" | ") { it.lowercase() }
        if (haystack.isBlank()) return null
        val keyword = profile.keywords.firstOrNull { it in haystack }
        return if (keyword != null) {
            "ISP/ORG matches ${profile.operator} home network (${profile.country})"
        } else {
            null
        }
    }

    private val ASN_REGEX = Regex("""AS\s*(\d+)""", RegexOption.IGNORE_CASE)

    fun extractAsnCode(asn: String?): String? {
        if (asn.isNullOrBlank()) return null
        return ASN_REGEX.find(asn)?.groupValues?.getOrNull(1)
    }
}
