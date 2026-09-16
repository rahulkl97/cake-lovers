package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen

@Composable
fun CelebrationKitBanner(
    modifier: Modifier = Modifier,
    isCartPage: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HoneyButter)
            .border(1.dp, GoldenCaramel.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("celebration_kit_banner")
    ) {
        Icon(
            imageVector = Icons.Default.Celebration,
            contentDescription = "Complimentary Celebration Kit",
            tint = GoldenCaramel,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Free Celebration Kit Included",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkAmber,
                    fontSize = 13.sp
                )
            )
            Text(
                text = "Eco-friendly birchwood knife + 2 golden sparkler candles (₹0)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkAmber.copy(alpha = 0.8f),
                    fontSize = 11.5.sp
                )
            )
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Included free",
            tint = PistachioGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}
