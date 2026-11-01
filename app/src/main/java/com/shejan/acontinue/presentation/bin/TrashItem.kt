package com.shejan.acontinue.presentation.bin

import com.shejan.acontinue.domain.model.EntryType

data class TrashItem(
    val id: Long,
    val isActivity: Boolean,
    val title: String,
    val previewText: String,
    val entryType: EntryType,
    val deletedAtMillis: Long,
    val colorHex: Long = 0L,
    val categoryName: String = ""
) {
    val uniqueKey: String
        get() = if (isActivity) "act_$id" else "note_$id"
}

