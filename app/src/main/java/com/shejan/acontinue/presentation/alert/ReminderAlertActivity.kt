package com.shejan.acontinue.presentation.alert

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.shejan.acontinue.core.notification.AlarmScheduler
import com.shejan.acontinue.core.notification.ReminderSoundManager
import com.shejan.acontinue.data.local.ContinueDatabase
import com.shejan.acontinue.data.mapper.toDomain
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.presentation.create.getCategoryIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAlertActivity : ComponentActivity() {

    private var activityId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen up and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        activityId = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_TITLE) ?: "Reminder"
        val location = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_LOCATION).orEmpty()
        val startTime = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_START_TIME, System.currentTimeMillis())
        val priorityStr = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_PRIORITY)
        val priority = runCatching { ActivityPriority.valueOf(priorityStr.orEmpty()) }.getOrDefault(ActivityPriority.MEDIUM)
        val categoryName = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_CATEGORY) ?: "My reminders"

        // Trigger priority-appropriate audio & haptic feedback
        if (priority == ActivityPriority.HIGH) {
            ReminderSoundManager.startContinuousAlarm(this)
        } else if (priority == ActivityPriority.MEDIUM) {
            ReminderSoundManager.playShortAlert(this)
        }

        setContent {
            ReminderAlertContent(
                title = title,
                location = location,
                startTime = startTime,
                priority = priority,
                categoryName = categoryName,
                onComplete = { completeReminder() },
                onSnooze = { snoozeReminder() },
                onDismiss = { dismissAlert() }
            )
        }
    }

    override fun onDestroy() {
        ReminderSoundManager.stop()
        super.onDestroy()
    }

    private fun completeReminder() {
        ReminderSoundManager.stop()
        val id = activityId
        if (id > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = ContinueDatabase.getInstance(applicationContext).activityDao()
                    dao.updateCompletionStatus(id, true)
                    AlarmScheduler(applicationContext).cancel(id)
                } finally {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(id.toInt())
                    withContext(Dispatchers.Main) {
                        finishAndRemoveTask()
                    }
                }
            }
        } else {
            finishAndRemoveTask()
        }
    }

    private fun snoozeReminder() {
        ReminderSoundManager.stop()
        val id = activityId
        if (id > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = ContinueDatabase.getInstance(applicationContext).activityDao()
                    val entity = dao.getActivityByIdSync(id)
                    if (entity != null) {
                        val snoozeMillis = System.currentTimeMillis() + (10 * 60 * 1000L)
                        val snoozed = entity.toDomain().copy(
                            startTimeMillis = snoozeMillis,
                            reminderMinutesList = listOf(0),
                            reminderMinutesBefore = 0
                        )
                        AlarmScheduler(applicationContext).schedule(snoozed)
                    }
                } finally {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(id.toInt())
                    withContext(Dispatchers.Main) {
                        finishAndRemoveTask()
                    }
                }
            }
        } else {
            finishAndRemoveTask()
        }
    }

    private fun dismissAlert() {
        ReminderSoundManager.stop()
        val id = activityId
        if (id > 0) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(id.toInt())
        }
        finishAndRemoveTask()
    }
}

@Composable
private fun ReminderAlertContent(
    title: String,
    location: String,
    startTime: Long,
    priority: ActivityPriority,
    categoryName: String,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isStrong = priority == ActivityPriority.HIGH
    val accentRed = Color(0xFFEF5350)
    val accentBlue = Color(0xFF3897F0)

    val timeString = if (startTime > 0) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(startTime))
    } else ""

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isStrong) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        color = Color(0xFF000000),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Dismiss X Button & Priority Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Tag Pill
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isStrong) accentRed else Color(0xFF38383A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = priority.reminderTitle.uppercase(),
                        color = if (isStrong) accentRed else Color(0xFF8E8E93),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Content: Pulsing Icon, Category, Time, Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing Icon Frame
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .border(1.5.dp, if (isStrong) accentRed else accentBlue, CircleShape)
                        .background(Color(0xFF141416), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStrong) Icons.Outlined.Alarm else Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = if (isStrong) accentRed else accentBlue,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Category Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(categoryName.lowercase()),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = categoryName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Display in Monospace
                if (timeString.isNotEmpty()) {
                    Text(
                        text = timeString,
                        color = Color(0xFF8E8E93),
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Reminder Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 $location",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Bottom Actions: Snooze & Complete Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Complete Button
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Complete",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Snooze Button (10 mins)
                OutlinedButton(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF38383A)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Snooze,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze 10m",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
