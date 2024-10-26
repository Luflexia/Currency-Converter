package com.example.currencyconverterv2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.databinding.ActivityLoginBinding
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var usersJson: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загрузка пользователей из JSON
        loadUsers()

        // Настройка кнопки входа
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateUser(username, password)) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Неверные учетные данные", Toast.LENGTH_SHORT).show()
            }
        }

        // Настройка кнопки регистрации
        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            if (registerUser(username, password)) {
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUsers() {
        val file = File(filesDir, "users.json")
        if (!file.exists()) {
            // Если файл не существует, создаем пустой JSON
            usersJson = JSONObject("{'users':[]}")
            saveUsers()
        } else {
            // Чтение файла
            val inputStream = FileInputStream(file)
            val json = inputStream.bufferedReader().use { it.readText() }
            usersJson = JSONObject(json)
        }
    }

    private fun saveUsers() {
        val file = File(filesDir, "users.json")
        FileOutputStream(file).use { it.write(usersJson.toString().toByteArray()) }
    }

    private fun validateUser(username: String, password: String): Boolean {
        val usersArray = usersJson.getJSONArray("users")
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            if (user.getString("username") == username && user.getString("password") == password) {
                return true
            }
        }
        return false
    }

    private fun registerUser(username: String, password: String): Boolean {
        // Проверяем, что такого пользователя еще нет
        val usersArray = usersJson.getJSONArray("users")
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            if (user.getString("username") == username) {
                return false // Пользователь уже существует
            }
        }

        // Добавляем нового пользователя
        val newUser = JSONObject()
        newUser.put("username", username)
        newUser.put("password", password)
        usersArray.put(newUser)
        saveUsers()
        return true
    }
}
