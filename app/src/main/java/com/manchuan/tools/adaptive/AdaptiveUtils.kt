package com.manchuan.tools.adaptive

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigationrail.NavigationRailView
import com.manchuan.tools.R


/** Utility class for the Adaptive package.  */
internal object AdaptiveUtils {
    const val MEDIUM_SCREEN_WIDTH_SIZE = 600
    const val LARGE_SCREEN_WIDTH_SIZE = 1240

    /**
     * Updates the visibility of the main navigation view components according to screen size.
     *
     *
     * The small screen layout should have a bottom navigation and optionally a fab. The medium
     * layout should have a navigation rail with a fab, and the large layout should have a navigation
     * drawer with an extended fab.
     */
    fun updateNavigationViewLayout(
        screenWidth: Int,
        drawerLayout: DrawerLayout,
        modalNavDrawer: NavigationView,
        fab: FloatingActionButton?,
        bottomNav: BottomNavigationView,
        navRail: NavigationRailView,
        navDrawer: NavigationView,
        navFab: ExtendedFloatingActionButton,
    ) {
        // Set navigation menu button to show a modal navigation drawer in medium screens.
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        setNavRailButtonOnClickListener(
            drawerLayout, navRail.headerView!!.findViewById(R.id.nav_button), modalNavDrawer)
        setModalDrawerButtonOnClickListener(
            drawerLayout,
            modalNavDrawer.getHeaderView(0).findViewById(R.id.nav_button),
            modalNavDrawer)
        modalNavDrawer.setNavigationItemSelectedListener { item ->
            modalNavDrawer.setCheckedItem(item)
            drawerLayout.closeDrawer(modalNavDrawer)
            true
        }
        if (screenWidth < MEDIUM_SCREEN_WIDTH_SIZE) {
            // Small screen
            if (fab != null) {
                fab.visibility = View.VISIBLE
            }
            bottomNav.visibility = View.VISIBLE
            navRail.visibility = View.GONE
            navDrawer.visibility = View.GONE
        } else if (screenWidth < LARGE_SCREEN_WIDTH_SIZE) {
            // Medium screen
            if (fab != null) {
                fab.visibility = View.GONE
            }
            bottomNav.visibility = View.GONE
            navRail.visibility = View.VISIBLE
            navDrawer.visibility = View.GONE
            navFab.shrink()
        } else {
            // Large screen
            if (fab != null) {
                fab.visibility = View.GONE
            }
            bottomNav.visibility = View.GONE
            navRail.visibility = View.GONE
            navDrawer.visibility = View.VISIBLE
            navFab.extend()
        }
    }

    /* Sets navigation rail's header button to open the modal navigation drawer. */
    private fun setNavRailButtonOnClickListener(
        drawerLayout: DrawerLayout,
        navButton: View,
        modalDrawer: NavigationView,
    ) {
        navButton.setOnClickListener { drawerLayout.openDrawer(modalDrawer) }
    }

    /* Sets modal navigation drawer's header button to close the drawer. */
    private fun setModalDrawerButtonOnClickListener(
        drawerLayout: DrawerLayout,
        button: View,
        modalDrawer: NavigationView,
    ) {
        button.setOnClickListener { drawerLayout.closeDrawer(modalDrawer) }
    }
}