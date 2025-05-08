package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.elektronicarebeta1.databinding.ActivityDashboardBinding
import com.example.elektronicarebeta1.models.RepairRequest
import com.example.elektronicarebeta1.repositories.FirebaseRepairRepository
import com.example.elektronicarebeta1.repositories.FirebaseUserRepository
import com.example.elektronicarebeta1.viewmodels.DashboardUiState
import com.example.elektronicarebeta1.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            FirebaseUserRepository(),
            FirebaseRepairRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupObservers()
        setupClickListeners()
        checkAuthState()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DashboardUiState.Loading -> {
                            // Show loading state
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is DashboardUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.welcomeText.text = "Welcome back, ${state.user.fullName.split(" ").first()}!"
                            updateRecentRepairs(state.recentRepairs)
                        }
                        is DashboardUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@DashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Category clicks
            phoneCategory.setOnClickListener { navigateToServices("Phones") }
            laptopCategory.setOnClickListener { navigateToServices("Laptops") }
            tvCategory.setOnClickListener { navigateToServices("TVs") }
            printerCategory.setOnClickListener { navigateToServices("Printers") }

            // Navigation clicks
            navHome.setOnClickListener { /* Already on home */ }
            navHistory.setOnClickListener { startActivity(Intent(this@DashboardActivity, RepairHistoryActivity::class.java)) }
            navServices.setOnClickListener { startActivity(Intent(this@DashboardActivity, ServicesActivity::class.java)) }
            navProfile.setOnClickListener { startActivity(Intent(this@DashboardActivity, ProfileActivity::class.java)) }

            // Notification
            notificationIcon.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, NotificationActivity::class.java))
            }

            // View all repairs
            viewAllRecent.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, RepairHistoryActivity::class.java))
            }
        }
    }

    private fun updateRecentRepairs(repairs: List<RepairRequest>) {
        // Update repair cards based on the repairs list
        if (repairs.isEmpty()) {
            binding.noRepairsText.visibility = View.VISIBLE
            binding.repairCardsContainer.visibility = View.GONE
        } else {
            binding.noRepairsText.visibility = View.GONE
            binding.repairCardsContainer.visibility = View.VISIBLE
            
            // Update repair cards
            repairs.take(2).forEachIndexed { index, repair ->
                val card = when (index) {
                    0 -> binding.repairCard1
                    1 -> binding.repairCard2
                    else -> null
                }
                
                card?.let {
                    updateRepairCard(it, repair)
                }
            }
        }
    }

    private fun updateRepairCard(cardView: View, repair: RepairRequest) {
        val deviceName = cardView.findViewById<TextView>(R.id.deviceName)
        val repairType = cardView.findViewById<TextView>(R.id.repairType)
        val repairDate = cardView.findViewById<TextView>(R.id.repairDate)
        val repairLocation = cardView.findViewById<TextView>(R.id.repairLocation)
        val repairStatus = cardView.findViewById<TextView>(R.id.repairStatus)
        val repairCost = cardView.findViewById<TextView>(R.id.repairCost)
        
        deviceName.text = repair.deviceModel
        repairType.text = repair.issue
        repairDate.text = formatDate(repair.scheduledDate)
        repairLocation.text = repair.serviceCenterId // You might want to fetch service center name
        repairStatus.text = repair.status.name
        repairCost.text = formatCurrency(repair.estimatedCost)
        
        cardView.setOnClickListener {
            val intent = Intent(this, RepairDetailActivity::class.java).apply {
                putExtra("repair_id", repair.id)
            }
            startActivity(intent)
        }
    }

    private fun navigateToServices(category: String) {
        val intent = Intent(this, ServicesActivity::class.java).apply {
            putExtra("category", category)
        }
        startActivity(intent)
    }

    private fun checkAuthState() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun formatDate(timestamp: Long): String {
        // Implement date formatting
        return android.text.format.DateFormat.format("MMMM dd, yyyy", timestamp).toString()
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp${String.format("%,.0f", amount)}"
    }
}