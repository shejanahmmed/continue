package com.shejan.acontinue.domain.model

enum class EntryType {
    ACTIVITY,
    REMINDER
}

enum class ActivityCategory(val displayName: String, val colorHex: Long) {
    CLASS("Class", 0xFFE5A93C),     // Amber / Gold (matches reference image event accent)
    LAB("Lab", 0xFF00BFA5),         // Mint / Teal
    MEETING("Meeting", 0xFF7E57C2), // Royal Purple
    PROJECT("Project", 0xFFD71921), // Utilitarian Accent Red
    PERSONAL("Personal", 0xFF3D82F6), // Azure Blue
    OTHER("Other", 0xFF4CAF50)      // Emerald Green
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
