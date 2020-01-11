package com.harounach.roote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.harounach.roote.databinding.ActivityMainBinding
import com.harounach.roote.login.LoginFragmentDirections
import com.harounach.roote.login.LoginViewModel
import com.harounach.roote.rider.RiderMapsFragmentDirections
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    // Login viewModel
    private lateinit var loginViewModel: LoginViewModel

    // NavController
    private lateinit var navController: NavController

    // AppBarConfiguration
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // instantiate the ViewModel
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        // Get the NavController
        navController = findNavController(R.id.myNavHostFragment)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        // Set up Toolbar with NavController
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)


        // Setup ActionBar
        val toolbar = binding.toolBar
        setSupportActionBar(toolbar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

}
