package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.elektronicarebeta1.viewmodels.DashboardViewModel
import com.example.elektronicarebeta1.viewmodels.DashboardViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(FirebaseUserRepository(), FirebaseRepairRepository())
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
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is DashboardUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.welcomeText.text =
                                    getString(
                                            R.string.welcome_back_name,
                                            state.user.fullName.split(" ").first()
                                    )
                            updateRecentRepairs(state.recentRepairs)
                        }
                        is DashboardUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                            this@DashboardActivity,
                                            state.message,
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
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
            navHome.setOnClickListener { /* Already on home */}
            navHistory.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, RepairHistoryActivity::class.java))
            }
            navServices.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, ServicesActivity::class.java))
            }
            navProfile.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, ProfileActivity::class.java))
            }

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
        if (repairs.isEmpty()) {
            binding.noRepairsText.visibility = View.VISIBLE
            binding.repairCardsContainer.visibility = View.GONE
            return
        }

        binding.noRepairsText.visibility = View.GONE
        binding.repairCardsContainer.visibility = View.VISIBLE

        repairs.take(2).forEachIndexed { index, repair ->
            val card =
                    when (index) {
                        0 -> binding.repairCard1
                        1 -> binding.repairCard2
                        else -> null
                    }

            card?.let { updateRepairCard(it, repair) }
        }
    }

    private fun updateRepairCard(cardView: View, repair: RepairRequest) {
        with(cardView) {
            findViewById<TextView>(R.id.deviceName).text = repair.deviceModel
            findViewById<TextView>(R.id.repairType).text = repair.issue
            findViewById<TextView>(R.id.repairDate).text = formatDate(repair.scheduledDate)
            findViewById<TextView>(R.id.repairLocation).text = repair.serviceCenterId
            findViewById<TextView>(R.id.repairStatus).apply {
                text = repair.status.name
                setTextColor(getStatusColor(repair.status))
                background = getStatusBackground(repair.status)
            }
            findViewById<TextView>(R.id.repairCost).text =
                    getString(R.string.price_format, repair.estimatedCost)

            setOnClickListener {
                val intent = Intent(this@DashboardActivity, RepairDetailActivity::class.java)
                intent.putExtra("repair_id", repair.id)
                startActivity(intent)
            }
        }
    }

    private fun navigateToServices(category: String) {
        val intent =
                Intent(this, ServicesActivity::class.java).apply { putExtra("category", category) }
        startActivity(intent)
    }

    private fun checkAuthState() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(timestamp)
    }

    private fun getStatusColor(status: RepairStatus): Int {
        return when (status) {
            RepairStatus.COMPLETED -> getColor(R.color.status_completed)
            RepairStatus.IN_PROGRESS -> getColor(R.color.status_in_progress)
            RepairStatus.CANCELLED -> getColor(R.color.status_cancelled)
            else -> getColor(R.color.status_pending)
        }
    }

    private fun getStatusBackground(status: RepairStatus): Drawable {
        return when (status) {
            RepairStatus.COMPLETED -> getDrawable(R.drawable.status_completed_bg)!!
            RepairStatus.IN_PROGRESS -> getDrawable(R.drawable.status_inprogress_bg)!!
            RepairStatus.CANCELLED -> getDrawable(R.drawable.status_cancelled_bg)!!
            else -> getDrawable(R.drawable.status_pending_bg)!!
        }
    }
}
