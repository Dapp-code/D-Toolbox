package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ToolboxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf("login") } // login, signup, reset

    // Login fields
    var loginUser by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var loginPassVisible by remember { mutableStateOf(false) }

    // Signup fields
    var signUpUser by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPass by remember { mutableStateOf("") }
    var signUpPassVisible by remember { mutableStateOf(false) }
    var signUpQuestion by remember { mutableStateOf("Siapa nama guru favorit Anda?") }
    var signUpAnswer by remember { mutableStateOf("") }

    // Reset fields
    var resetUser by remember { mutableStateOf("") }
    var resetQuestion by remember { mutableStateOf("Siapa nama guru favorit Anda?") }
    var resetAnswer by remember { mutableStateOf("") }
    var resetNewPass by remember { mutableStateOf("") }
    var resetPassVisible by remember { mutableStateOf(false) }

    val securityQuestions = listOf(
        "Siapa nama guru favorit Anda?",
        "Apa nama hewan peliharaan pertama Anda?",
        "Di kota mana orang tua Anda bertemu?",
        "Apa merek mobil pertama Anda?"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFF2B2B).copy(alpha = 0.4f), Color(0x1AFFFFFF))))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Glow App Logo Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0x19FF2B2B), RoundedCornerShape(50))
                        .border(1.5.dp, Color(0xFFFF2B2B), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFFFF2B2B),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "D-TOOLBOX UTILITY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = when (mode) {
                        "login" -> "Masuk untuk mengakses alat-alat pribadi Anda"
                        "signup" -> "Buat akun lokal baru yang disimpan 100% aman"
                        else -> "Atur ulang kata sandi dengan verifikasi keamanan"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Conditionally render forms
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "AuthFormTransition"
                ) { currentMode ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (currentMode) {
                            "login" -> {
                                OutlinedTextField(
                                    value = loginUser,
                                    onValueChange = { loginUser = it },
                                    label = { Text("Username") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("login_username_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = loginPass,
                                    onValueChange = { loginPass = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    trailingIcon = {
                                        IconButton(onClick = { loginPassVisible = !loginPassVisible }) {
                                            Icon(
                                                imageVector = if (loginPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (loginPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                    singleLine = true
                                )

                                Button(
                                    onClick = { viewModel.login(loginUser, loginPass) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("login_submit_button")
                                ) {
                                    Text("MASUK SIKAL", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Buat Akun",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF2B2B)),
                                        modifier = Modifier
                                            .clickable { mode = "signup" }
                                            .padding(8.dp)
                                            .testTag("go_to_signup_button")
                                    )

                                    Text(
                                        text = "Lupa Password?",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clickable { mode = "reset" }
                                            .padding(8.dp)
                                            .testTag("go_to_reset_button")
                                    )
                                }
                            }

                            "signup" -> {
                                OutlinedTextField(
                                    value = signUpUser,
                                    onValueChange = { signUpUser = it },
                                    label = { Text("Username Baru") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_username_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = signUpEmail,
                                    onValueChange = { signUpEmail = it },
                                    label = { Text("Alamat Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = signUpPass,
                                    onValueChange = { signUpPass = it },
                                    label = { Text("Password Baru") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    trailingIcon = {
                                        IconButton(onClick = { signUpPassVisible = !signUpPassVisible }) {
                                            Icon(
                                                imageVector = if (signUpPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (signUpPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                                    singleLine = true
                                )

                                // Security Question Section
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x05FFFFFF)),
                                    border = BorderStroke(1.dp, Color(0x13FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Pertanyaan Keamanan (Guna Reset Password):", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF2B2B))
                                        
                                        var expanded by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0x0CFFFFFF), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(8.dp))
                                                .clickable { expanded = true }
                                                .padding(12.dp)
                                        ) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(signUpQuestion, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                Icon(Icons.Default.Help, null, tint = Color(0xFFFF2B2B), modifier = Modifier.size(16.dp))
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(Color(0xFF1E1E1E))
                                            ) {
                                                securityQuestions.forEach { q ->
                                                    DropdownMenuItem(
                                                        text = { Text(q, color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            signUpQuestion = q
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = signUpAnswer,
                                            onValueChange = { signUpAnswer = it },
                                            label = { Text("Jawaban Keamanan") },
                                            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFFF2B2B),
                                                unfocusedBorderColor = Color(0x2BFFFFFF),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("signup_answer_input"),
                                            singleLine = true
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { 
                                        viewModel.signUp(signUpUser, signUpEmail, signUpPass, signUpQuestion, signUpAnswer)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("signup_submit_button")
                                ) {
                                    Text("DAFTAR SEKARANG", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }

                                Text(
                                    text = "Sudah punya akun? Kembali Login",
                                    style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { mode = "login" }
                                        .padding(8.dp)
                                        .testTag("back_to_login_button")
                                )
                            }

                            "reset" -> {
                                OutlinedTextField(
                                    value = resetUser,
                                    onValueChange = { resetUser = it },
                                    label = { Text("Username Anda") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reset_username_input"),
                                    singleLine = true
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x05FFFFFF)),
                                    border = BorderStroke(1.dp, Color(0x13FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Pilih pertanyaan keamanan yang Anda buat:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF2B2B))
                                        
                                        var expanded by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0x0CFFFFFF), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(8.dp))
                                                .clickable { expanded = true }
                                                .padding(12.dp)
                                        ) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(resetQuestion, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                Icon(Icons.Default.Help, null, tint = Color(0xFFFF2B2B), modifier = Modifier.size(16.dp))
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(Color(0xFF1E1E1E))
                                            ) {
                                                securityQuestions.forEach { q ->
                                                    DropdownMenuItem(
                                                        text = { Text(q, color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            resetQuestion = q
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = resetAnswer,
                                            onValueChange = { resetAnswer = it },
                                            label = { Text("Jawaban Keamanan") },
                                            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFFF2B2B),
                                                unfocusedBorderColor = Color(0x2BFFFFFF),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("reset_answer_input"),
                                            singleLine = true
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = resetNewPass,
                                    onValueChange = { resetNewPass = it },
                                    label = { Text("Password Baru Anda") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF2B2B)) },
                                    trailingIcon = {
                                        IconButton(onClick = { resetPassVisible = !resetPassVisible }) {
                                            Icon(
                                                imageVector = if (resetPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (resetPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF2B2B),
                                        unfocusedBorderColor = Color(0x2BFFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reset_password_input"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { 
                                        viewModel.resetPassword(resetUser, resetQuestion, resetAnswer, resetNewPass)
                                        mode = "login"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("reset_submit_button")
                                ) {
                                    Text("PEMBARUI PASSWORD", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }

                                Text(
                                    text = "Kembali ke Login",
                                    style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { mode = "login" }
                                        .padding(8.dp)
                                        .testTag("reset_back_button")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
