package com.notcvnt.rknhardering.checker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.notcvnt.rknhardering.model.EvidenceConfidence
import com.notcvnt.rknhardering.model.GeoIpFacts
import com.notcvnt.rknhardering.network.DnsResolverConfig
import com.notcvnt.rknhardering.probe.SystemPingProber
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class RttTriangulationCheckerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val ruGeoFacts = GeoIpFacts(countryCode = "RU")

    @After
    fun tearDown() {
        RttTriangulationChecker.dependenciesOverride = null
    }

    // Case 1: no geoFacts, no SIM/network (Robolectric TelephonyManager returns empty ISOs)
    // → country undetermined → detected=false, needsReview=true, finding contains "country undetermined"
    @Test
    fun `home_country_unknown_returns_inconclusive`() = runBlocking {
        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = null,
        )

        assertFalse(result.detected)
        assertTrue(result.needsReview)
        assertTrue(result.findings.any { "country undetermined" in it.description })
    }

    // Case 2: RU 25ms, foreign 120ms, country=RU
    // homeMedian=25 ≤ 80 → "no anomaly" branch → detected=false, needsReview=false
    @Test
    fun `home_fast_foreign_slow_clean`() = runBlocking {
        RttTriangulationChecker.dependenciesOverride = uniformDependencies(
            ruAvgMs = 25.0,
            foreignAvgMs = 120.0,
        )

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = ruGeoFacts,
        )

        assertFalse(result.detected)
        assertFalse(result.needsReview)
    }

    // Case 3: RU 150ms, foreign 30ms, country=RU
    // homeMedian=150 > 80, foreignMedian=30, homeMedian > foreignMedian → detected=true, MEDIUM
    @Test
    fun `home_slow_foreign_fast_detected_medium`() = runBlocking {
        RttTriangulationChecker.dependenciesOverride = uniformDependencies(
            ruAvgMs = 150.0,
            foreignAvgMs = 30.0,
        )

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = ruGeoFacts,
        )

        assertTrue(result.detected)
        val mainFinding = result.findings.first()
        assertEquals(EvidenceConfidence.MEDIUM, mainFinding.confidence)
    }

    // Case 4: RU 150ms, foreign 250ms, country=RU
    // homeMedian=150 > 80, foreignMedian=250, homeMedian <= foreignMedian → detected=false, LOW, needsReview=true
    @Test
    fun `home_slow_foreign_slower_low_confidence`() = runBlocking {
        RttTriangulationChecker.dependenciesOverride = uniformDependencies(
            ruAvgMs = 150.0,
            foreignAvgMs = 250.0,
        )

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = ruGeoFacts,
        )

        assertFalse(result.detected)
        assertTrue(result.needsReview)
        val mainFinding = result.findings.first()
        assertEquals(EvidenceConfidence.LOW, mainFinding.confidence)
    }

    // Case 5: RU 150ms with high jitter, foreign 30ms with high jitter
    // homeMedian=150 > foreignMedian=30 → MEDIUM before jitter check
    // All 10 targets have jitter=80 > 60 → 10/10 = 100% > 50% → downgrade MEDIUM→LOW
    //
    // RU:      avg=150, min=70,  max=150 → jitter=80 > 60
    // Foreign: avg=30,  min=0,   max=80  → jitter=80 > 60
    @Test
    fun `high_jitter_downgrades_confidence`() = runBlocking {
        val ruHosts = setOf("yandex.ru", "mail.ru", "vk.com", "sberbank.ru", "gosuslugi.ru")
        RttTriangulationChecker.dependenciesOverride = RttTriangulationChecker.Dependencies(
            resolveIpv4 = { host, _ -> "10.0.${if (host in ruHosts) "1" else "2"}.1" },
            ping = { address, _ ->
                if (address.startsWith("10.0.1")) {
                    makePingResult(received = 3, avg = 150.0, min = 70.0, max = 150.0)
                } else {
                    makePingResult(received = 3, avg = 30.0, min = 0.0, max = 80.0)
                }
            },
        )

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = ruGeoFacts,
        )

        assertTrue(result.detected)
        val mainFinding = result.findings.first()
        assertEquals(EvidenceConfidence.LOW, mainFinding.confidence)
    }

    // Case 6: all resolve/ping fail → homeMedian=null, foreignMedian=null → detected=false, needsReview=true, "unavailable"
    @Test
    fun `all_unreachable_unavailable`() = runBlocking {
        RttTriangulationChecker.dependenciesOverride = RttTriangulationChecker.Dependencies(
            resolveIpv4 = { host, _ -> throw IOException("network unreachable for $host") },
            ping = { _, _ -> error("should not be called") },
        )

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = ruGeoFacts,
        )

        assertFalse(result.detected)
        assertTrue(result.needsReview)
        assertTrue(result.findings.any { "unavailable" in it.description })
    }

    // Case 7: countryCode="DE" → detected=false, finding contains "only RU supported"
    @Test
    fun `non_ru_country_inconclusive`() = runBlocking {
        val deGeoFacts = GeoIpFacts(countryCode = "DE")

        val result = RttTriangulationChecker.check(
            context = context,
            resolverConfig = DnsResolverConfig.system(),
            geoFacts = deGeoFacts,
        )

        assertFalse(result.detected)
        assertTrue(result.findings.any { "RU only" in it.description })
    }

    // Helpers

    /**
     * Builds Dependencies where all RU targets return [ruAvgMs] and all foreign targets return
     * [foreignAvgMs]. Jitter is low (max - min = 10 ms) so it never triggers the downgrade rule.
     */
    private fun uniformDependencies(
        ruAvgMs: Double,
        foreignAvgMs: Double,
    ): RttTriangulationChecker.Dependencies {
        val ruHosts = setOf("yandex.ru", "mail.ru", "vk.com", "sberbank.ru", "gosuslugi.ru")
        return RttTriangulationChecker.Dependencies(
            resolveIpv4 = { host, _ -> "10.0.${if (host in ruHosts) "1" else "2"}.1" },
            ping = { address, _ ->
                val avg = if (address.startsWith("10.0.1")) ruAvgMs else foreignAvgMs
                // jitter = max - min = 10 ms ≤ 60 → no downgrade
                makePingResult(received = 3, avg = avg, min = avg - 5.0, max = avg + 5.0)
            },
        )
    }

    private fun makePingResult(
        received: Int,
        avg: Double? = null,
        min: Double? = null,
        max: Double? = null,
    ): SystemPingProber.PingResult {
        return SystemPingProber.PingResult(
            address = "0.0.0.0",
            sent = 4,
            received = received,
            minRttMs = min,
            avgRttMs = avg,
            maxRttMs = max,
            exitCode = if (received > 0) 0 else 1,
            rawOutput = "",
        )
    }
}
