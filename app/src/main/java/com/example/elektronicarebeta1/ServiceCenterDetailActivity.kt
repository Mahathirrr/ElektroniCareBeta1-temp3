```kotlin
class ServiceCenterDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTechnicianBinding
    private lateinit var auth: FirebaseAuth
    
    private val viewModel: ServiceViewModel by viewModels {
        ServiceViewModelFactory(FirebaseServiceRepository())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTechnicianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        
        val centerId = intent.getStringExtra("center_id") ?: run {
            finish()
            return
        }
        
        setupViews()
        loadServiceCenter(centerId)
    }
    
    private fun setupViews() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.bookNowButton.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("center_id", intent.getStringExtra("center_id"))
            }
            startActivity(intent)
        }
    }
    
    private fun loadServiceCenter(centerId: String) {
        lifecycleScope.launch {
            val center = viewModel.getServiceCenterById(centerId)
            center?.let { updateUI(it) }
        }
    }
    
    private fun updateUI(center: ServiceCenter) {
        with(binding) {
            // Load center image
            if (center.images.isNotEmpty()) {
                Glide.with(this@ServiceCenterDetailActivity)
                    .load(center.images.first())
                    .into(centerImage)
            }
            
            // Basic info
            centerName.text = center.name
            centerRating.text = center.rating.toString()
            reviewCount.text = "(${center.reviewCount})"
            centerDescription.text = center.description
            
            // Location and hours
            locationText.text = center.address
            hoursText.text = "${center.workingHours.openTime} - ${center.workingHours.closeTime}"
            
            // Services
            servicesContainer.removeAllViews()
            center.services.forEach { service ->
                val serviceView = layoutInflater.inflate(
                    R.layout.item_service_detail,
                    servicesContainer,
                    false
                )
                
                serviceView.findViewById<TextView>(R.id.serviceName).text = service.name
                serviceView.findViewById<TextView>(R.id.servicePrice).text = 
                    getString(R.string.price_format, service.basePrice)
                
                servicesContainer.addView(serviceView)
            }
        }
    }
}
```