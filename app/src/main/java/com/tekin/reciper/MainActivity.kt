package com.tekin.reciper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.tekin.reciper.databinding.ActivityMainBinding

// Update pngs according to dpi

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        replaceFragment(Home())

        binding.bottomnavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_homebutton -> replaceFragment(Home())
                R.id.nav_searchbutton -> replaceFragment(Search())
                R.id.nav_listbutton -> replaceFragment(List())
                R.id.nav_settingsbutton -> replaceFragment(Settings())
                else -> {
                }
            }
            true
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun reload(){
    }
}
