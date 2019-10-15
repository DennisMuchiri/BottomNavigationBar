package com.ismaeldivita.chipnavigation.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ismaeldivita.chipnavigation.model.MenuItem

internal abstract class MenuItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    abstract fun bind(item: MenuItem, mytypeface: Typeface?)

    abstract fun showBadge(count: Int = 0)

    abstract fun dismissBadge()

}