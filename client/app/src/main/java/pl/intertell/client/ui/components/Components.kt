package pl.intertell.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun Card(
    modifier: Modifier = Modifier,
    background: Color = IntertellColors.White,
    border: Color? = IntertellColors.HairlineOnLight,
    radius: Int = 18,
    padding: Int = 18,
    content: @Composable ColumnScopeAlias.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.dp))
            .background(background)
            .then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(radius.dp)) else Modifier)
            .padding(padding.dp),
        content = content,
    )
}

// Alias so call sites don't need to import Column's scope explicitly.
typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope

@Composable
fun SolidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = IntertellColors.Accent,
    contentColor: Color = IntertellColors.White,
    height: Int = 54,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IntertellType.titleBold, color = contentColor)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 50,
    contentColor: Color = IntertellColors.TextPrimary,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(1.5.dp, IntertellColors.ToggleTrackOff.copy(alpha = 0.15f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IntertellType.bodyBold, color = contentColor)
    }
}

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = IntertellColors.Accent,
    inactiveColor: Color = IntertellColors.ToggleTrackOff,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) activeColor else inactiveColor)
            .then(if (enabled) Modifier.clickable { onCheckedChange(!checked) } else Modifier)
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(IntertellColors.White),
        )
    }
}

@Composable
fun StatusChip(text: String, foreground: Color, background: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text, style = IntertellType.chip, color = foreground)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = modifier.padding(bottom = 10.dp))
}

@Composable
fun MonoLabelValue(label: String, value: String, valueStyle: TextStyle = IntertellType.monoBold, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(11.dp))
            .background(IntertellColors.ScreenBackground)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Text(label, style = IntertellType.label, color = IntertellColors.Text50)
        Text(value, style = valueStyle, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IntertellColors.White)
            .border(1.dp, IntertellColors.Navy.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(label, style = IntertellType.label, color = IntertellColors.Text50)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = IntertellType.titleBold.copy(color = IntertellColors.TextPrimary),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                capitalization = KeyboardCapitalization.None,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Composable
fun BackLink(text: String, onClick: () -> Unit, color: Color = IntertellColors.Accent) {
    Text(
        text,
        style = IntertellType.bodyBold,
        color = color,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun ScrimOverlay(onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    // Dismiss is via an explicit "Anuluj" button inside [content], matching the
    // source design — the scrim itself doesn't intercept taps.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntertellColors.Navy.copy(alpha = 0.45f)),
        contentAlignment = Alignment.BottomCenter,
        content = content,
    )
}

@Composable
fun FullScreenOutcome(
    title: String,
    body: String,
    ctaText: String,
    onCta: () -> Unit,
    background: Color = IntertellColors.Navy,
    iconBackground: Color = IntertellColors.GreenCheck,
    icon: String = "✓",
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, style = IntertellType.headline, color = IntertellColors.White)
            }
            Text(
                title,
                style = IntertellType.headline,
                color = IntertellColors.White,
                modifier = Modifier.padding(top = 22.dp),
            )
            Text(
                body,
                style = IntertellType.body,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 10.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(IntertellColors.White.copy(alpha = 0.14f))
                    .clickable(onClick = onCta)
                    .padding(horizontal = 30.dp, vertical = 15.dp),
            ) {
                Text(ctaText, style = IntertellType.titleBold, color = IntertellColors.White)
            }
        }
    }
}

@Composable
fun RowSpaceBetween(modifier: Modifier = Modifier, verticalAlignment: Alignment.Vertical = Alignment.CenterVertically, content: @Composable RowScopeAlias.() -> Unit) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = verticalAlignment, content = content)
}

typealias RowScopeAlias = androidx.compose.foundation.layout.RowScope
