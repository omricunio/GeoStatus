package com.omric.geostatus.ui.login

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.omric.geostatus.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var emailTextView: EditText
    private lateinit var passwordTextView: EditText
    private lateinit var progressBarView: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.login_activity)
        viewModel = ViewModelProvider(this).get<LoginViewModel>(LoginViewModel::class.java)

        val button = findViewById<Button>(R.id.loginButton)
        emailTextView = findViewById(R.id.editTextEmailLogin)
        passwordTextView = findViewById(R.id.editTextPasswordLogin)
        progressBarView = findViewById(R.id.loginProgressBar)


        button.setOnClickListener {
            if(!validateEmail()) {
                return@setOnClickListener
            }
            progressBarView.isVisible = true
            auth.signInWithEmailAndPassword(
                emailTextView.text.toString(),
                passwordTextView.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    progressBarView.isVisible = false
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d(TAG, "signInWithEmail:success")
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
    }

    fun validateEmail(): Boolean {
        val email = emailTextView.text.toString()
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        emailTextView.error = "Email is not valid"
        return false;
    }
}