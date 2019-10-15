package com.ismaeldivita.chipnavigation

//import androidx.annotation.MenuRes
//import androidx.coordinatorlayout.widget.CoordinatorLayout
//import androidx.annotation.IntDef
//import androidx.annotation.IntRange
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.support.annotation.FontRes
import android.support.annotation.IntRange
import android.support.annotation.MenuRes
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.ismaeldivita.chipnavigation.behavior.HideOnScrollBehavior
import com.ismaeldivita.chipnavigation.model.MenuItem
import com.ismaeldivita.chipnavigation.model.MenuParser
import com.ismaeldivita.chipnavigation.util.applyWindowInsets
import com.ismaeldivita.chipnavigation.util.forEachChild
import com.ismaeldivita.chipnavigation.util.getChildren
import com.ismaeldivita.chipnavigation.view.HorizontalMenuItemView
import com.ismaeldivita.chipnavigation.view.MenuItemView
import com.ismaeldivita.chipnavigation.view.VerticalMenuItemView


class ChipNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), CoordinatorLayout.AttachedBehavior {

    private lateinit var orientationMode: MenuOrientation
    private val behavior: HideOnScrollBehavior
    private var listener: OnItemSelectedListener? = null
    private var minimumExpandedWidth: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChipNavigationBar)

        val menuFontFamily = a.getResourceId(R.styleable.ChipNavigationBar_cnb_menuFontFamily,-1)
        val menuResource = a.getResourceId(R.styleable.ChipNavigationBar_cnb_menuResource, -1)
        val hideOnScroll = a.getBoolean(R.styleable.ChipNavigationBar_cnb_hideOnScroll, false)
        val minExpanded = a.getDimension(R.styleable.ChipNavigationBar_cnb_minExpandedWidth, 0F)
        val leftInset = a.getBoolean(R.styleable.ChipNavigationBar_cnb_addLeftInset, false)
        val topInset = a.getBoolean(R.styleable.ChipNavigationBar_cnb_addTopInset, false)
        val rightInset = a.getBoolean(R.styleable.ChipNavigationBar_cnb_addRightInset, false)
        val bottomInset = a.getBoolean(R.styleable.ChipNavigationBar_cnb_addBottomInset, false)
        val orientation = when (a.getInt(R.styleable.ChipNavigationBar_cnb_orientationMode, 0)) {
            0 -> MenuOrientation.HORIZONTAL
            1 -> MenuOrientation.VERTICAL
            else -> MenuOrientation.HORIZONTAL
        }

        a.recycle()

        behavior = HideOnScrollBehavior(context, attrs)
        setMenuOrientation(orientation)

        if (menuResource >= 0) {
            setMenuResource(menuResource,menuFontFamily)
        }

        setMinimumExpandedWidth(minExpanded.toInt())
        setHideOnScroll(hideOnScroll)
        applyWindowInsets(leftInset, topInset, rightInset, bottomInset)
        collapse()

        isClickable = true
    }

    /**
     * Inflate a menu from the specified XML resource
     *
     * @param menuRes Resource ID for an XML layout resource to load
     */
    fun setMenuResource(@MenuRes menuRes: Int,@FontRes menuFontFamily:Int) {
        var typeface:Typeface?
        typeface=null;
        if(menuFontFamily!=-1) {
            try {
                typeface = ResourcesCompat.getFont(context, menuFontFamily)
            }catch (e: Exception) {
               e.printStackTrace();
            }
        }
        val menu = (MenuParser(context).parse(menuRes))
        val childListener: (View) -> Unit = { view ->
            val id = view.id
            setItemSelected(id)
            listener?.onItemSelected(id)
        }

        removeAllViews()
        menu.items.forEach {
            val itemView = createMenuItem().apply {
                bind(it,typeface)
                setOnClickListener(childListener)
            }
            addView(itemView)
        }

        /*val mm_typeface = Typeface.createFromAsset(context.assets,"mm.ttf")
        for (i in 0 until menu.items.size) {
            val menuItem = menu.items.get(i)
            if (menuItem != null) {
                applyFont(menuItem, mm_typeface)
            }
        }*/
    }
   fun applyFont(menuItem: MenuItem, mm_typeface:Typeface){
       val spannableString = SpannableString(menuItem.title)
       spannableString.setSpan(
           CustomTypeFace("", mm_typeface),
           0,
           spannableString.length,
           Spanned.SPAN_INCLUSIVE_INCLUSIVE
       )
       menuItem.title=spannableString
   }

    /**
     * Set the menu orientation
     *
     * @param mode orientation
     */
    fun setMenuOrientation(menuOrientation: MenuOrientation) {
        orientationMode = menuOrientation
        orientation = when (menuOrientation) {
            MenuOrientation.HORIZONTAL -> HORIZONTAL
            MenuOrientation.VERTICAL -> VERTICAL
        }
        setHideOnScroll(behavior.scrollEnabled)
    }

    /**
     * Set the enabled state for the menu item with the provided [id]
     *
     * @param id menu item id
     * @param isEnabled true if this view is enabled, false otherwise
     */
    fun setItemEnabled(id: Int, isEnabled: Boolean) {
        getItemById(id)?.isEnabled = isEnabled
    }

    /**
     * Remove the selected state from the current item and set the selected state to true
     * for the menu item with the [id]
     *
     * This event will not be propagated to the current [OnItemSelectedListener]
     *
     * @param id menu item id
     */
    fun setItemSelected(id: Int) {
        val selectedItem = getSelectedItem()

        if (selectedItem?.id != id) {
            selectedItem?.isSelected = false
            getItemById(id)?.isSelected = true
        }
    }

    /**
     * Set the enabled state for the hide on scroll [CoordinatorLayout.Behavior].
     * The behavior is only active when orientation mode is HORIZONTAL
     *
     * @param isEnabled True if this view is enabled, false otherwise
     */
    fun setHideOnScroll(isEnabled: Boolean) {
        behavior.scrollEnabled = isEnabled && orientationMode == MenuOrientation.HORIZONTAL
    }

    /**
     * Set the minimum width for the vertical expanded state.
     *
     * @param minExpandedWidth width in pixels
     */
    fun setMinimumExpandedWidth(minExpandedWidth: Int) {
        minimumExpandedWidth = minExpandedWidth
    }

    /**
     * Set the duration of the enter animation for the hide on scroll [CoordinatorLayout.Behavior]
     * Default value [HideOnScrollBehavior.DEFAULT_ENTER_DURATION]
     * The behavior is only active when orientation orientationMode is HORIZONTAL
     *
     * @param duration animation duration in milliseconds
     */
    fun setEnterAnimationDuration(duration: Long) {
        behavior.enterAnimationDuration = duration
    }

    /**
     * Set the duration of the exit animation for the hide on scroll [CoordinatorLayout.Behavior]
     * Default value [HideOnScrollBehavior.DEFAULT_EXIT_DURATION]
     * The behavior is only active when orientation is HORIZONTAL
     *
     * @param duration animation duration in milliseconds
     */
    fun setExitAnimationDuration(duration: Long) {
        behavior.exitAnimationDuration = duration
    }

    /**
     * Register a callback to be invoked when a menu item is selected
     *
     * @param listener The callback that will run
     */
    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.listener = listener
    }

    /**
     * Register a callback to be invoked when a menu item is selected
     *
     * @param block The callback that will run
     */
    fun setOnItemSelectedListener(block: (Int) -> Unit) {
        setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                block(id)
            }
        })
    }

    /**
     * Display a notification numberless badge for a menu item
     *
     * @param id menu item id
     */
    fun showBadge(id: Int) {
        getItemById(id)?.showBadge()
    }

    /**
     * Display a notification badge with a counter for a menu item
     * The maximum digits length to be displayed is 2 otherwise "+99" will be displayed
     *
     * @param id menu item id
     */
    fun showBadge(id: Int, @IntRange(from = 1) count: Int) {
        getItemById(id)?.showBadge(maxOf(count, 0))
    }

    /**
     * Dismiss the displayed badge for a menu item
     *
     * @param id menu item id
     */
    fun dismissBadge(id: Int) {
        getItemById(id)?.dismissBadge()
    }

    /**
     * Show menu if the orientationMode is HORIZONTAL otherwise, do nothing
     */
    fun show() {
        if (orientationMode == MenuOrientation.HORIZONTAL) {
            behavior.slideUp(this)
        }
    }

    /**
     * Hide menu if the orientationMode is HORIZONTAL otherwise, do nothing
     */
    fun hide() {
        if (orientationMode == MenuOrientation.HORIZONTAL) {
            behavior.slideDown(this)
        }
    }

    /**
     * Collapse the menu items if orientationMode is VERTICAL otherwise, do nothing
     */
    fun collapse() {
        if (orientationMode == MenuOrientation.VERTICAL) {
            forEachChild {
                it.minimumWidth = 0
                (it as? VerticalMenuItemView)?.collapse()
            }
        }
    }

    /**
     * Expand the menu items if orientationMode is VERTICAL otherwise, do nothing
     */
    fun expand() {
        if (orientationMode == MenuOrientation.VERTICAL) {
            forEachChild {
                it.minimumWidth = minimumExpandedWidth
                (it as? VerticalMenuItemView)?.expand()
            }
        }
    }

    /**
     * Return the selected menu item id
     *
     * @return menu item id or -1 if none item is selected
     */
    fun getSelectedItemId(): Int = getSelectedItem()?.id ?: -1

    /**
     * Return the current selected menu item
     *
     * @return the selected menu item view or null if none is selected
     */
    private fun getSelectedItem() = getChildren().firstOrNull { it.isSelected }

    /**
     * Return a menu item view with provided [id]
     *
     * @param id menu item id
     * @return the menu item view or null if the id was not found
     */
    private fun getItemById(id: Int) = getChildren()
        .filterIsInstance<MenuItemView>()
        .firstOrNull { it.id == id }

    /**
     * Create a menu item view based on the menu orientationMode
     *
     * @return a new [MenuItemView] instance
     */
    private fun createMenuItem(): MenuItemView = when (orientationMode) {
        MenuOrientation.HORIZONTAL -> HorizontalMenuItemView(context)
        MenuOrientation.VERTICAL -> VerticalMenuItemView(context)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behavior

    /**
     * Interface definition for a callback to be invoked when a menu item is selected
     */
    interface OnItemSelectedListener {
        /**
         * Called when a item has been selected
         *
         * @param id menu item id
         */
        fun onItemSelected(id: Int)
    }

    enum class MenuOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private inner class CustomTypeFace(family: String, val typefaceVal: Typeface) : TypefaceSpan(family) {

        override fun updateDrawState(textPaint: TextPaint) {
            applyCustomTypeFace(textPaint, typefaceVal)
        }

        override fun updateMeasureState(textPaint: TextPaint) {
            applyCustomTypeFace(textPaint, typefaceVal)
        }

        fun applyCustomTypeFace(paint: Paint, typeface: Typeface) {
            val oldStyle: Int
            val old = paint.getTypeface()
            if (old == null) {
                oldStyle = 0
            } else {
                oldStyle = old!!.getStyle()
            }

            val fake = oldStyle and typeface.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.setFakeBoldText(true)
            }

            if (fake and Typeface.ITALIC != 0) {
                paint.setTextSkewX(-0.25f)
            }
            paint.setTypeface(typeface)
        }
    }
}