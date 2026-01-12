package io.fusionpowered.bluemoon.Helpers

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified T : Parcelable> Intent.getParceableExtraSimplified(key: String) : T?  {
    if (Build.VERSION.SDK_INT >= 33)
        return getParcelableExtra(key, T::class.java)
    else
        @Suppress("DEPRECATION")
        return getParcelableExtra(key)
}