package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.PistachioLight

@Composable
fun PureVegIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.5.dp, PistachioGreen, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(PistachioGreen)
        )
    }
}

@Composable
fun VegEgglessBadge(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PistachioLight)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        PureVegIcon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "100% Pure Veg",
            style = MaterialTheme.typography.labelSmall.copy(
                color = PistachioGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun GlutenFreeBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFF3E0))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Gluten-Free",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFE65100),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun NutFreeBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE3F2FD))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Nut-Free",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun LeadTimeBadge(leadTimeHours: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Preparation lead time",
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = if (leadTimeHours <= 2) "⚡ ${leadTimeHours}h prep" else "🎂 ${leadTimeHours}h notice",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}
