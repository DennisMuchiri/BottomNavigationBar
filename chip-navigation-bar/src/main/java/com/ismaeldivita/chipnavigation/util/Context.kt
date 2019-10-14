package com.ismaeldivita.chipnavigation.util

import android.content.Context
import android.support.annotation.AttrRes
import android.util.TypedValue
//import androidx.annotation.AttrRes

/**
 * Get value from attribute
 *
 * @param attr resource id
 * @return attribute value
 */
internal fun Context.getValueFromAttr(
    @AttrRes attr: Int
): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}