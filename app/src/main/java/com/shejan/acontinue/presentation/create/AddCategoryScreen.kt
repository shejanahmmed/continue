package com.shejan.acontinue.presentation.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

data class CategoryIconItem(
    val name: String,
    val icon: ImageVector
)

val CATEGORY_COLORS = listOf(
    0xFFFA7268, // Salmon / Pink (1)
    0xFFFFA726, // Orange (2)
    0xFFFFD54F, // Amber Yellow (3)
    0xFFAED581, // Lime Green (4)
    0xFF4FC3F7, // Sky Blue (5)
    0xFF4DD0E1, // Cyan / Mint (6)
    0xFFBA68C8, // Orchid / Magenta (7)
    0xFFB39DDB  // Lavender / Light Purple (8)
)

val CATEGORY_ICONS: List<CategoryIconItem> = listOf(
    // Row 1
    CategoryIconItem("bell", Icons.Filled.Notifications),
    CategoryIconItem("list", Icons.AutoMirrored.Outlined.FormatListBulleted),
    CategoryIconItem("home", Icons.Outlined.Home),
    CategoryIconItem("heart", Icons.Outlined.FavoriteBorder),
    CategoryIconItem("diamond", Icons.Outlined.Diamond),
    CategoryIconItem("lightbulb", Icons.Outlined.Lightbulb),

    // Row 2
    CategoryIconItem("gift", Icons.Outlined.CardGiftcard),
    CategoryIconItem("cake", Icons.Outlined.Cake),
    CategoryIconItem("cart", Icons.Outlined.ShoppingCart),
    CategoryIconItem("restaurant", Icons.Outlined.Restaurant),
    CategoryIconItem("soup", Icons.Outlined.SoupKitchen),
    CategoryIconItem("shield", Icons.Outlined.Shield),

    // Row 3
    CategoryIconItem("bank", Icons.Outlined.AccountBalance),
    CategoryIconItem("card", Icons.Outlined.CreditCard),
    CategoryIconItem("receipt", Icons.AutoMirrored.Outlined.ReceiptLong),
    CategoryIconItem("ticket", Icons.Outlined.ConfirmationNumber),
    CategoryIconItem("flight", Icons.Outlined.Flight),
    CategoryIconItem("luggage", Icons.Outlined.Luggage),

    // Row 4
    CategoryIconItem("train", Icons.Outlined.Train),
    CategoryIconItem("car", Icons.Outlined.DirectionsCar),
    CategoryIconItem("badge", Icons.Outlined.Badge),
    CategoryIconItem("game", Icons.Outlined.SportsEsports),
    CategoryIconItem("sports", Icons.Outlined.SportsBasketball),
    CategoryIconItem("trophy", Icons.Outlined.EmojiEvents),

    // Row 5
    CategoryIconItem("run", Icons.AutoMirrored.Outlined.DirectionsRun),
    CategoryIconItem("bar", Icons.Outlined.LocalBar),
    CategoryIconItem("business", Icons.Outlined.Apartment),
    CategoryIconItem("school", Icons.Outlined.School),
    CategoryIconItem("grad", Icons.Outlined.WorkspacePremium),
    CategoryIconItem("book", Icons.AutoMirrored.Outlined.MenuBook),

    // Row 6
    CategoryIconItem("edit", Icons.Outlined.Edit),
    CategoryIconItem("alarm", Icons.Outlined.AccessAlarm),
    CategoryIconItem("medical", Icons.Outlined.MedicalServices),
    CategoryIconItem("people", Icons.Outlined.People),
    CategoryIconItem("child", Icons.Outlined.Face),
    CategoryIconItem("pets", Icons.Outlined.Pets),

    // Row 7
    CategoryIconItem("eco", Icons.Outlined.Eco),
    CategoryIconItem("public", Icons.Outlined.Public),
    CategoryIconItem("bookmark", Icons.Outlined.Bookmark),
    CategoryIconItem("article", Icons.AutoMirrored.Outlined.Article),
    CategoryIconItem("build", Icons.Outlined.Build),
    CategoryIconItem("bolt", Icons.Outlined.Bolt)
)

fun getCategoryIcon(name: String): ImageVector {
    return CATEGORY_ICONS.firstOrNull { it.name == name }?.icon ?: Icons.Filled.Notifications
}

@Composable
fun AddCategoryScreen(
    initialCategory: ReminderCategory? = null,
    onSaveCategory: (name: String, colorHex: Long, iconName: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val scrollState = rememberScrollState()

    var categoryName by remember(initialCategory) {
        mutableStateOf(initialCategory?.name ?: "Category 1")
    }
    var selectedColorHex by remember(initialCategory) {
        mutableLongStateOf(initialCategory?.colorHex ?: 0xFFB39DDB)
    }
    var selectedIconName by remember(initialCategory) {
        mutableStateOf(initialCategory?.iconName ?: "bell")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Card with overlapping circular icon preview badge
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Name Input Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = colors.cardBackground,
                    border = BorderStroke(1.dp, colors.borderStroke)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 44.dp, bottom = 20.dp)
                    ) {
                        Text(
                            text = "Category name",
                            color = Color(0xFF3897F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        BasicTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(Color(0xFF3897F0)),
                            singleLine = true
                        )

                        HorizontalDivider(
                            color = colors.textPrimary.copy(alpha = 0.8f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Floating Circular Icon Preview Badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF222226),
                    border = BorderStroke(1.dp, colors.borderStroke.copy(alpha = 0.6f)),
                    modifier = Modifier.size(76.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(selectedIconName),
                            contentDescription = "Selected icon preview",
                            tint = Color(selectedColorHex),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Card: Color Palette + 42-Icon Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.borderStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    // Color Palette Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CATEGORY_COLORS.forEach { color ->
                            val isSelected = color == selectedColorHex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(color), CircleShape)
                                    .clickable { selectedColorHex = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Selected color",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        color = colors.borderStroke.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6-Column Icon Grid
                    val iconRows = CATEGORY_ICONS.chunked(6)
                    iconRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowItems.forEach { item ->
                                val isSelected = item.name == selectedIconName
                                Box(
                                    modifier = Modifier.size(46.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3897F0), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable { selectedIconName = item.name },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.name,
                                                tint = Color(0xFF8E8E93),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Dock: Cancel | Save
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF3A3A3C).copy(alpha = 0.95f),
            border = BorderStroke(1.dp, colors.borderStroke)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = onNavigateBack)
                        .padding(horizontal = 22.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(colors.borderStroke.copy(alpha = 0.6f))
                )

                val isSaveEnabled = categoryName.isNotBlank()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(enabled = isSaveEnabled) {
                            onSaveCategory(categoryName.trim(), selectedColorHex, selectedIconName)
                        }
                        .padding(horizontal = 22.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (isSaveEnabled) colors.textPrimary else colors.textDisabled,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
