package com.example.elektronicarebeta1

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.elektronicarebeta1.data.repositories.RepairRequestRepository
import com.example.elektronicarebeta1.databinding.ActivityBookingBinding
import com.example.elektronicarebeta1.viewmodels.RepairRequestViewModel
import com.example.elektronicarebeta1.viewmodels.RepairRequestViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding

    private val viewModel: RepairRequestViewModel by viewModels {
        RepairRequestViewModelFactory(RepairRequestRepository())
    }

    private val imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { viewModel.addImage(it) }
            }

    private val cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    tempImageUri?.let { viewModel.addImage(it) }
                }
            }

    private var tempImageUri: Uri? = null
    private var selectedDate: Long = 0
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeState()
    }

    private fun setupViews() {
        with(binding) {
            backButton.setOnClickListener { finish() }

            takePhotoButton.setOnClickListener { launchCamera() }

            uploadButton.setOnClickListener { imagePickerLauncher.launch("image/*") }

            dateContainer.setOnClickListener { showDatePicker() }

            timeContainer.setOnClickListener { showTimePicker() }

            submitButton.setOnClickListener { submitRequest() }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedImages.collect { images -> updateImagePreviews(images) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.repairRequestsState.collect { state ->
                    when (state) {
                        is RepairRequestsState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.submitButton.isEnabled = false
                        }
                        is RepairRequestsState.RequestCreated -> {
                            binding.progressBar.visibility = View.GONE
                            showSuccessDialog()
                        }
                        is RepairRequestsState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.submitButton.isEnabled = true
                            Toast.makeText(this@BookingActivity, state.message, Toast.LENGTH_LONG)
                                    .show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                            binding.submitButton.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        val imageFile = File(externalCacheDir, "temp_image.jpg")
        tempImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
        cameraLauncher.launch(tempImageUri)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
                        this,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            selectedDate = calendar.timeInMillis
                            updateDateDisplay(calendar)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
                .show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            selectedTime = String.format("%02d:%02d", hour, minute)
                            updateTimeDisplay()
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                )
                .show()
    }

    private fun updateDateDisplay(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.dateText.text = dateFormat.format(calendar.time)
    }

    private fun updateTimeDisplay() {
        binding.timeText.text = selectedTime
    }

    private fun updateImagePreviews(images: List<Uri>) {
        binding.imagePreviewContainer.removeAllViews()

        images.forEach { uri ->
            val imageView =
                    layoutInflater.inflate(
                            R.layout.item_image_preview,
                            binding.imagePreviewContainer,
                            false
                    )

            Glide.with(this).load(uri).into(imageView.findViewById(R.id.previewImage))

            imageView.findViewById<View>(R.id.removeButton).setOnClickListener {
                viewModel.removeImage(uri)
            }

            binding.imagePreviewContainer.addView(imageView)
        }
    }

    private fun submitRequest() {
        val issue = binding.issueDescription.text.toString()
        val centerId = intent.getStringExtra("center_id") ?: return
        val deviceType = intent.getStringExtra("device_type") ?: return
        val deviceModel = intent.getStringExtra("device_model") ?: return

        if (issue.isBlank()) {
            Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate == 0L) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createRepairRequest(
                deviceType = deviceType,
                deviceModel = deviceModel,
                issue = issue,
                scheduledDate = selectedDate,
                scheduledTime = selectedTime,
                serviceCenterId = centerId
        )
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this, R.style.Theme_Dialog)
        dialog.setContentView(R.layout.popup_request_success)

        dialog.findViewById<View>(R.id.btn_whatsapp).setOnClickListener {
            startActivity(createWhatsAppIntent())
            dialog.dismiss()
            finish()
        }

        dialog.findViewById<View>(R.id.btn_homepage).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        dialog.show()
    }

    private fun createWhatsAppIntent(): Intent {
        val message = "Hi, I would like to inquire about my service request."
        return Intent(Intent.ACTION_VIEW).apply {
            data =
                    Uri.parse(
                            "https://wa.me/${getString(R.string.service_whatsapp)}?text=${Uri.encode(message)}"
                    )
        }
    }
}
