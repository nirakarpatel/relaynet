package com.nmiet.app

import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.TopicMastery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelayNetUnitTest {

    @Test
    fun pilotPack_containsFiveDiagnosticQuestions() {
        val pack = PilotCurriculumCatalog.getPilotLearningPack()
        assertEquals("relaynet_class9_science_photosynthesis_v1", pack.packId)
        assertEquals(9, pack.classGrade)
        assertEquals(5, pack.quizQuestions.size)
        assertTrue(pack.titleOdia.contains("ଆଲୋକ ଶ୍ଳେଷଣ"))
        assertNotNull(pack.lesson.equationOrFormula)
    }

    @Test
    fun offlineTutor_answersQuestionsInOdiaAndEnglish() {
        val (odiaResp, engResp) = PilotCurriculumCatalog.getOfflineTutorResponse("ଆଲୋକ ଶ୍ଳେଷଣ କଣ?")
        assertTrue(odiaResp.contains("ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି"))
        assertTrue(engResp.contains("Photosynthesis"))

        val (eqOdia, eqEng) = PilotCurriculumCatalog.getOfflineTutorResponse("What is the formula equation?")
        assertTrue(eqOdia.contains("6CO₂"))
        assertTrue(eqEng.contains("6CO₂"))
    }

    @Test
    fun topicMastery_calculatesCorrectTiers() {
        val strong = TopicMastery(
            topicId = "1",
            topicTitleOdia = "ଓଡ଼ିଆ",
            topicTitleEnglish = "English",
            subject = "Science",
            masteryScore = 90,
            lastPracticedDate = 1000L,
            attemptsCount = 2,
            recommendedNextAction = "Next"
        )
        assertTrue(strong.tierLabel.contains("Strong Mastery"))

        val developing = strong.copy(masteryScore = 55)
        assertTrue(developing.tierLabel.contains("Developing"))

        val foundation = strong.copy(masteryScore = 30)
        assertTrue(foundation.tierLabel.contains("Needs Foundation"))
    }
}
