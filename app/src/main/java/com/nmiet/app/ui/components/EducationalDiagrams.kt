package com.nmiet.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun EducationalDiagramCard(
    diagramType: String?,
    modifier: Modifier = Modifier
) {
    if (diagramType.isNullOrBlank()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = getDiagramTitle(diagramType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "100% Offline Vector",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Render specific diagram canvas
            when (diagramType) {
                "PHOTOSYNTHESIS_CHLOROPLAST_FLOW" -> PhotosynthesisDiagramCanvas()
                "PLANT_ANIMAL_CELL_DIAGRAM" -> PlantCellDiagramCanvas()
                "BALANCED_DIET_PYRAMID" -> BalancedDietPyramidCanvas()
                "DISTANCE_TIME_GRAPH" -> DistanceTimeGraphCanvas()
                "LINEAR_GRAPH_COORDINATES" -> LinearGraphCoordinatesCanvas()
                else -> GenericConceptDiagramCanvas(diagramType)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DiagramLegendRow(diagramType)
        }
    }
}

private fun getDiagramTitle(diagramType: String): String {
    return when (diagramType) {
        "PHOTOSYNTHESIS_CHLOROPLAST_FLOW" -> "Visual: Chloroplast Light & Dark Reaction Flow"
        "PLANT_ANIMAL_CELL_DIAGRAM" -> "Visual: Plant Cell Structure & Organelles"
        "BALANCED_DIET_PYRAMID" -> "Visual: Food Nutrients & Balanced Diet Pyramid"
        "DISTANCE_TIME_GRAPH" -> "Visual: Uniform Motion Distance-Time Graph"
        "LINEAR_GRAPH_COORDINATES" -> "Visual: Cartesian Coordinate Linear Graph (ax+by+c=0)"
        else -> "Visual Educational Illustration"
    }
}

@Composable
fun PhotosynthesisDiagramCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F2418))
    ) {
        val w = size.width
        val h = size.height

        // Outer chloroplast boundary
        drawRoundRect(
            color = Color(0xFF1E5128),
            topLeft = Offset(w * 0.08f, h * 0.12f),
            size = Size(w * 0.84f, h * 0.76f),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Sunlight Radiation Rays (Left)
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(w * 0.02f, h * 0.05f),
            end = Offset(w * 0.22f, h * 0.35f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(w * 0.08f, h * 0.02f),
            end = Offset(w * 0.26f, h * 0.32f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Grana (Thylakoid stacks) - Light Reaction
        for (i in 0..3) {
            drawRoundRect(
                color = Color(0xFF4E9F3D),
                topLeft = Offset(w * 0.20f, h * (0.32f + i * 0.09f)),
                size = Size(w * 0.18f, h * 0.07f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }

        // Arrow from H2O to Grana
        drawLine(
            color = Color(0xFF4FC3F7),
            start = Offset(w * 0.12f, h * 0.65f),
            end = Offset(w * 0.20f, h * 0.50f),
            strokeWidth = 2.5f.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Oxygen (O2) Released from Grana
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(w * 0.28f, h * 0.28f),
            end = Offset(w * 0.28f, h * 0.06f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Stroma Matrix - Dark Reaction (Calvin Cycle Circle)
        drawCircle(
            color = Color(0xFF1976D2),
            radius = h * 0.22f,
            center = Offset(w * 0.68f, h * 0.50f),
            style = Stroke(width = 2.5f.dp.toPx())
        )

        // Connecting ATP/NADPH flow from Grana to Stroma
        val atpPath = Path().apply {
            moveTo(w * 0.38f, h * 0.40f)
            quadraticTo(w * 0.48f, h * 0.30f, w * 0.56f, h * 0.42f)
        }
        drawPath(atpPath, color = Color(0xFFFFB300), style = Stroke(width = 2.dp.toPx()))

        // Sugar / Glucose output
        drawLine(
            color = Color(0xFFFF8A65),
            start = Offset(w * 0.68f, h * 0.72f),
            end = Offset(w * 0.68f, h * 0.94f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PlantCellDiagramCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1E24))
    ) {
        val w = size.width
        val h = size.height

        // Rigid Outer Cell Wall
        drawRoundRect(
            color = Color(0xFF2E7D32),
            topLeft = Offset(w * 0.08f, h * 0.10f),
            size = Size(w * 0.84f, h * 0.80f),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )

        // Plasma Membrane
        drawRoundRect(
            color = Color(0xFF66BB6A),
            topLeft = Offset(w * 0.12f, h * 0.14f),
            size = Size(w * 0.76f, h * 0.72f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Central Large Vacuole
        drawRoundRect(
            color = Color(0xFF29B6F6).copy(alpha = 0.35f),
            topLeft = Offset(w * 0.42f, h * 0.24f),
            size = Size(w * 0.42f, h * 0.52f),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )

        // Nucleus
        drawCircle(
            color = Color(0xFFAB47BC),
            radius = h * 0.16f,
            center = Offset(w * 0.28f, h * 0.45f)
        )
        // Nucleolus inside
        drawCircle(
            color = Color(0xFFF06292),
            radius = h * 0.06f,
            center = Offset(w * 0.28f, h * 0.45f)
        )

        // Chloroplasts (green ovals)
        drawOval(
            color = Color(0xFF43A047),
            topLeft = Offset(w * 0.18f, h * 0.70f),
            size = Size(w * 0.14f, h * 0.12f)
        )
        // Mitochondria (orange oval)
        drawOval(
            color = Color(0xFFFF7043),
            topLeft = Offset(w * 0.22f, h * 0.20f),
            size = Size(w * 0.12f, h * 0.10f)
        )
    }
}

@Composable
fun BalancedDietPyramidCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E28))
    ) {
        val w = size.width
        val h = size.height

        val apex = Offset(w * 0.50f, h * 0.12f)
        val leftBase = Offset(w * 0.12f, h * 0.88f)
        val rightBase = Offset(w * 0.88f, h * 0.88f)

        // Base Layer (Carbohydrates - Bread, Rice)
        val pathBase = Path().apply {
            moveTo(w * 0.12f, h * 0.88f)
            lineTo(w * 0.25f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.65f)
            lineTo(w * 0.88f, h * 0.88f)
            close()
        }
        drawPath(pathBase, color = Color(0xFFFFB74D))

        // Middle Layer (Vitamins, Minerals, Fruits & Veggies)
        val pathMid = Path().apply {
            moveTo(w * 0.25f, h * 0.65f)
            lineTo(w * 0.36f, h * 0.44f)
            lineTo(w * 0.64f, h * 0.44f)
            lineTo(w * 0.75f, h * 0.65f)
            close()
        }
        drawPath(pathMid, color = Color(0xFF81C784))

        // Upper Layer (Proteins - Pulses, Milk, Eggs)
        val pathUpper = Path().apply {
            moveTo(w * 0.36f, h * 0.44f)
            lineTo(w * 0.44f, h * 0.28f)
            lineTo(w * 0.56f, h * 0.28f)
            lineTo(w * 0.64f, h * 0.44f)
            close()
        }
        drawPath(pathUpper, color = Color(0xFF64B5F6))

        // Top Apex (Fats, Oils, Sugars)
        val pathTop = Path().apply {
            moveTo(w * 0.44f, h * 0.28f)
            lineTo(apex.x, apex.y)
            lineTo(w * 0.56f, h * 0.28f)
            close()
        }
        drawPath(pathTop, color = Color(0xFFE57373))
    }
}

@Composable
fun DistanceTimeGraphCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131A26))
    ) {
        val w = size.width
        val h = size.height
        val origin = Offset(w * 0.16f, h * 0.82f)

        // Axes (X: Time t, Y: Distance s)
        drawLine(Color.White, origin, Offset(w * 0.90f, h * 0.82f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color.White, origin, Offset(w * 0.16f, h * 0.15f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

        // Grid ticks
        for (i in 1..4) {
            val gx = origin.x + i * (w * 0.17f)
            val gy = origin.y - i * (h * 0.15f)
            drawLine(Color.Gray.copy(alpha = 0.3f), Offset(gx, origin.y), Offset(gx, h * 0.15f), strokeWidth = 1.dp.toPx())
            drawLine(Color.Gray.copy(alpha = 0.3f), Offset(origin.x, gy), Offset(w * 0.90f, gy), strokeWidth = 1.dp.toPx())
        }

        // Uniform velocity straight slope line
        drawLine(
            color = Color(0xFF00E5FF),
            start = origin,
            end = Offset(w * 0.84f, h * 0.22f),
            strokeWidth = 3.5f.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sample points on line
        drawCircle(Color(0xFFFFD600), radius = 5.dp.toPx(), center = Offset(origin.x + (w * 0.34f), origin.y - (h * 0.30f)))
        drawCircle(Color(0xFFFFD600), radius = 5.dp.toPx(), center = Offset(origin.x + (w * 0.68f), origin.y - (h * 0.60f)))
    }
}

@Composable
fun LinearGraphCoordinatesCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B1824))
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.50f, h * 0.50f)

        // Cartesian Axes
        drawLine(Color.LightGray, Offset(w * 0.10f, center.y), Offset(w * 0.90f, center.y), strokeWidth = 2.dp.toPx())
        drawLine(Color.LightGray, Offset(center.x, h * 0.10f), Offset(center.x, h * 0.90f), strokeWidth = 2.dp.toPx())

        // Linear equation straight line (2x + 3y = 12)
        drawLine(
            color = Color(0xFFFF4081),
            start = Offset(w * 0.15f, h * 0.78f),
            end = Offset(w * 0.85f, h * 0.22f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Intercept Dots
        drawCircle(Color(0xFF00E676), radius = 5.dp.toPx(), center = Offset(center.x, h * 0.36f)) // y-intercept
        drawCircle(Color(0xFF00E676), radius = 5.dp.toPx(), center = Offset(w * 0.68f, center.y)) // x-intercept
    }
}

@Composable
fun GenericConceptDiagramCanvas(name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Educational Concept Illustration: $name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DiagramLegendRow(diagramType: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (diagramType) {
            "PHOTOSYNTHESIS_CHLOROPLAST_FLOW" -> {
                LegendBadge(color = Color(0xFFFFD54F), text = "Sunlight")
                LegendBadge(color = Color(0xFF4E9F3D), text = "Grana (Light)")
                LegendBadge(color = Color(0xFF1976D2), text = "Stroma (Dark)")
                LegendBadge(color = Color(0xFF81C784), text = "O₂ Gas")
            }
            "PLANT_ANIMAL_CELL_DIAGRAM" -> {
                LegendBadge(color = Color(0xFF2E7D32), text = "Cell Wall")
                LegendBadge(color = Color(0xFFAB47BC), text = "Nucleus")
                LegendBadge(color = Color(0xFF29B6F6), text = "Vacuole")
                LegendBadge(color = Color(0xFF43A047), text = "Chloroplast")
            }
            "BALANCED_DIET_PYRAMID" -> {
                LegendBadge(color = Color(0xFFFFB74D), text = "Carbs")
                LegendBadge(color = Color(0xFF81C784), text = "Vitamins")
                LegendBadge(color = Color(0xFF64B5F6), text = "Proteins")
                LegendBadge(color = Color(0xFFE57373), text = "Fats")
            }
            "DISTANCE_TIME_GRAPH" -> {
                LegendBadge(color = Color(0xFF00E5FF), text = "v = d/t Slope")
                LegendBadge(color = Color(0xFFFFD600), text = "Sample (t, d)")
            }
            "LINEAR_GRAPH_COORDINATES" -> {
                LegendBadge(color = Color(0xFFFF4081), text = "ax + by + c = 0")
                LegendBadge(color = Color(0xFF00E676), text = "Intercepts")
            }
        }
    }
}

@Composable
fun LegendBadge(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
