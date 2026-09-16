package com.example.ui.screens.customstudio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel

@Composable
fun CakeCanvasPreview(
    tiersCount: Int,
    spongeFlavor: String,
    frostingFlavor: String,
    customPipingText: String,
    modifier: Modifier = Modifier
) {
    // Determine visual colors based on flavors
    val frostingColor = when (frostingFlavor) {
        "Belgian Dark Chocolate Ganache" -> Color(0xFF3E2723)
        "Salted Caramel Buttercream" -> Color(0xFFD7A15C)
        "Alphonso Mango Whip" -> Color(0xFFFFCC80)
        "Rose Cardamom Rabdi Cream" -> Color(0xFFF8BBD0)
        else -> Color(0xFFFFF9EE) // Cream Cheese / Ivory
    }

    val spongeColor = when (spongeFlavor) {
        "Red Velvet Classic" -> Color(0xFF8B1E2D)
        "Belgian Dark Chocolate" -> Color(0xFF2C1810)
        "Saffron Cardamom (Rasmalai Base)" -> Color(0xFFFFD54F)
        "Alphonso Mango Sponge" -> Color(0xFFFFB74D)
        else -> Color(0xFFF5E6CC) // Vanilla Bean
    }

    val plateColor = GoldenCaramel

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw base pedestal / cake board
                val boardWidth = canvasWidth * 0.85f
                val boardHeight = 16f
                val boardX = (canvasWidth - boardWidth) / 2f
                val boardY = canvasHeight - 24f

                drawRoundRect(
                    color = plateColor,
                    topLeft = Offset(boardX, boardY),
                    size = Size(boardWidth, boardHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Golden rim highlight
                drawRoundRect(
                    color = Color(0xFFFFE082),
                    topLeft = Offset(boardX + 4f, boardY + 2f),
                    size = Size(boardWidth - 8f, 3f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Calculate tier heights and widths
                val tier1Width = canvasWidth * 0.64f
                val tier1Height = 44f
                val tier1X = (canvasWidth - tier1Width) / 2f
                val tier1Y = boardY - tier1Height

                val tier2Width = canvasWidth * 0.46f
                val tier2Height = 40f
                val tier2X = (canvasWidth - tier2Width) / 2f
                val tier2Y = tier1Y - tier2Height

                val tier3Width = canvasWidth * 0.32f
                val tier3Height = 36f
                val tier3X = (canvasWidth - tier3Width) / 2f
                val tier3Y = tier2Y - tier3Height

                // Helper to draw a single tier
                fun drawTier(x: Float, y: Float, w: Float, h: Float) {
                    // Sponge inner visible edge
                    drawRoundRect(
                        color = spongeColor,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Frosting layer / outer coat
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                frostingColor.copy(alpha = 0.85f),
                                frostingColor,
                                frostingColor.copy(alpha = 0.9f)
                            ),
                            startX = x,
                            endX = x + w
                        ),
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Tier border
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.08f),
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 2f)
                    )

                    // Frosting piping rosettes along top border
                    val rosettes = (w / 18f).toInt()
                    for (i in 0..rosettes) {
                        val rx = x + (i * 18f).coerceAtMost(w)
                        drawCircle(
                            color = frostingColor.copy(alpha = 0.95f),
                            radius = 4f,
                            center = Offset(rx, y)
                        )
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.7f),
                            radius = 1.5f,
                            center = Offset(rx, y)
                        )
                    }

                    // Bottom beaded border
                    for (i in 0..rosettes) {
                        val rx = x + (i * 18f).coerceAtMost(w)
                        drawCircle(
                            color = frostingColor,
                            radius = 3f,
                            center = Offset(rx, y + h)
                        )
                    }
                }

                // Render Tiers from bottom to top
                drawTier(tier1X, tier1Y, tier1Width, tier1Height)

                var topTierY = tier1Y
                var topTierX = tier1X
                var topTierW = tier1Width

                if (tiersCount >= 2) {
                    drawTier(tier2X, tier2Y, tier2Width, tier2Height)
                    topTierY = tier2Y
                    topTierX = tier2X
                    topTierW = tier2Width
                }

                if (tiersCount >= 3) {
                    drawTier(tier3X, tier3Y, tier3Width, tier3Height)
                    topTierY = tier3Y
                    topTierX = tier3X
                    topTierW = tier3Width
                }

                // Candles and Sparkles on top tier
                val candleCount = if (tiersCount == 1) 3 else 5
                val candleSpacing = topTierW / (candleCount + 1)
                for (i in 1..candleCount) {
                    val cx = topTierX + (i * candleSpacing)
                    val cy = topTierY - 18f

                    // Candle stick
                    drawLine(
                        color = Color(0xFFE91E63),
                        start = Offset(cx, topTierY),
                        end = Offset(cx, cy),
                        strokeWidth = 4f
                    )

                    // Wick
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(cx, cy),
                        end = Offset(cx, cy - 4f),
                        strokeWidth = 1.5f
                    )

                    // Glowing flame
                    val flamePath = Path().apply {
                        moveTo(cx, cy - 14f)
                        cubicTo(cx + 4f, cy - 10f, cx + 4f, cy - 6f, cx, cy - 4f)
                        cubicTo(cx - 4f, cy - 6f, cx - 4f, cy - 10f, cx, cy - 14f)
                        close()
                    }
                    drawPath(flamePath, color = Color(0xFFFF9800))
                    drawCircle(
                        color = Color(0xFFFFEB3B),
                        radius = 2.5f,
                        center = Offset(cx, cy - 7f)
                    )
                }

                // Sparkle stars
                drawCircle(color = Color(0xFFFFD700), radius = 2.5f, center = Offset(topTierX - 10f, topTierY - 8f))
                drawCircle(color = Color(0xFFFFD700), radius = 2.5f, center = Offset(topTierX + topTierW + 10f, topTierY - 8f))
            }

            // Real-time custom writing on cake display ribbon
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF3D6))
                    .border(1.dp, GoldenCaramel.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (customPipingText.isNotBlank()) "Hand-Piped: \"$customPipingText\"" else "Custom message piping here",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkAmber,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
