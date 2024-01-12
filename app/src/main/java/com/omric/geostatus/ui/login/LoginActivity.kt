package com.omric.geostatus.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.omric.geostatus.R
import com.omric.geostatus.ui.root.RootActivity

class LoginActivity : AppCompatActivity() {

    private var isRegisterMode = false
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var emailTextView: EditText
    private lateinit var passwordTextView: EditText
    private lateinit var progressBarView: ProgressBar
    private lateinit var signUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.login_activity)
        viewModel = ViewModelProvider(this).get<LoginViewModel>(LoginViewModel::class.java)

        loginButton = findViewById<Button>(R.id.loginButton)
        emailTextView = findViewById(R.id.editTextEmailLogin)
        passwordTextView = findViewById(R.id.editTextPasswordLogin)
        progressBarView = findViewById(R.id.loginProgressBar)
        signUpTextView = findViewById(R.id.signUpText)

        signUpTextView.setOnClickListener {
            toggleLoginRegisterMode()
        }

        loginButton.setOnClickListener {
            if (!validateEmail()) {
                return@setOnClickListener
            }
            val email = emailTextView.text.toString()
            val password = passwordTextView.text.toString()
            progressBarView.isVisible = true
            if (isRegisterMode) {
                register(email, password)
            } else {
                login(email, password)
            }
            progressBarView.isVisible = false
        }
    }

    private fun validateEmail(): Boolean {
        val email = emailTextView.text.toString()
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        emailTextView.error = "Email is not valid"
        return false;
    }

    private fun toggleLoginRegisterMode() = if (isRegisterMode) {
        isRegisterMode = false
        signUpTextView.setText(R.string.register_action)
        loginButton.setText(R.string.login_button_text)
    } else {
        isRegisterMode = true
        signUpTextView.setText(R.string.login_action)
        loginButton.setText(R.string.register_button_text)
    }

    private fun passLogin() {
        val intent = Intent(this, RootActivity::class.java)
        startActivity(intent)
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBarView.isVisible = false
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    passLogin()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    passLogin()
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Registration failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}