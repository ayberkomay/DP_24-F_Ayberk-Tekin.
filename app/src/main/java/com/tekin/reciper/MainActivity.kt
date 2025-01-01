package com.tekin.reciper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tekin.reciper.databinding.ActivityMainBinding
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.ui.Home
import com.tekin.reciper.ui.List.ListNotSignedIn
import com.tekin.reciper.ui.List.ListSignedIn
import com.tekin.reciper.ui.Search
import com.tekin.reciper.ui.User.UserNotSignedIn
import com.tekin.reciper.ui.User.UserSignedIn

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(Home())

        val bottomnavi = binding.bottomnavigation
        bottomnavi.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_homebutton -> replaceFragment(Home())
                R.id.nav_searchbutton -> replaceFragment(Search())
                R.id.nav_listbutton -> {
                    if (viewModel.signedIn()) {
                        replaceFragment(ListSignedIn())
                    } else {
                        replaceFragment(ListNotSignedIn())
                    }
                }
                R.id.nav_settingsbutton -> {
                    if (viewModel.signedIn()) {
                        replaceFragment(UserSignedIn())
                    } else {
                        replaceFragment(UserNotSignedIn())
                    }
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
            if (currentFragment is Home) {
                binding.bottomnavigation.selectedItemId = R.id.nav_homebutton
            } else if (currentFragment is Search) {
                binding.bottomnavigation.selectedItemId = R.id.nav_searchbutton
            } else if (currentFragment is ListSignedIn || currentFragment is ListNotSignedIn) {
                binding.bottomnavigation.selectedItemId = R.id.nav_listbutton
            } else if (currentFragment is UserSignedIn || currentFragment is UserNotSignedIn) {
                binding.bottomnavigation.selectedItemId = R.id.nav_settingsbutton
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
