package com.nmiet.app.data.remote

import com.nmiet.app.domain.model.ContentItem
import kotlinx.coroutines.delay
import java.security.MessageDigest

class FakeRemoteContentSource : RemoteContentSource {

    private var refreshCount = 0

    override suspend fun fetchLatestContent(): Result<List<ContentItem>> {
        // Simulate network latency (400ms)
        delay(400)
        refreshCount++

        val baseItems = getInitialCatalog()
        val extraItem = if (refreshCount > 1) {
            listOf(
                createItem(
                    id = "nmiet-news-fresh-$refreshCount",
                    title = "Live Campus Update #$refreshCount: Smart Campus Wi-Fi Expansion",
                    summary = "Phase II of high-speed Wi-Fi network roll-out completed across academic blocks 3 and 4.",
                    body = "The IT Services Division announces the activation of 50 new high-density Wi-Fi 6 access points in Blocks 3 and 4. All registered students and faculty members can connect using their student portal credentials. Network performance testing will take place between 18:00 and 20:00 today.",
                    category = "Campus Life",
                    source = "IT Infrastructure Cell",
                    publishedOffsetMinutes = 5
                )
            )
        } else {
            emptyList()
        }

        return Result.success(extraItem + baseItems)
    }

    companion object {
        fun getInitialCatalog(): List<ContentItem> {
            return listOf(
                createItem(
                    id = "nmiet-acad-01",
                    title = "End-Semester Examination Schedule: Autumn 2026",
                    summary = "The definitive timetable for B.Tech, M.Tech, and MCA end-semester examinations is now released.",
                    body = "The Controller of Examinations has published the Autumn 2026 examination timetable. Practical lab examinations commence on November 15, followed by theory exams beginning November 24. Students must download their admit cards from the academic ERP portal before November 10. No student will be admitted to the examination hall without physical ID and hall ticket.",
                    category = "Exams",
                    source = "Office of Examination",
                    publishedOffsetMinutes = 120
                ),
                createItem(
                    id = "nmiet-notices-02",
                    title = "Annual National Hackathon 'HackNMIET 2026' Registration Open",
                    summary = "36-hour offline hackathon with INR 2,00,000 in prizes. Themes include AI in Healthcare, Mesh Networks, and Green Tech.",
                    body = "The Department of Computer Science & Engineering invites student teams to register for HackNMIET 2026. Teams of 2 to 4 members may apply. Mentors from top tech enterprises will be on campus to guide participants throughout the 36-hour sprint. Accommodation and catering are provided for all shortlisted finalists. Deadline for abstract submission is October 25.",
                    category = "Campus Life",
                    source = "CSE Student Chapter",
                    publishedOffsetMinutes = 240
                ),
                createItem(
                    id = "nmiet-research-03",
                    title = "Research Colloquium: Distributed Systems & Offline-First Protocols",
                    summary = "Dr. S. K. Mahapatra to present keynote on opportunistic peer-to-peer data dissemination in disaster management.",
                    body = "Join the Department of Information Technology for this month's research colloquium. The session explores how localized Bluetooth Mesh and store-and-forward networks can provide vital communication infrastructure in network-denied environments. Date: Friday, 3:30 PM at Seminar Hall A. Open to all students and research scholars.",
                    category = "Research",
                    source = "R&D Advisory Council",
                    publishedOffsetMinutes = 480
                ),
                createItem(
                    id = "nmiet-acad-04",
                    title = "Library Digital Access: IEEE Xplore & Springer Nature Subscriptions Renewed",
                    summary = "Central Library renews premier digital database subscriptions for the 2026-2027 academic year.",
                    body = "The Central Library has successfully renewed campus-wide remote and intranet access to IEEE Xplore, ScienceDirect, and Springer Nature. Off-campus VPN access credentials have been synchronized with your student institutional email IDs. Contact the library helpdesk for assistance.",
                    category = "Academics",
                    source = "Central Library Council",
                    publishedOffsetMinutes = 720
                ),
                createItem(
                    id = "nmiet-notices-05",
                    title = "Placement Cell: Campus Recruitment Drive Schedule for 2027 Batch",
                    summary = "Pre-placement training sessions and company visit dates released for final and pre-final year students.",
                    body = "The Training & Placement Cell announces the commencement of pre-placement grooming workshops covering data structures, algorithmic problem solving, and aptitude assessments. Attendance in at least 85% of workshops is mandatory for campus drive eligibility.",
                    category = "Notices",
                    source = "Training & Placement Cell",
                    publishedOffsetMinutes = 1440
                )
            )
        }

        private fun createItem(
            id: String,
            title: String,
            summary: String,
            body: String,
            category: String,
            source: String,
            publishedOffsetMinutes: Long
        ): ContentItem {
            val publishedAt = System.currentTimeMillis() - (publishedOffsetMinutes * 60 * 1000)
            val hash = sha256("$id:$title:$body")
            return ContentItem(
                id = id,
                title = title,
                summary = summary,
                body = body,
                category = category,
                source = source,
                publishedAt = publishedAt,
                contentHash = hash,
                isSavedOffline = false,
                isReceivedViaNearby = false
            )
        }

        private fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }.take(16)
        }
    }
}
