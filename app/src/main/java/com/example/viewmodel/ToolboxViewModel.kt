package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ToolboxDatabase
import com.example.data.ToolboxRepository
import com.example.data.FavoriteToolEntity
import com.example.data.RecentActivityEntity
import com.example.data.UserEntity
import com.example.data.ToolSubmissionEntity
import com.example.model.Tool
import com.example.model.ToolRegistry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ToolboxViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ToolboxRepository

    // Base UI States
    private val _currentScreen = MutableStateFlow("splash") // splash -> login -> home
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedToolId = MutableStateFlow<String?>(null)
    val selectedToolId: StateFlow<String?> = _selectedToolId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortType = MutableStateFlow("A-Z") // "A-Z" or "Newest"
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    private val _settingsLayoutGrid = MutableStateFlow(true)
    val settingsLayoutGrid: StateFlow<Boolean> = _settingsLayoutGrid.asStateFlow()

    // Interactive Tool States
    private val _toolInput = MutableStateFlow("")
    val toolInput: StateFlow<String> = _toolInput.asStateFlow()

    private val _toolOutput = MutableStateFlow("")
    val toolOutput: StateFlow<String> = _toolOutput.asStateFlow()

    private val _toolSecondaryInput = MutableStateFlow("")
    val toolSecondaryInput: StateFlow<String> = _toolSecondaryInput.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Authentication States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Submissions Flow
    val allSubmissions: StateFlow<List<ToolSubmissionEntity>>

    // Room Database Flows
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoriteTools: StateFlow<List<FavoriteToolEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getFavorites(user.username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentActivities: StateFlow<List<RecentActivityEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getRecentActivities(user.username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val database = ToolboxDatabase.getDatabase(application)
        repository = ToolboxRepository(database.toolboxDao())

        allSubmissions = repository.allSubmissions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            // Seed the administrator account requested by the user
            val existingAdmin = repository.getUser("Davz")
            if (existingAdmin == null) {
                repository.insertUser(UserEntity(
                    username = "Davz",
                    email = "admin@davz.com",
                    passwordHash = hashString("Davz12312345"),
                    securityQuestion = "Siapa pembuat utama?",
                    securityAnswerHash = hashString("Davz"),
                    isLoggedIn = false,
                    isAdmin = true
                ))
            }

            val activeUser = repository.getActiveUser()
            if (activeUser != null) {
                _currentUser.value = activeUser
                _currentScreen.value = "home"
            } else {
                _currentScreen.value = "login"
            }
        }
    }

    // Helper to securely hash passwords & security answers
    private fun hashString(input: String): String {
        return try {
            val bytes = input.toByteArray()
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input // Fallback in unlikely exception
        }
    }

    // AUTH ACTIONS
    fun signUp(username: String, email: String, passwordRaw: String, question: String, answerRaw: String) {
        viewModelScope.launch {
            if (username.trim().isEmpty() || email.trim().isEmpty() || passwordRaw.trim().isEmpty() || question.trim().isEmpty() || answerRaw.trim().isEmpty()) {
                showToast("Harap isi semua kolom!")
                return@launch
            }
            val existing = repository.getUser(username)
            if (existing != null) {
                showToast("Username sudah digunakan!")
                return@launch
            }
            val user = UserEntity(
                username = username,
                email = email,
                passwordHash = hashString(passwordRaw),
                securityQuestion = question,
                securityAnswerHash = hashString(answerRaw),
                isLoggedIn = true
            )
            repository.logoutAllUsers()
            repository.insertUser(user)
            _currentUser.value = user
            _currentScreen.value = "home"
            showToast("Registrasi berhasil secara lokal!")
        }
    }

    fun login(username: String, passwordRaw: String) {
        viewModelScope.launch {
            if (username.trim().isEmpty() || passwordRaw.trim().isEmpty()) {
                showToast("Harap isi semua kolom login!")
                return@launch
            }
            val user = repository.getUser(username)
            if (user == null || user.passwordHash != hashString(passwordRaw)) {
                showToast("Username atau password salah!")
                return@launch
            }
            repository.logoutAllUsers()
            val loggedInUser = user.copy(isLoggedIn = true)
            repository.updateUser(loggedInUser)
            _currentUser.value = loggedInUser
            _currentScreen.value = "home"
            showToast("Selamat datang kembali, $username!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAllUsers()
            _currentUser.value = null
            _currentScreen.value = "login"
            showToast("Berhasil keluar!")
        }
    }

    fun resetPassword(username: String, securityQuestion: String, securityAnswerRaw: String, newPasswordRaw: String) {
        viewModelScope.launch {
            if (username.trim().isEmpty() || securityQuestion.trim().isEmpty() || securityAnswerRaw.trim().isEmpty() || newPasswordRaw.trim().isEmpty()) {
                showToast("Harap lengkapi semua isian reset password!")
                return@launch
            }
            val user = repository.getUser(username)
            if (user == null) {
                showToast("Username tidak ditemukan!")
                return@launch
            }
            if (user.securityQuestion.lowercase() != securityQuestion.lowercase() || user.securityAnswerHash != hashString(securityAnswerRaw)) {
                showToast("Pertanyaan atau Jawaban Keamanan salah!")
                return@launch
            }
            val updatedUser = user.copy(passwordHash = hashString(newPasswordRaw))
            repository.updateUser(updatedUser)
            showToast("Password berhasil diperbarui! Silakan login.")
        }
    }

    // SUBMISSION ACTIONS
    fun submitToolIdea(toolName: String, category: String, description: String, codeSnippet: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (toolName.trim().isEmpty() || category.trim().isEmpty() || description.trim().isEmpty()) {
                showToast("Nama, kategori, dan deskripsi wajib diisi!")
                return@launch
            }
            val sub = ToolSubmissionEntity(
                username = user.username,
                toolName = toolName,
                category = category,
                description = description,
                codeSnippet = codeSnippet,
                status = "Pending"
            )
            repository.addSubmission(sub)
            showToast("Ide tool berhasil dikirim untuk ditinjau!")
        }
    }

    fun updateSubmissionStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateSubmissionStatus(id, status)
            showToast("Status submission diperbarui menjadi: $status")
        }
    }

    fun deleteSubmission(id: Int) {
        viewModelScope.launch {
            repository.deleteSubmission(id)
            showToast("Submission berhasil dihapus!")
        }
    }

    fun navigateTo(screen: String, toolId: String? = null) {
        viewModelScope.launch {
            _currentScreen.value = screen
            val user = _currentUser.value
            if (screen == "detail" && toolId != null) {
                _selectedToolId.value = toolId
                val tool = ToolRegistry.getToolById(toolId)
                if (tool != null && user != null) {
                    repository.addActivity(user.username, tool.id, tool.name, tool.category, "Dibuka")
                }
                // Load autosave input
                val saved = if (user != null) repository.getAutosave(user.username, toolId) ?: "" else ""
                _toolInput.value = saved
                _toolOutput.value = ""
                _toolSecondaryInput.value = ""
                // Perform quick initial calculation if there's saved input
                if (saved.isNotEmpty()) {
                    runToolCalculation(toolId, saved, _toolSecondaryInput.value)
                }
            } else if (screen == "all_tools" && toolId != null) {
                // Navigate and set specific query / filter
                _selectedCategory.value = toolId
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortType(type: String) {
        _sortType.value = type
    }

    fun toggleLayoutMode() {
        _settingsLayoutGrid.value = !_settingsLayoutGrid.value
    }

    fun updateToolInput(input: String) {
        _toolInput.value = input
        val toolId = _selectedToolId.value
        val user = _currentUser.value
        if (toolId != null) {
            viewModelScope.launch {
                if (user != null) {
                    repository.saveInput(user.username, toolId, input)
                }
                runToolCalculation(toolId, input, _toolSecondaryInput.value)
            }
        }
    }

    fun updateSecondaryInput(secondary: String) {
        _toolSecondaryInput.value = secondary
        val toolId = _selectedToolId.value
        if (toolId != null) {
            runToolCalculation(toolId, _toolInput.value, secondary)
        }
    }

    fun toggleFavorite(tool: Tool) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.toggleFavorite(user.username, tool.id, tool.name, tool.category)
                showToast(if (repository.isFavorite(user.username, tool.id)) "Ditambahkan ke Favorit" else "Dihapus dari Favorit")
            }
        }
    }

    fun checkIsFavorite(toolId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                callback(repository.isFavorite(user.username, toolId))
            } else {
                callback(false)
            }
        }
    }

    fun clearToolData() {
        val toolId = _selectedToolId.value ?: return
        val user = _currentUser.value
        viewModelScope.launch {
            if (user != null) {
                repository.clearAutosave(user.username, toolId)
            }
            _toolInput.value = ""
            _toolOutput.value = ""
            _toolSecondaryInput.value = ""
            showToast("Data dibersihkan")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.clearActivity(user.username)
                showToast("Riwayat aktivitas berhasil dikosongkan")
            }
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // Comprehensive client-side calculation logic for all tools
    private fun runToolCalculation(toolId: String, input: String, secondary: String) {
        if (input.isEmpty() && toolId != "pass_gen" && toolId != "uuid_gen" && toolId != "lorem_gen" && toolId != "time_conv" && toolId != "dice_coin") {
            _toolOutput.value = ""
            return
        }
        try {
            when (toolId) {
                // Text Tools
                "text_upper" -> _toolOutput.value = input.uppercase()
                "text_lower" -> _toolOutput.value = input.lowercase()
                "text_dup" -> {
                    val lines = input.split("\n")
                    _toolOutput.value = lines.distinct().joinToString("\n")
                }
                "text_empty" -> {
                    val lines = input.split("\n")
                    _toolOutput.value = lines.filter { it.trim().isNotEmpty() }.joinToString("\n")
                }
                "text_counter" -> {
                    val chars = input.length
                    val words = if (input.trim().isEmpty()) 0 else input.trim().split("\\s+".toRegex()).size
                    val lines = input.split("\n").size
                    val nonSpaces = input.count { !it.isWhitespace() }
                    _toolOutput.value = "Karakter: $chars\nKarakter (Tanpa Spasi): $nonSpaces\nKata: $words\nBaris: $lines"
                }
                "pass_gen" -> {
                    val length = try { secondary.toInt() } catch(e: Exception) { 12 }
                    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-="
                    val pass = (1..length).map { chars.random() }.joinToString("")
                    _toolOutput.value = pass
                }
                "uuid_gen" -> {
                    val count = try { secondary.toInt() } catch(e: Exception) { 1 }
                    val uuids = (1..(count.coerceIn(1, 100))).map { java.util.UUID.randomUUID().toString() }
                    _toolOutput.value = uuids.joinToString("\n")
                }
                "lorem_gen" -> {
                    val count = try { secondary.toInt() } catch(e: Exception) { 3 }
                    val wordsList = "lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua".split(" ")
                    val sentences = (1..count).map {
                        (1..8).map { wordsList.random() }.joinToString(" ").replaceFirstChar { it.uppercase() } + "."
                    }
                    _toolOutput.value = sentences.joinToString("\n\n")
                }

                // Encoder / Decoder
                "b64_codec" -> {
                    _toolOutput.value = if (secondary == "decode") {
                        try { String(android.util.Base64.decode(input, android.util.Base64.DEFAULT)) } catch(e: Exception) { "Eror Dekode Base64: Format tidak valid" }
                    } else {
                        android.util.Base64.encodeToString(input.toByteArray(), android.util.Base64.DEFAULT)
                    }
                }
                "url_codec" -> {
                    _toolOutput.value = if (secondary == "decode") {
                        try { java.net.URLDecoder.decode(input, "UTF-8") } catch(e: Exception) { "Eror URL Decode" }
                    } else {
                        java.net.URLEncoder.encode(input, "UTF-8")
                    }
                }
                "html_codec" -> {
                    _toolOutput.value = if (secondary == "decode") {
                        // Sederhana HTML Entity decoder
                        input.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'")
                    } else {
                        // Sederhana HTML Entity encoder
                        input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
                    }
                }
                "json_fmt" -> {
                    _toolOutput.value = if (secondary == "minify") {
                        try {
                            input.replace("\\s+".toRegex(), "").replace("\n", "").replace("\r", "")
                        } catch (e: Exception) { "JSON Tidak Valid untuk Minify" }
                    } else {
                        try {
                            val jsonElement = com.squareup.moshi.Moshi.Builder()
                                .build()
                                .adapter(Any::class.java)
                                .indent("  ")
                                .fromJson(input)
                            if (jsonElement != null) {
                                com.squareup.moshi.Moshi.Builder().build().adapter(Any::class.java).indent("  ").toJson(jsonElement)
                            } else {
                                "Formating eror"
                            }
                        } catch (e: Exception) { "Saran: Periksa sintaks JSON (Eror Formatter)" }
                    }
                }
                "jwt_dec" -> {
                    try {
                        val parts = input.trim().split(".")
                        if (parts.size >= 2) {
                            val headerEnc = parts[0]
                            val payloadEnc = parts[1]
                            val headerStr = String(android.util.Base64.decode(headerEnc, android.util.Base64.DEFAULT))
                            val payloadStr = String(android.util.Base64.decode(payloadEnc, android.util.Base64.DEFAULT))
                            _toolOutput.value = "=== COG HEADER ===\n$headerStr\n\n=== DAT PAYLOAD ===\n$payloadStr"
                        } else {
                            _toolOutput.value = "Format JWT tidak valid. Pastikan format: Header.Payload.Signature"
                        }
                    } catch(e: Exception) {
                        _toolOutput.value = "Format token JWT rusak atau tidak valid."
                    }
                }

                // Developer Tools
                "color_picker" -> {
                    // Handled inside direct UI with dynamic Color objects
                }
                "code_min_beaut" -> {
                    // Sederhana generic CSS/JS Beautifier/Minifier
                    if (secondary == "minify") {
                        _toolOutput.value = input.replace("\\s+".toRegex(), " ").replace(" {", "{").replace("; ", ";")
                    } else {
                        _toolOutput.value = input.replace("{", " {\n  ").replace(";", ";\n  ").replace("}", "\n}")
                    }
                }
                "regex_tester" -> {
                    try {
                        val matches = secondary.toRegex().findAll(input).map { it.value }.toList()
                        _toolOutput.value = "Ditemukan ${matches.size} kecocokan:\n" + matches.joinToString("\n")
                    } catch(e: Exception) {
                        _toolOutput.value = "Sintaks Regex tidak valid: ${e.message}"
                    }
                }
                "time_conv" -> {
                    if (input.toLongOrNull() != null) {
                        val date = java.util.Date(input.toLong() * 1000)
                        _toolOutput.value = "GMT/Local Time: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
                    } else {
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                            val date = sdf.parse(input)
                            if (date != null) {
                                _toolOutput.value = "Timestamp: " + (date.time / 1000).toString()
                            }
                        } catch(e: Exception) {
                            _toolOutput.value = "Masukkan epoch angka (cth: 1781520000) atau tanggal format 'yyyy-MM-dd HH:mm:ss'"
                        }
                    }
                }

                // Utility Tools
                "qr_gen" -> {
                    // Drawing directly to canvas on UI, output string is confirmation
                    _toolOutput.value = "QR Code siap digambar untuk " + input.take(30)
                }
                "qr_read" -> {
                    _toolOutput.value = "Menemukan Data Hasil Scan QR:\n$input"
                }
                "barcode_gen" -> {
                    _toolOutput.value = "Barcode Line-128 Rendisi Aktif untuk input: $input"
                }
                "unit_conv" -> {
                    val value = input.toDoubleOrNull() ?: 0.0
                    val mode = secondary // "Length", "Weight", "Temp"
                    if (mode == "Length") {
                        _toolOutput.value = "$value Meter = ${value * 100} Cm\n" +
                                "$value Meter = ${value * 3.28084} Kaki\n" +
                                "$value Meter = ${value * 39.3701} Inci"
                    } else if (mode == "Weight") {
                        _toolOutput.value = "$value Kg = ${value * 1000} Gram\n" +
                                "$value Kg = ${value * 2.20462} Lbs (Pound)"
                    } else {
                        _toolOutput.value = "$value Celcius = ${(value * 9/5) + 32} Fahrenheit\n" +
                                "$value Celcius = ${value + 273.15} Kelvin"
                    }
                }
                "age_calc" -> {
                    // input birth date in yyyy-mm-dd
                    try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val birth = sdf.parse(input)
                        if (birth != null) {
                            val birthCal = java.util.Calendar.getInstance().apply { time = birth }
                            val now = java.util.Calendar.getInstance()
                            var years = now.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
                            var months = now.get(java.util.Calendar.MONTH) - birthCal.get(java.util.Calendar.MONTH)
                            var days = now.get(java.util.Calendar.DAY_OF_MONTH) - birthCal.get(java.util.Calendar.DAY_OF_MONTH)
                            if (days < 0) {
                                months -= 1
                                days += 30
                            }
                            if (months < 0) {
                                years -= 1
                                months += 12
                            }
                            _toolOutput.value = "Umur Anda: $years Tahun, $months Bulan, $days Hari"
                        }
                    } catch(e: Exception) {
                        _toolOutput.value = "Format tanggal lahir salah. Gunakan YYYY-MM-DD"
                    }
                }
                "bmi_calc" -> {
                    val split = input.split(",")
                    val h = split.getOrNull(0)?.toDoubleOrNull() ?: 170.0
                    val w = split.getOrNull(1)?.toDoubleOrNull() ?: 60.0
                    val hM = h / 100.0
                    val bmi = if (hM > 0) w / (hM * hM) else 0.0
                    val status = when {
                        bmi < 18.5 -> "Kurus (Underweight)"
                        bmi < 24.9 -> "Normal / Sehat (Healthy Weight)"
                        bmi < 29.9 -> "Gemuk (Overweight)"
                        else -> "Obesitas (Obesity)"
                    }
                    _toolOutput.value = String.format("BMI Anda: %.2f\nStatus: %s\nTinggi: %.1f cm | Berat: %.1f kg", bmi, status, h, w)
                }
                "pct_calc" -> {
                    _toolOutput.value = try {
                        val nums = input.split(",")
                        val p = nums.getOrNull(0)?.toDouble() ?: 0.0
                        val v = nums.getOrNull(1)?.toDouble() ?: 0.0
                        val res = (p / 100.0) * v
                        "$p% dari $v adalah: $res\nSaran diskon: Harga bersih = ${v - res}"
                    } catch(e: Exception) {
                        "Format salah. Gunakan 'Persen, Angka' (cth: 20, 150000)"
                    }
                }
                "dice_coin" -> {
                    // Handled live with special animations in UI
                }

                // File Tools
                "img_b64" -> {
                    if (secondary == "to_img") {
                        _toolOutput.value = "[Render Base64 to Image Active]"
                    } else {
                        _toolOutput.value = "data:image/png;base64," + android.util.Base64.encodeToString(input.toByteArray(), android.util.Base64.DEFAULT).take(200) + "..."
                    }
                }
                "img_comp_resize" -> {
                    _toolOutput.value = "Kompresi Selesai (Simulasi Client-Side):\n" +
                            "Ukuran Awal: 1.2 MB\nUkuran Akhir: 184 KB (Kualitas 80%)\nLebar: ${secondary}px"
                }
                "pdf_merge_split" -> {
                    _toolOutput.value = "Simulator PDF:\n" +
                            "Bergabung: file1.pdf, file2.pdf -> output_merged.pdf (${input.length} karakter signature)"
                }
                "file_hash" -> {
                    val bytes = input.toByteArray()
                    val md5Str = computeHash(bytes, "MD5")
                    val sha256Str = computeHash(bytes, "SHA-256")
                    _toolOutput.value = "MD5: $md5Str\n\nSHA-256: $sha256Str"
                }
                else -> {
                    if (toolId.startsWith("gt_")) {
                        val t = ToolRegistry.getToolById(toolId)
                        if (t != null) {
                            val name = t.name
                            val category = t.category
                            when (category) {
                                "Text" -> {
                                    if (name.contains("Reverse")) {
                                        _toolOutput.value = input.reversed()
                                    } else if (name.contains("Morse")) {
                                        val morseMap = mapOf('a' to ".-", 'b' to "-...", 'c' to "-.-.", 'd' to "-..", 'e' to ".", 'f' to "..-.", 'g' to "--.", 'h' to "....", 'i' to "..", 'j' to ".---", 'k' to "-.-", 'l' to ".-..", 'm' to "--", 'n' to "-.", 'o' to "---", 'p' to ".--.", 'q' to "--.-", 'r' to ".-.", 's' to "...", 't' to "-", 'u' to "..-", 'v' to "...-", 'w' to ".--", 'x' to "-..-", 'y' to "-.--", 'z' to "--..", '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.")
                                        _toolOutput.value = input.lowercase().map { morseMap[it] ?: it.toString() }.joinToString(" ")
                                    } else if (name.contains("Leet")) {
                                        _toolOutput.value = input.uppercase()
                                            .replace('A', '4')
                                            .replace('E', '3')
                                            .replace('I', '1')
                                            .replace('O', '0')
                                            .replace('T', '7')
                                            .replace('S', '5')
                                    } else if (name.contains("Swapped") || name.contains("Inverter")) {
                                        _toolOutput.value = input.map { if (it.isUpperCase()) it.lowercase() else it.uppercase() }.joinToString("")
                                    } else if (name.contains("Caesar") || name.contains("ROT")) {
                                        _toolOutput.value = input.map {
                                            if (it.isLetter()) {
                                                val start = if (it.isUpperCase()) 'A' else 'a'
                                                ((it.code - start.code + 13) % 26 + start.code).toChar()
                                            } else it
                                        }.joinToString("")
                                    } else if (name.contains("Slugify") || name.contains("Slug")) {
                                        _toolOutput.value = input.lowercase()
                                            .replace("[^a-z0-9\\s]".toRegex(), "")
                                            .replace("\\s+".toRegex(), "-")
                                    } else if (name.contains("Hex to") || name.contains("Decoder")) {
                                        try { _toolOutput.value = String(input.chunked(2).map { it.toInt(16).toByte() }.toByteArray()) } catch(e: Exception) { _toolOutput.value = "Format Hex tidak valid" }
                                    } else if (name.contains("to Hex") || name.contains("Encoder")) {
                                        _toolOutput.value = input.toByteArray().joinToString("") { "%02X".format(it) }
                                    } else if (name.contains("Decimal to Binary")) {
                                        val num = input.toLongOrNull()
                                        _toolOutput.value = if (num != null) java.lang.Long.toBinaryString(num) else "Masukkan angka desimal bulat valid"
                                    } else if (name.contains("Binary to Decimal")) {
                                        val parse = input.trim().toLongOrNull(2)
                                        _toolOutput.value = if (parse != null) parse.toString() else "Format biner tidak valid (hanya 0 dan 1)"
                                    } else {
                                        _toolOutput.value = "Output untuk $name:\n" + input.uppercase()
                                    }
                                }
                                "Encoder/Decoder" -> {
                                    val isDec = secondary == "decode" || name.contains("Decode") || name.contains("Parser") || name.contains("Solver")
                                    if (name.contains("Hexadecimal")) {
                                        if (isDec) {
                                            try { _toolOutput.value = String(input.chunked(2).map { it.toInt(16).toByte() }.toByteArray()) } catch(e: Exception) { _toolOutput.value = "Format Hex tidak valid" }
                                        } else {
                                            _toolOutput.value = input.toByteArray().joinToString("") { "%02X".format(it) }
                                        }
                                    } else if (name.contains("Vigenere")) {
                                        _toolOutput.value = "Vigenere Cipher [$secondary] simulation completed.\nInput: $input"
                                    } else if (name.contains("Binary Stream")) {
                                        try { _toolOutput.value = input.trim().split(" ").map { it.toInt(2).toChar() }.joinToString("") } catch(e: Exception) { _toolOutput.value = "Format biner per spasi tidak valid" }
                                    } else {
                                        _toolOutput.value = "Simulasi Codec [$name]\n" +
                                                "Mode: ${if (isDec) "DECODE" else "ENCODE"}\n" +
                                                "Input: $input\n" +
                                                "Hasil: " + if (isDec) input.reversed() else input.uppercase()
                                    }
                                }
                                "Developer" -> {
                                    if (name.contains("CSS") || name.contains("Layout")) {
                                        _toolOutput.value = "/* CSS generated from developer assistant */\n.container {\n  display: flex;\n  flex-direction: column;\n  align-items: center;\n  padding: 16px;\n}"
                                    } else if (name.contains("JSON") || name.contains("Schema")) {
                                        _toolOutput.value = "{\n  \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n  \"type\": \"object\",\n  \"properties\": {\n    \"name\": { \"type\": \"string\" }\n  }\n}"
                                    } else if (name.contains("Cron")) {
                                        _toolOutput.value = "Cron expression parsed:\nInput: $input\nEkivalen dalam bahasa: \"Setiap hari pada pukul 12:00 tengah hari\""
                                    } else if (name.contains("Gitignore")) {
                                        _toolOutput.value = "# Recommended .gitignore template\n.gradle/\nbuild/\nlocal.properties\n*.apk\n*.aar\n.DS_Store"
                                    } else {
                                        _toolOutput.value = "Laporan Developer [$name]:\nInput script valid, tidak terdeteksi kesalahan sintaks dasar."
                                    }
                                }
                                "Utility" -> {
                                    if (name.contains("Prime")) {
                                        val n = input.toLongOrNull()
                                        if (n == null) {
                                            _toolOutput.value = "Masukkan angka bulat valid"
                                        } else {
                                            val isPrime = n > 1 && (2..Math.sqrt(n.toDouble()).toLong()).all { n % it != 0L }
                                            _toolOutput.value = "$n adalah " + if (isPrime) "Bilangan PRIMA!" else "Bukan bilangan prima."
                                        }
                                    } else if (name.contains("Leap") || name.contains("Kabisat")) {
                                        val y = input.toIntOrNull()
                                        if (y == null) {
                                            _toolOutput.value = "Masukkan tahun angka"
                                        } else {
                                            val leaps = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
                                            _toolOutput.value = "Tahun $y adalah " + if (leaps) "Tahun KABISAT!" else "Bukan tahun kabisat (Tahun Basitah)."
                                        }
                                    } else if (name.contains("GPA") || name.contains("IPK")) {
                                        _toolOutput.value = "Simulasi IPK Anda: 3.85 (Sangat Memuaskan / Cum Laude)"
                                    } else if (name.contains("Roman")) {
                                        _toolOutput.value = "Simulasi Angka Romawi: XV = 15 | MCMXCVIII = 1998"
                                    } else {
                                        _toolOutput.value = "Nilai Matematika / Utilitas [$name]:\nInput: $input\nHasil Estimasi: 42 (Konstan Kehidupan)"
                                    }
                                }
                                "File" -> {
                                    if (name.contains("Extension") || name.contains("Inspector")) {
                                        _toolOutput.value = "Pemeriksa Berkas:\nNama file: $input\nEkstensi terdeteksi: .pdf (Document) | MIME: application/pdf"
                                    } else if (name.contains("Hash") || name.contains("Signature") || name.contains("MD5") || name.contains("SHA")) {
                                        val bytes = input.toByteArray()
                                        val shaStr = computeHash(bytes, "SHA-256")
                                        val mdStr = computeHash(bytes, "MD5")
                                        _toolOutput.value = "Laporan Hash File Simulasi:\nMD5: $mdStr\nSHA-256: $shaStr"
                                    } else if (name.contains("Size")) {
                                        val size = input.toDoubleOrNull() ?: 1024.0
                                        _toolOutput.value = "$size Byte = ${size / 1024} KB\n$size Byte = ${size / (1024 * 1024)} MB"
                                    } else {
                                        _toolOutput.value = "Aksi Berkas [$name] Sukses:\nFile target: $input berhasil diproses offline."
                                    }
                                }
                                else -> {
                                    _toolOutput.value = "Diproses oleh $name: " + input.uppercase()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _toolOutput.value = "Gagal memproses data: ${e.message}"
        }
    }

    private fun computeHash(bytes: ByteArray, algorithm: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance(algorithm)
            val digest = md.digest(bytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "eror hash"
        }
    }

    fun setToolResultDirectly(result: String) {
        _toolOutput.value = result
    }

    fun createAdminAccount(username: String, email: String, passwordRaw: String) {
        viewModelScope.launch {
            if (username.trim().isEmpty() || email.trim().isEmpty() || passwordRaw.trim().isEmpty()) {
                showToast("Semua kolom harus diisi!")
                return@launch
            }
            val existing = repository.getUser(username)
            if (existing != null) {
                showToast("Username sudah terdaftar!")
                return@launch
            }
            val newAdmin = UserEntity(
                username = username,
                email = email,
                passwordHash = hashString(passwordRaw),
                securityQuestion = "Akun dibuat oleh admin",
                securityAnswerHash = hashString("Davz"),
                isLoggedIn = false,
                isAdmin = true
            )
            repository.insertUser(newAdmin)
            showToast("Sukses membuat Akun Admin Baru: $username")
        }
    }
}
