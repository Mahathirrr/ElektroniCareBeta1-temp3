package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false

    private lateinit var fullNameEdit: EditText
    private lateinit var mobileEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText

    private lateinit var fullNameError: TextView
    private lateinit var mobileError: TextView
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView

    private val indonesianPhonePattern = Pattern.compile("^(\\+62|62|0)8[1-9][0-9]{6,9}$")

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let {
                    firebaseAuthWithGoogle(it)
                } ?: run {
                    Toast.makeText(this, "Google Sign-In failed: ID token is null", Toast.LENGTH_LONG).show()
                    showError(emailError, "Google sign in failed: ID token is null")
                }
            } catch (e: ApiException) {
                val errorMessage = when(e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in was cancelled"
                    GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign in failed"
                    GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Sign in already in progress"
                    GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Invalid account selected"
                    GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Sign in required"
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error - check your connection"
                    else -> "Google sign in failed: ${e.statusCode}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                showError(emailError, errorMessage)
            }
        } else {
            Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
            showError(emailError, "Google Sign-In was canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setupViews()
    }

    private fun setupViews() {
        fullNameEdit = findViewById(R.id.edit_full_name)
        mobileEdit = findViewById(R.id.edit_mobile)
        emailEdit = findViewById(R.id.edit_email)
        passwordEdit = findViewById(R.id.edit_password)

        fullNameError = findViewById(R.id.fullname_error)
        mobileError = findViewById(R.id.mobile_error)
        emailError = findViewById(R.id.email_error)
        passwordError = findViewById(R.id.password_error)

        val createAccountButton = findViewById<Button>(R.id.btn_create_account)
        val togglePasswordVisibility = findViewById<ImageView>(R.id.toggle_password_visibility)
        val backButton = findViewById<View>(R.id.back_button)
        val signInLink = findViewById<TextView>(R.id.link_sign_in)
        val googleSignInButton = findViewById<View>(R.id.btn_google_signin)

        fullNameEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearError(fullNameError)
        }

        mobileEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearError(mobileError)
            if (!hasFocus) {
                val number = mobileEdit.text.toString()
                if (number.isNotEmpty()) {
                    mobileEdit.setText(formatIndonesianPhoneNumber(number))
                }
            }
        }

        emailEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearError(emailError)
        }

        passwordEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearError(passwordError)
        }

        togglePasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEdit.transformationMethod = null
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordEdit.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordEdit.setSelection(passwordEdit.text.length)
        }

        createAccountButton.setOnClickListener {
            clearAllErrors()
            val fullName = fullNameEdit.text.toString().trim()
            val mobile = mobileEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (validateInputs(fullName, mobile, email, password)) {
                Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()
                registerUser(fullName, mobile, email, password)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        signInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        googleSignInButton.setOnClickListener {
            try {
                Toast.makeText(this, "Starting Google Sign-In...", Toast.LENGTH_SHORT).show()
                signInWithGoogle()
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In Button Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    public fun validateInputs(fullName: String, mobile: String, email: String, password: String): Boolean {
        var isValid = true

        if (fullName.isEmpty()) {
            showError(fullNameError, "Please enter your full name")
            isValid = false
        }

        if (!isValidIndonesianPhoneNumber(mobile)) {
            showError(mobileError, "Please enter a valid phone number")
            isValid = false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(emailError, "Please enter a valid email address")
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            showError(passwordError, "Password must be at least 6 characters")
            isValid = false
        }

        return isValid
    }

    public fun showError(errorView: TextView, message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun clearError(errorView: TextView) {
        errorView.text = ""
        errorView.visibility = View.GONE
    }

    private fun clearAllErrors() {
        clearError(fullNameError)
        clearError(mobileError)
        clearError(emailError)
        clearError(passwordError)
    }

    private fun isValidIndonesianPhoneNumber(phone: String): Boolean {
        return indonesianPhonePattern.matcher(phone).matches()
    }

    public fun formatIndonesianPhoneNumber(number: String): String {
        var formatted = number.replace("[^0-9]".toRegex(), "")
        if (formatted.startsWith("62")) {
            formatted = "0${formatted.substring(2)}"
        } else if (formatted.startsWith("+62")) {
            formatted = "0${formatted.substring(3)}"
        } else if (!formatted.startsWith("0")) {
            formatted = "0$formatted"
        }
        return formatted
    }

    private fun registerUser(fullName: String, mobile: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val userData = hashMapOf(
                    "fullName" to fullName,
                    "mobile" to formatIndonesianPhoneNumber(mobile),
                    "email" to email
                )

                user?.let {
                    db.collection("users")
                        .document(it.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@RegisterActivity, "Error saving data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                showError(emailError, "Registration failed: ${e.message}")
            }
    }

    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                user?.let {
                    val userData = hashMapOf(
                        "fullName" to it.displayName,
                        "email" to it.email,
                        "mobile" to ""
                    )

                    db.collection("users")
                        .document(it.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this@RegisterActivity, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@RegisterActivity, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Google authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToDashboard() {
        try {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error navigating to dashboard: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
