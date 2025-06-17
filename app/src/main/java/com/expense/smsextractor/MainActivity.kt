package com.expense.smsextractor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.expense.smsextractor.data.AppDatabase
import com.expense.smsextractor.databinding.ActivityMainBinding
import android.view.View
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel setup
        val database = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = ExpenseViewModelFactory(database)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExpenseViewModel::class.java)

        // ViewPager setup
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Drafts"
                1 -> "Added"
                else -> null
            }
        }.attach()

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.loadingText.visibility = View.VISIBLE
                binding.tabLayout.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.tabLayout.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
            }
        }

        checkSmsPermission()
        handleInitialLoad()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadExpenses(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleInitialLoad() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            viewModel.loadExpenses(this)
            prefs.edit().putBoolean("is_first_launch", false).apply()
        } else {
            // Check if db is empty and prompt to refresh
            viewModel.draftCount.observe(this) { draftCount ->
                viewModel.addedCount.observe(this) { addedCount ->
                    if (draftCount == 0 && addedCount == 0) {
                        Snackbar.make(binding.root, "Use the refresh button to scan for expenses.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_CODE)
        } else {
            viewModel.loadExpenses(applicationContext)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.loadExpenses(applicationContext)
            }
        }
    }

    companion object {
        private const val SMS_PERMISSION_CODE = 101
    }
}