class ServicesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServicesBinding
    private lateinit var auth: FirebaseAuth
    
    private val viewModel: ServiceViewModel by viewModels {
        ServiceViewModelFactory(FirebaseServiceRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupViews()
        observeData()
    }

    private fun setupViews() {
        with(binding) {
            // Setup back button
            headerLayout.backButton.setOnClickListener {
                finish()
            }

            // Setup category clicks
            phoneCategory.setOnClickListener { filterByCategory("Phones") }
            laptopCategory.setOnClickListener { filterByCategory("Laptops") }
            tvCategory.setOnClickListener { filterByCategory("TVs") }
            printerCategory.setOnClickListener { filterByCategory("Printers") }

            // Setup notification click
            notificationIcon.setOnClickListener {
                startActivity(Intent(this@ServicesActivity, NotificationActivity::class.java))
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.serviceCenters.collect { centers ->
                    updateServiceCenters(centers)
                }
            }
        }
    }

    private fun updateServiceCenters(centers: List<ServiceCenter>) {
        // Clear existing views
        binding.technicianContainer.removeAllViews()

        centers.forEach { center ->
            val centerView = layoutInflater.inflate(
                R.layout.item_service_center,
                binding.technicianContainer,
                false
            )

            // Bind center data to view
            with(centerView) {
                findViewById<TextView>(R.id.centerName).text = center.name
                findViewById<TextView>(R.id.centerRating).text = 
                    getString(R.string.rating_format, center.rating)
                findViewById<TextView>(R.id.reviewCount).text = 
                    getString(R.string.review_count_format, center.reviewCount)
                findViewById<TextView>(R.id.centerAddress).text = center.address
                findViewById<TextView>(R.id.workingHours).text = 
                    "${center.workingHours.openTime} - ${center.workingHours.closeTime}"

                // Load center image
                findViewById<ImageView>(R.id.centerImage)?.let { imageView ->
                    if (center.images.isNotEmpty()) {
                        Glide.with(this@ServicesActivity)
                            .load(center.images.first())
                            .placeholder(R.drawable.placeholder_service_center)
                            .into(imageView)
                    }
                }

                // Setup service tags
                val tagsContainer = findViewById<FlexboxLayout>(R.id.serviceTagsContainer)
                center.services.forEach { service ->
                    val tagView = layoutInflater.inflate(
                        R.layout.item_service_tag,
                        tagsContainer,
                        false
                    )
                    tagView.findViewById<TextView>(R.id.tagText).text = service.name
                    tagsContainer.addView(tagView)
                }

                // Setup click listeners
                findViewById<View>(R.id.viewDetailsButton).setOnClickListener {
                    val intent = Intent(this@ServicesActivity, ServiceCenterDetailActivity::class.java)
                    intent.putExtra("center_id", center.id)
                    startActivity(intent)
                }

                findViewById<View>(R.id.bookNowButton).setOnClickListener {
                    val intent = Intent(this@ServicesActivity, BookingActivity::class.java)
                    intent.putExtra("center_id", center.id)
                    startActivity(intent)
                }
            }

            binding.technicianContainer.addView(centerView)
        }
    }

    private fun filterByCategory(category: String) {
        // Update UI to show selected category
        with(binding) {
            phoneCategory.isSelected = category == "Phones"
            laptopCategory.isSelected = category == "Laptops"
            tvCategory.isSelected = category == "TVs"
            printerCategory.isSelected = category == "Printers"
        }

        // Filter service centers by category
        lifecycleScope.launch {
            viewModel.filterServiceCentersByCategory(category)
        }
    }
}