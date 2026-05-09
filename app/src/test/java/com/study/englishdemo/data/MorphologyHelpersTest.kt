package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MorphologyHelpersTest {
    @Test
    fun decomposeWord_splitsPrefixRootSuffix() {
        val segs = decomposeWord("prescribe", "scrib")
        assertThat(segs.map { it.text }).containsExactly("pre", "scrib", "e").inOrder()
        assertThat(segs.map { it.kind }).containsExactly(
            Morpheme.PREFIX, Morpheme.ROOT, Morpheme.SUFFIX,
        ).inOrder()
    }

    @Test
    fun decomposeWord_handlesRootAtStart() {
        val segs = decomposeWord("statement", "stat")
        assertThat(segs.map { it.text }).containsExactly("stat", "ement").inOrder()
        assertThat(segs[0].kind).isEqualTo(Morpheme.ROOT)
        assertThat(segs[1].kind).isEqualTo(Morpheme.SUFFIX)
    }

    @Test
    fun decomposeWord_missingRootReturnsPlain() {
        val segs = decomposeWord("floor", "")
        assertThat(segs).hasSize(1)
        assertThat(segs.first().kind).isEqualTo(Morpheme.PLAIN)
    }

    @Test
    fun decomposeWord_unmatchedRootReturnsPlain() {
        val segs = decomposeWord("running", "scrib")
        assertThat(segs).hasSize(1)
        assertThat(segs.first().text).isEqualTo("running")
    }

    @Test
    fun computePaceRecommendation_autoDisabledReturnsBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 500,
            examDate = LocalDate.of(2026, 6, 1),
            today = LocalDate.of(2026, 5, 9),
            baseline = 20,
            autoEnabled = false,
        )
        assertThat(pace.isAuto).isFalse()
        assertThat(pace.target).isEqualTo(20)
        assertThat(pace.remainingDays).isEqualTo(23)
    }

    @Test
    fun computePaceRecommendation_noExamDateClampsBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 500,
            examDate = null,
            today = LocalDate.of(2026, 5, 9),
            baseline = 999,
            autoEnabled = true,
        )
        assertThat(pace.target).isEqualTo(40)
        assertThat(pace.examDate).isNull()
        assertThat(pace.remainingDays).isNull()
    }

    @Test
    fun computePaceRecommendation_autoComputesTargetWithBuffer() {
        // 600 words / 30 days = 20/day raw, 24/day with 20% buffer
        val pace = computePaceRecommendation(
            remainingWords = 600,
            examDate = LocalDate.of(2026, 6, 8),
            today = LocalDate.of(2026, 5, 9),
            baseline = 10,
            autoEnabled = true,
        )
        assertThat(pace.isAuto).isTrue()
        assertThat(pace.remainingDays).isEqualTo(30)
        assertThat(pace.target).isEqualTo(24)
    }

    @Test
    fun computePaceRecommendation_pastExamDateFallsBackToBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 200,
            examDate = LocalDate.of(2026, 5, 1),
            today = LocalDate.of(2026, 5, 9),
            baseline = 20,
            autoEnabled = true,
        )
        assertThat(pace.isAuto).isFalse()
        assertThat(pace.target).isEqualTo(20)
        assertThat(pace.remainingDays).isLessThan(0)
    }
}
