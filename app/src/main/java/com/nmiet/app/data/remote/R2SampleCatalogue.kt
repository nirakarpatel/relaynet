package com.nmiet.app.data.remote

import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.ContentMediaType
import com.nmiet.app.domain.model.DownloadStatus

/**
 * Verified sample catalogue corresponding to the active objects in the Cloudflare R2 bucket:
 * https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/
 *
 * Each entry includes verified exact byte sizes and trusted SHA-256 hashes computed directly
 * from the live R2 objects.
 */
object R2SampleCatalogue {

    fun getVerifiedSampleItems(baseUrl: String = R2ContentConfig.DEFAULT_PUBLIC_BASE_URL): List<ContentItem> {
        return listOf(
            ContentItem(
                id = "r2_res_quick_revision_txt",
                title = "Photosynthesis Quick Revision Notes",
                description = "High-yield revision notes: light reaction, chlorophyll function, dark reaction, and check questions.",
                mediaType = ContentMediaType.REVISION_NOTES,
                remoteUrl = R2ContentConfig.getObjectUrl("resources/quick_revision.txt", baseUrl),
                sizeBytes = 479L,
                sha256Checksum = "e79e8564426209b95186116cac7059e4e34ee42b4f9cc6a0f1a775816320f8a9",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "English",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_diag_photosynthesis_png",
                title = "Photosynthesis Process Diagram",
                description = "Detailed visual diagram of light-dependent chloroplast pathways, water photolysis, and sugar synthesis.",
                mediaType = ContentMediaType.IMAGE_DIAGRAM,
                remoteUrl = R2ContentConfig.getObjectUrl("diagrams/photosynthesis.png", baseUrl),
                sizeBytes = 42_691L,
                sha256Checksum = "66723e43543ac568711300f31c348bddf7745f94345a0a6207beaa6b4c6c309f",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "Bilingual / Visual",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_diag_water_cycle_png",
                title = "Water Cycle & Transpiration Diagram",
                description = "Visual diagram illustrating evaporation, condensation, precipitation, and plant transpiration cycles.",
                mediaType = ContentMediaType.IMAGE_DIAGRAM,
                remoteUrl = R2ContentConfig.getObjectUrl("diagrams/water_cycle.png", baseUrl),
                sizeBytes = 31_176L,
                sha256Checksum = "99b41f9d0c98f2c5fd5dd0c049133c702b811f79ea29518f5d7696b5b2d6eea0",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "Bilingual / Visual",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_pack_nearby_v2_zip",
                title = "RelayNet Learning Pack v2 (Complete Bundle)",
                description = "Standard offline learning pack ZIP containing bilingual lessons, diagrams, revision notes, and signed manifest.",
                mediaType = ContentMediaType.LEARNING_PACK_ZIP,
                remoteUrl = R2ContentConfig.getObjectUrl("RelayNet_Nearby_Learning_Pack_v2.zip", baseUrl),
                sizeBytes = 70_562L,
                sha256Checksum = "1925c78b5c4b53affdaab782fd31f69ae2e98eb51774c2a106eb981d58751264",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "Bilingual (English / Odia)",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_lesson_english_json",
                title = "English Lesson Summary (JSON)",
                description = "Structured English curriculum lesson overview with core biological equations.",
                mediaType = ContentMediaType.JSON_RESOURCE,
                remoteUrl = R2ContentConfig.getObjectUrl("lesson/english.json", baseUrl),
                sizeBytes = 751L,
                sha256Checksum = "39404d9e610c62afad5267d1f35920dfc12193878be2b5f6384fc265706b31e0",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "English",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_lesson_odia_json",
                title = "ନବମ ଶ୍ରେଣୀ ବିଜ୍ଞାନ: ଓଡ଼ିଆ ପାଠ ସାରାଂଶ (JSON)",
                description = "ଓଡ଼ିଆ ମାଧ୍ୟମ ପାଠ୍ୟକ୍ରମ ସାରାଂଶ, ଆଲୋକ ପ୍ରକ୍ରିୟା ଏବଂ ରାସାୟନିକ ସମୀକରଣ।",
                mediaType = ContentMediaType.JSON_RESOURCE,
                remoteUrl = R2ContentConfig.getObjectUrl("lesson/odia.json", baseUrl),
                sizeBytes = 1_247L,
                sha256Checksum = "d62f5f45b17fcb5b9928b64c8b3b6a9e3a9a19cf6419a4bc8cfb66521ab1f8cf",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "Odia (ଓଡ଼ିଆ)",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            ),
            ContentItem(
                id = "r2_manifest_json",
                title = "Learning Pack Technical Manifest (JSON)",
                description = "Curriculum metadata schema specifying pack identity, board standards, version, and bundled asset paths.",
                mediaType = ContentMediaType.JSON_RESOURCE,
                remoteUrl = R2ContentConfig.getObjectUrl("manifest.json", baseUrl),
                sizeBytes = 903L,
                sha256Checksum = "412c92c07323bd3c3440b17f880bbe651b8a65b535a0ed55924fbf11f33b59e5",
                classGrade = 9,
                subject = "Science (Biology)",
                board = Board.ODISHA_STATE,
                language = "Technical (JSON)",
                downloadStatus = DownloadStatus.NOT_DOWNLOADED
            )
        )
    }
}
