package com.example.lockin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Signup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val username = findViewById<EditText>(R.id.username)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val loginTxt = findViewById<TextView>(R.id.loginTxt)

        loginTxt.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupBtn.setOnClickListener {
            val userStr = username.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val passStr = password.text.toString().trim()
            val confirmPassStr = confirmPassword.text.toString().trim()

            if (userStr.isEmpty() || emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passStr != confirmPassStr) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val helper = ApiHelper(this)
            helper.signup(userStr, emailStr, passStr)
        }
    }
}
