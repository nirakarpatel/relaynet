package com.nmiet.app

import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.ui.screens.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterAndSearchUnitTest {

    private val sampleItems = listOf(
        ContentItem(
            id = "item-1",
            title = "End-Semester Examination Schedule Autumn 2026",
            summary = "Timetable for B.Tech exams released.",
            body = "Full details of exam dates...",
            category = "Exams",
            source = "Examination Cell",
            publishedAt = 1000L,
            contentHash = "hash1"
        ),
        ContentItem(
            id = "item-2",
            title = "Annual National Hackathon HackNMIET",
            summary = "Registration open for 36-hour offline hackathon.",
            body = "Cash prizes and mentorship...",
            category = "Campus Life",
            source = "CSE Department",
            publishedAt = 2000L,
            contentHash = "hash2"
        ),
        ContentItem(
            id = "item-3",
            title = "Research Colloquium on Distributed Mesh Networks",
            summary = "Keynote on peer-to-peer opportunistic protocols.",
            body = "Seminar Hall A at 3:30 PM...",
            category = "Research",
            source = "R&D Council",
            publishedAt = 3000L,
            contentHash = "hash3"
        )
    )

    @Test
    fun filterItems_returnsAll_whenCategoryIsAllAndQueryIsEmpty() {
        val filtered = HomeViewModel.filterItems(sampleItems, "", "All")
        assertEquals(3, filtered.size)
    }

    @Test
    fun filterItems_filtersByCategory() {
        val examsOnly = HomeViewModel.filterItems(sampleItems, "", "Exams")
        assertEquals(1, examsOnly.size)
        assertEquals("Exams", examsOnly[0].category)

        val researchOnly = HomeViewModel.filterItems(sampleItems, "", "Research")
        assertEquals(1, researchOnly.size)
        assertEquals("item-3", researchOnly[0].id)
    }

    @Test
    fun filterItems_searchesByTitleQuery() {
        val searchHackathon = HomeViewModel.filterItems(sampleItems, "Hackathon", "All")
        assertEquals(1, searchHackathon.size)
        assertEquals("item-2", searchHackathon[0].id)
    }

    @Test
    fun filterItems_searchesBySummary() {
        val searchProtocol = HomeViewModel.filterItems(sampleItems, "opportunistic", "All")
        assertEquals(1, searchProtocol.size)
        assertEquals("item-3", searchProtocol[0].id)
    }

    @Test
    fun filterItems_combinesCategoryAndSearchQuery() {
        val match = HomeViewModel.filterItems(sampleItems, "B.Tech", "Exams")
        assertEquals(1, match.size)

        val noMatch = HomeViewModel.filterItems(sampleItems, "B.Tech", "Campus Life")
        assertTrue(noMatch.isEmpty())
    }
}
