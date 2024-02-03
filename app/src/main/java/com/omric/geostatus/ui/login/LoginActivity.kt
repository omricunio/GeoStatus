package com.omric.geostatus.ui.login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.omric.geostatus.R
import com.omric.geostatus.databinding.FragmentActivityBinding
import com.omric.geostatus.databinding.LoginLayoutBinding
import com.omric.geostatus.ui.root.RootActivity
import com.omric.geostatus.utils.ImageUtils
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private var _binding: LoginLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var isRegisterMode = false
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?, ) {
        super.onCreate(savedInstanceState)
        _binding = LoginLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        if(auth.uid != null) {
            passLogin()
        }

        binding.signUpText.setOnClickListener {
            toggleLoginRegisterMode()
        }

        binding.loginButton.setOnClickListener {
            if (!validateEmail()) {
                return@setOnClickListener
            }
            val email = binding.editTextEmailLogin.text.toString()
            val password = binding.editTextPasswordLogin.text.toString()
            binding.loginProgressBar.isVisible = true
            if (isRegisterMode) {
                register(email, password)
            } else {
                login(email, password)
            }
            binding.loginProgressBar.isVisible = false
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.editTextEmailLogin.text.toString()
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        binding.editTextEmailLogin.error = "Email is not valid"
        return false;
    }

    private fun toggleLoginRegisterMode() = if (isRegisterMode) {
        isRegisterMode = false
        binding.editTextName.isVisible = false
        binding.signUpText.setText(R.string.register_action)
        binding.loginButton.setText(R.string.login_button_text)
    } else {
        isRegisterMode = true
        binding.editTextName.isVisible = true
        binding.signUpText.setText(R.string.login_action)
        binding.loginButton.setText(R.string.register_button_text)
    }

    private fun passLogin() {
        val intent = Intent(this, RootActivity::class.java)
        startActivity(intent)
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.loginProgressBar.isVisible = false
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
                    val name = binding.editTextName.text.toString()
                    val user = task.result.user!!
                    task.result.user!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())

                    val usersCollection = Firebase.firestore.collection("users")
                    usersCollection.add(hashMapOf("uid" to user.uid, "name" to name))
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