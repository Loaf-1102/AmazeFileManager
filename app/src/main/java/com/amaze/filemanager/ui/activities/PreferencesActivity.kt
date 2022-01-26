/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import com.amaze.filemanager.R
import com.amaze.filemanager.ui.activities.superclasses.ThemedActivity
import com.amaze.filemanager.ui.colors.ColorPreferenceHelper
import com.amaze.filemanager.ui.fragments.preference_fragments.BasePrefsFragment
import com.amaze.filemanager.ui.fragments.preference_fragments.PreferencesConstants
import com.amaze.filemanager.ui.fragments.preference_fragments.PrefsFragment
import com.amaze.filemanager.ui.theme.AppTheme
import com.amaze.filemanager.utils.PreferenceUtils
import com.amaze.filemanager.utils.Utils
import com.readystatesoftware.systembartint.SystemBarTintManager
import java.io.File

class PreferencesActivity : ThemedActivity(), FolderChooserDialog.FolderCallback {
    val layout: View
        get() = _layout!!
    private var _layout: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        var savedInstanceState = savedInstanceState
        if (savedInstanceState == null && intent.hasExtra("savedInstanceState")) {
            savedInstanceState = intent.getBundleExtra("savedInstanceState")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        _layout = findViewById(R.id.activity_preferences)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.displayOptions =
            ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE
        initStatusBarResources(layout)

        if (savedInstanceState == null) {
            val fragment = PrefsFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences_container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }

        val x = Intent(this, MainActivity::class.java)
        x.action = Intent.ACTION_MAIN
        x.action = Intent.CATEGORY_LAUNCHER
        finish()
        startActivity(x)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else false
    }

    override fun recreate() {
        val bundle = Bundle()
        onSaveInstanceState(bundle)
        val intent = Intent(this, javaClass)
        intent.putExtra("savedInstanceState", bundle)

        finish()
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun pushFragment(fragment: BasePrefsFragment) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                R.anim.slide_out,
            )

            replace(R.id.preferences_container, fragment)
            supportActionBar?.title = getString(fragment.title)
            addToBackStack(null)
        }
    }

    fun invalidateNavBar() {
        val primaryColor = ColorPreferenceHelper
            .getPrimary(currentColorPreference, MainActivity.currentTab)
        if (Build.VERSION.SDK_INT == 20 || Build.VERSION.SDK_INT == 19) {
            val tintManager = SystemBarTintManager(this)
            tintManager.isStatusBarTintEnabled = true
            tintManager.setStatusBarTintColor(primaryColor)
            val p = findViewById<View>(R.id.activity_preferences).layoutParams
                as ViewGroup.MarginLayoutParams
            val config = tintManager.config
            p.setMargins(0, config.statusBarHeight, 0, 0)
        } else if (Build.VERSION.SDK_INT >= 21) {
            val colouredNavigation = getBoolean(PreferencesConstants.PREFERENCE_COLORED_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val tabStatusColor = PreferenceUtils.getStatusColor(primaryColor)
            window.statusBarColor = tabStatusColor
            if (colouredNavigation) {
                window.navigationBarColor = tabStatusColor
            } else if (window.navigationBarColor != Color.BLACK) {
                window.navigationBarColor = Color.BLACK
            }
        }
        if (appTheme == AppTheme.BLACK) {
            window.decorView.setBackgroundColor(Utils.getColor(this, android.R.color.black))
        }
    }

    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        supportFragmentManager.fragments.lastOrNull { it is BasePrefsFragment }?.let {
            (it as BasePrefsFragment).onFolderSelection(dialog, folder)
        }
    }

    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {
        supportFragmentManager.fragments.lastOrNull { it is BasePrefsFragment }?.let {
            (it as BasePrefsFragment).onFolderChooserDismissed(dialog)
        }
    }
}
