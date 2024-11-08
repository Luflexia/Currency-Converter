#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include <vector>

extern "C" {

// Простая функция для поиска подстроки
const char* strstr_custom(const char* haystack, const char* needle) {
    return strstr(haystack, needle);
}

// Простая функция для извлечения значения из JSON строки
std::string extract_value(const char* json, const char* key) {
    std::string result;
    const char* start = strstr_custom(json, key);
    if (start) {
        start = strstr_custom(start, ":");
        if (start) {
            start++; // пропускаем ':'
            while (*start == ' ' || *start == '"') start++; // пропускаем пробелы и кавычки
            const char* end = start;
            while (*end != '"' && *end != ',' && *end != '}') end++;
            result = std::string(start, end - start);
        }
    }
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_example_currencyconverterv2_activities_LoginActivity_validateUserNative(
        JNIEnv* env,
        jobject /* thisObj */,
        jstring usersJson,
        jstring username,
        jstring password) {

    const char *usersJsonStr = env->GetStringUTFChars(usersJson, 0);
    const char *usernameStr = env->GetStringUTFChars(username, 0);
    const char *passwordStr = env->GetStringUTFChars(password, 0);

    // Ищем пользователя в JSON
    std::string userKey = std::string("\"") + usernameStr + "\"";
    const char* userStart = strstr_custom(usersJsonStr, userKey.c_str());

    jboolean result = JNI_FALSE;

    if (userStart) {
        // Извлекаем пароль пользователя
        std::string storedPassword = extract_value(userStart, "password");

        // Сравниваем пароли
        if (storedPassword == passwordStr) {
            result = JNI_TRUE;
        }
    }

    env->ReleaseStringUTFChars(usersJson, usersJsonStr);
    env->ReleaseStringUTFChars(username, usernameStr);
    env->ReleaseStringUTFChars(password, passwordStr);

    return result;
}

}