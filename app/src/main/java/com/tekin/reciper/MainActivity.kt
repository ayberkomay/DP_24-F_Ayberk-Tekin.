package com.tekin.reciper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val READ_MEDIA_IMAGES_PERMISSION_CODE = 101
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

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

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_MEDIA_IMAGES_PERMISSION_CODE
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Gallery access permission granted",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "You cannot upload images without gallery access permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            READ_MEDIA_IMAGES_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Gallery access permission granted",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "You cannot upload images without gallery access permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
