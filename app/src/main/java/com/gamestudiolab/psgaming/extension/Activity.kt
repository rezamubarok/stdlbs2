package com.gamestudiolab.psgaming.extension

import android.app.Activity
import android.app.AlertDialog
import android.os.Environment
import java.io.File


fun Activity.getExtensionPath(): String? {
    val folder = File(Environment.getExternalStorageDirectory(), "Android/obb/$packageName")
    if (folder.exists()) {
        folder.listFiles()?.forEach { file ->
            if (file.absolutePath.endsWith(".obb")) {
                return file.absolutePath
            }
        }
    }

    return null
}

fun Activity.getGameFolder(): String {
    val folder = File(filesDir, "data").also { if (!it.exists()) it.mkdir() }
    return folder.absolutePath
}

fun Activity.getGamePath(): String? {
    val folder = File(filesDir, "data").also { if (!it.exists()) it.mkdir() }
    folder.listFiles()?.forEach { file ->
        if (file.absolutePath.endsWith(".iso")) {
            return file.absolutePath
        }
    }

    return null
}

fun Activity.showDialog(
    title: String? = null,
    message: String? = null,
    positive: String? = null,
    negative: String? = null,
    cancelable: Boolean? = false,
    positiveAction: (() -> Unit)? = null,
    negativeAction: (() -> Unit)? = null
) {
    AlertDialog.Builder(this).apply {
        title?.let { setTitle(it) }
        message?.let { setMessage(it) }
        cancelable?.let { setCancelable(it) }
        setPositiveButton((positive ?: getString(android.R.string.ok))) { _, _ ->
            positiveAction?.invoke()
        }
        negative?.let {
            setNegativeButton(it) { _, _ -> negativeAction?.invoke() }
        }
    }.show()
}