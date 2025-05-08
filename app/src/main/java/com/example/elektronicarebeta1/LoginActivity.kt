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

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false

    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText

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
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        try {
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setupViews()
    }

    private fun setupViews() {
        emailEdit = findViewById(R.id.edit_email)
        passwordEdit = findViewById(R.id.edit_password)
        emailError = findViewById(R.id.email_error)
        passwordError = findViewById(R.id.password_error)

        val signInButton = findViewById<Button>(R.id.btn_sign_in)
        val togglePasswordVisibility = findViewById<ImageView>(R.id.toggle_password_visibility)
        val backButton = findViewById<View>(R.id.back_button)
        val signUpLink = findViewById<TextView>(R.id.link_sign_up)
        val googleSignInButton = findViewById<View>(R.id.btn_google_signin)

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

        signInButton.setOnClickListener {
            clearAllErrors()
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (validateInputs(email, password)) {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                loginUser(email, password)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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

    public fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(emailError, "Please enter a valid email address")
            isValid = false
        }

        if (password.isEmpty()) {
            showError(passwordError, "Please enter your password")
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
        clearError(emailError)
        clearError(passwordError)
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                showError(emailError, "Invalid email or password")
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
                            Toast.makeText(this@LoginActivity, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@LoginActivity, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
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
