package com.amazonaws.ivs.player.scrollablefeed.common

import timber.log.Timber

private const val TIMBER_TAG = "ScrollableFeed"

class LineNumberDebugTree : Timber.DebugTree() {
    private var method = ""

    override fun createStackElementTag(element: StackTraceElement): String {
        method = "#${element.methodName}"
        return "(${element.fileName}:${element.lineNumber})"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, "${TIMBER_TAG}: $method: $message", t)
    }
}
