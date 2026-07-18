package com.rohlicek.rateio.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast


fun copyTextToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("copied_text", text)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}