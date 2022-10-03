package com.manchuan.tools.about

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.itxca.spannablex.activateClick
import com.itxca.spannablex.spannable
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.MultiTypeAdapter
import com.manchuan.tools.activity.PrivacyActivity
import com.manchuan.tools.activity.WebActivity
import com.manchuan.tools.activity.info.AppLocalActivity
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.extensions.addAlpha
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.textColorPrimary

/**
 * @author drakeet
 */
abstract class AbsAboutActivity : AnimationActivity() {
    var toolbar: Toolbar? = null
        private set
    var collapsingToolbar: CollapsingToolbarLayout? = null
        private set
    private var headerContentLayout: MaterialCardView? = null
    var items: MutableList<Any>? = null
        private set
    var adapter: MultiTypeAdapter? = null
        private set
    var sloganTextView: TextView? = null
        private set
    private var versionTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    var imageLoader: ImageLoader? = null
        private set
    private var initialized = false
    var onRecommendationClickedListener: OnRecommendationClickedListener? = null
    var onContributorClickedListener: OnContributorClickedListener? = null
    protected abstract fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView)
    protected abstract fun onItemsCreated(items: MutableList<Any>)
    private fun onTitleViewCreated() {}

    @SuppressLint("NotifyDataSetChanged")
    fun setImageLoader(imageLoader: ImageLoader) {
        this.imageLoader = imageLoader
        if (initialized) {
            adapter!!.notifyDataSetChanged()
        }
    }

    @LayoutRes
    protected fun layoutRes(): Int {
        return R.layout.about_page_main_activity
    }


    val COUNTS = 3 // 点击次数

    val DURATION: Long = 1800 // 规定有效时间

    var mHits = LongArray(COUNTS)

    private fun continuousClick() {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(COUNTS) //重新初始化数组
            startActivity<AppLocalActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(layoutRes())
        toolbar = findViewById(R.id.toolbar)
        toolbar?.apply {
            setNavigationOnClickListener {
                finish()
            }
            title = "关于"
        }
        val icon = findViewById<ImageView>(R.id.icon)
        sloganTextView = findViewById(R.id.slogan)
        versionTextView = findViewById(R.id.version)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        headerContentLayout = findViewById(R.id.header_content_layout)
        val containers = findViewById<LinearLayout>(R.id.containers)
        val privacy = findViewById<TextView>(R.id.privacy)
        val useKotlin = findViewById<MaterialButton>(R.id.use_kotlin)
        containers.addNavigationBarHeightToMarginBottom()
        useKotlin.doOnClick {
            startActivity<WebActivity>("url" to "https://kotlinlang.org/")
        }
        headerContentLayout?.doOnClick {
            continuousClick()
        }
        privacy.activateClick(false).text = spannable {
            "用户协议".span {
                color(colorPrimary())
                clickable(onClick = { view: View, s: String ->
                    startActivity<PrivacyActivity>("type" to 2)
                })
            }
            " | ".color(textColorPrimary().addAlpha(0.5F))
            "隐私政策".span {
                color(colorPrimary())
                clickable(onClick = { view: View, s: String ->
                    startActivity<PrivacyActivity>("type" to 1)
                })
            }
        }
        onTitleViewCreated()
        onCreateHeader(icon, sloganTextView!!, versionTextView!!)
        onApplyPresetAttrs()
        recyclerView = findViewById(R.id.list)
        applyEdgeToEdge()
    }

    private var givenInsetsToDecorView = false
    private fun applyEdgeToEdge() {
        val window = window
        val navigationBarColor = ContextCompat.getColor(this, R.color.about_page_navigationBarColor)
        window.navigationBarColor = navigationBarColor
        val appBarLayout = findViewById<AppBarLayout>(R.id.header_layout)
        val decorView = window.decorView
        val originalRecyclerViewPaddingBottom = recyclerView!!.paddingBottom
        givenInsetsToDecorView = false
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v: View?, windowInsets: WindowInsetsCompat ->
            val navigationBarsInsets =
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val isGestureNavigation =
                navigationBarsInsets.bottom <= 24 * resources.displayMetrics.density
            if (!isGestureNavigation) {
                ViewCompat.onApplyWindowInsets(decorView, windowInsets)
                givenInsetsToDecorView = true
            } else if (givenInsetsToDecorView) {
                ViewCompat.onApplyWindowInsets(
                    decorView, WindowInsetsCompat.Builder().setInsets(
                        WindowInsetsCompat.Type.navigationBars(), Insets.of(
                            navigationBarsInsets.left,
                            navigationBarsInsets.top,
                            navigationBarsInsets.right,
                            0
                        )
                    ).build()
                )
            }
            decorView.setPadding(
                windowInsets.systemWindowInsetLeft,
                decorView.paddingTop,
                windowInsets.systemWindowInsetRight,
                decorView.paddingBottom
            )
            appBarLayout.setPadding(
                appBarLayout.paddingLeft,
                windowInsets.systemWindowInsetTop,
                appBarLayout.paddingRight,
                appBarLayout.paddingBottom
            )
            recyclerView!!.setPadding(
                recyclerView!!.paddingLeft,
                recyclerView!!.paddingTop,
                recyclerView!!.paddingRight,
                originalRecyclerViewPaddingBottom + navigationBarsInsets.bottom
            )
            windowInsets
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        adapter = MultiTypeAdapter()
        adapter!!.register(Category::class.java, CategoryViewBinder())
        adapter!!.register(Card::class.java, CardViewBinder())
        adapter!!.register(Line::class.java, LineViewBinder())
        adapter!!.register(Contributor::class.java, ContributorViewBinder(this))
        adapter!!.register(License::class.java, LicenseViewBinder())
        adapter!!.register(Recommendation::class.java, RecommendationViewBinder(this))
        items = ArrayList()
        onItemsCreated(items!!)
        adapter!!.items = items!!
        adapter!!.setHasStableIds(true)
        recyclerView!!.addItemDecoration(DividerItemDecoration(adapter!!))
        recyclerView!!.adapter = adapter
        initialized = true
    }

    private fun onApplyPresetAttrs() {
        val a = obtainStyledAttributes(R.styleable.AbsAboutActivity)
        val headerBackground = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderBackground)
        headerBackground?.let { setHeaderBackground(it) }
        val headerContentScrim =
            a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderContentScrim)
        headerContentScrim?.let { setHeaderContentScrim(it) }
        @ColorInt val headerTextColor =
            a.getColor(R.styleable.AbsAboutActivity_aboutPageHeaderTextColor, -1)
        if (headerTextColor != -1) {
            setHeaderTextColor(headerTextColor)
        }
        val navigationIcon = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageNavigationIcon)
        navigationIcon?.let { setNavigationIcon(it) }
        a.recycle()
    }

    /**
     * Use [.setHeaderBackground] instead.
     *
     * @param resId The resource id of header background
     */
    @Deprecated("")
    fun setHeaderBackgroundResource(@DrawableRes resId: Int) {
        setHeaderBackground(resId)
    }

    fun setHeaderBackground(@DrawableRes resId: Int) {
        setHeaderBackground(ContextCompat.getDrawable(this, resId)!!)
    }

    fun setHeaderBackground(drawable: Drawable) {
        ViewCompat.setBackground(headerContentLayout!!, drawable)
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     */
    fun setHeaderContentScrim(drawable: Drawable) {
        collapsingToolbar!!.contentScrim = drawable
    }

    fun setHeaderContentScrim(@DrawableRes resId: Int) {
        ContextCompat.getDrawable(this, resId)?.let { setHeaderContentScrim(it) }
    }

    fun setHeaderTextColor(@ColorInt color: Int) {
        collapsingToolbar!!.setCollapsedTitleTextColor(color)
        sloganTextView!!.setTextColor(color)
        versionTextView!!.setTextColor(color)
    }

    /**
     * Set the icon to use for the toolbar's navigation button.
     *
     * @param resId Resource ID of a drawable to set
     */
    fun setNavigationIcon(@DrawableRes resId: Int) {
        toolbar!!.setNavigationIcon(resId)
    }

    fun setNavigationIcon(drawable: Drawable) {
        toolbar!!.navigationIcon = drawable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
