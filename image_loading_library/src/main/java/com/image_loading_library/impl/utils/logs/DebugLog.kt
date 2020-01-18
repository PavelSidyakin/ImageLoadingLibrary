package com.image_loading_library.impl.utils.logs

import com.image_loading_library.BuildConfig

// Calling of a logs functions will be cut in release build (prevent creation of a log message in release build)

inline fun log(block: XLog.() -> Unit) {
    if (BuildConfig.DEBUG) XLog.block()
}
