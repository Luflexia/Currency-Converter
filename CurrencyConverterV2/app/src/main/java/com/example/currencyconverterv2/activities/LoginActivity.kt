package com.example.currencyconverterv2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.currencyconverterv2.databinding.ActivityLoginBinding
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var usersJson: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUsers()

        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateUser(username, password)) {
                // Сохраняем информацию о текущем пользователе
                val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("CURRENT_USER", username).apply()

                // Переходим на главный экран
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (registerUser(username, password)) {
                // Сохраняем информацию о новом пользователе
                val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("CURRENT_USER", username).apply()

                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()

                // Переходим на главный экран
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Не удалось зарегистрировать пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUsers() {
        try {
            val file = File(filesDir, "users.json")
            if (!file.exists()) {
                usersJson = JSONObject().apply {
                    put("users", JSONObject())
                }
                saveUsers()
            } else {
                val inputStream = FileInputStream(file)
                val json = inputStream.bufferedReader().use { it.readText() }
                usersJson = JSONObject(json)
            }
            Log.d("LoginActivity", "Users loaded: $usersJson")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error loading users: ${e.message}", e)
            Toast.makeText(this, "Ошибка загрузки пользователей: ${e.message}", Toast.LENGTH_LONG).show()
            usersJson = JSONObject().apply {
                put("users", JSONObject())
            }
        }
    }

    private fun saveUsers() {
        try {
            val file = File(filesDir, "users.json")
            FileOutputStream(file).use { it.write(usersJson.toString().toByteArray()) }
            Log.d("LoginActivity", "Users saved: $usersJson")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error saving users: ${e.message}", e)
            Toast.makeText(this, "Ошибка сохранения пользователей: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

//    private fun validateUser(username: String, password: String): Boolean {
//        val users = usersJson.optJSONObject("users")
//        val userObj = users?.optJSONObject(username)
//        return userObj?.optString("password") == password
//    }

    private external fun validateUserNative(usersJson: String, username: String, password: String): Boolean

    companion object {
        init {
            System.loadLibrary("user_validation")
        }
    }

    private fun validateUser(username: String, password: String): Boolean {
        return validateUserNative(usersJson.toString(), username, password)
    }

private fun registerUser(username: String, password: String): Boolean {
    val users = usersJson.optJSONObject("users") ?: JSONObject()
    if (users.has(username)) {
        return false // Пользователь уже существует
    }
    users.put(username, JSONObject().apply {
        put("password", password)
    })
    usersJson.put("users", users)
    saveUsers()
    return true
}

    private fun saveLoggedInUser(username: String) {
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("loggedInUser", username)
            apply()
        }
        Log.d("LoginActivity", "Logged in user saved: $username")
    }


}