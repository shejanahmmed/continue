package com.shejan.acontinue.domain.model

enum class EntryType {
    ACTIVITY,
    REMINDER
}

enum class ActivityCategory(val displayName: String, val colorHex: Long) {
    PERSONAL("Personal", 0xFF00A9F4), // Sky Blue (Row 1, #1 - matches selected in screenshot)
    PROJECT("Project", 0xFFE53935),   // Coral / Red (Row 1, #2)
    ROSE("Rose", 0xFFEC407A),         // Pink / Rose (Row 1, #3)
    ORANGE("Orange", 0xFFFF9800),     // Orange (Row 1, #4)
    CLASS("Class", 0xFFFFC107),       // Amber / Yellow (Row 1, #5)
    MINT("Mint", 0xFF00C853),         // Emerald / Mint (Row 1, #6)

    LIME("Lime", 0xFF7CB342),         // Lime Green (Row 2, #1)
    LAB("Lab", 0xFF00BCD4),           // Aqua / Cyan (Row 2, #2)
    INDIGO("Indigo", 0xFF5C6BC0),     // Cornflower Blue (Row 2, #3)
    COBALT("Cobalt", 0xFF1E88E5),     // Royal Blue (Row 2, #4)
    MEETING("Meeting", 0xFF9C27B0),   // Purple / Violet (Row 2, #5)
    OTHER("Other", 0xFF9E9E9E);       // Graphite / Gray (Row 2, #6)

    companion object {
        val EVENT_PALETTE_COLORS: List<ActivityCategory> = listOf(
            PERSONAL, PROJECT, ROSE, ORANGE, CLASS, MINT,
            LIME, LAB, INDIGO, COBALT, MEETING, OTHER
        )
    }
}

enum class ActivityPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class RecurrenceMode(val displayName: String) {
    NONE("Don't repeat"),
    DAILY("Daily"),
    WEEKDAYS("Every weekday"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

enum class ReminderOffset(val minutesBefore: Int, val displayName: String) {
    NONE(-1, "No early alert"),
    AT_TIME(0, "At time of event"),
    FIVE_MINUTES(5, "5 minutes before"),
    TEN_MINUTES(10, "10 minutes before"),
    FIFTEEN_MINUTES(15, "15 minutes before"),
    THIRTY_MINUTES(30, "30 minutes before"),
    ONE_HOUR(60, "1 hour before");

    companion object {
        fun fromMinutes(minutes: Int): ReminderOffset {
            return entries.firstOrNull { it.minutesBefore == minutes } ?: FIFTEEN_MINUTES
        }
    }
}
