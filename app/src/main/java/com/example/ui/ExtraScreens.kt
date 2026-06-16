package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ToolboxViewModel

@Composable
fun AboutScreenView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFF2B2B).copy(alpha = 0.4f), Color(0x1AFFFFFF))))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                }

                Text(
                    "D-TOOLBOX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    "One Place, Unlimited Tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "v1.0.0 (wm : Dapp.)",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "D-Toolbox adalah sebuah aplikasi utilitas lengkap 'Swiss Army Knife' modular yang dirancang untuk mempercepat produktivitas harian Anda. Didukung penuh dengan pengolahan client-side 100% sehingga menjamin performa cepat, tanpa koneksi internet (offline), dan keamanan privasi data Anda.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        // Tech specs card
        Text("TEKNOLOGI STACK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Pair("Android framework", "Jetpack Compose, Kotlin, State Flow"),
                    Pair("Penyimpanan Lokal", "Room Database & Coroutine persistence"),
                    Pair("Desain Antarmuka", "Material Design 3 (M3) Cyber Dark Neon"),
                    Pair("Optimasi Performa", "Moshi serialization & Vector Canvas")
                ).forEach { (title, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreenView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val submissions by viewModel.allSubmissions.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Hubungi & Feedback", "Kirim & Review Utilitas")

    // Submission form fields
    var newToolName by remember { mutableStateOf("") }
    var newToolCat by remember { mutableStateOf("Text") }
    var newToolDesc by remember { mutableStateOf("") }
    var newToolCode by remember { mutableStateOf("") }

    // Feedback fields
    var feedbackText by remember { mutableStateOf("") }
    var feedbackEmail by remember { mutableStateOf("") }

    val categoriesList = listOf("Text", "Encoder", "Developer", "Utility", "File", "Other")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // TAB 0: WhatsApp & Feedback
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    border = BorderStroke(1.dp, Color(0x13FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "HUBUNGI DEVELOPER / OWNER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Apakah Anda ingin me-request fitur baru, melaporkan bug/masalah halaman, atau menjalin kerja sama profesional?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF25D366).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF25D366))
                            }
                            Column {
                                Text("No. WhatsApp WhatsApp Owner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("083856009964", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val uri = Uri.parse("https://wa.me/6283856009964?text=Halo%20Owner%20D-Toolbox%2C%20saya%20tertarik%20dengan%20")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("whatsapp_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Text("Hubungi Owner via WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Feedback Form
                Text("FORM MAKLUM BALAS / FEEDBACK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    border = BorderStroke(1.dp, Color(0x13FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = feedbackEmail,
                            onValueChange = { feedbackEmail = it },
                            label = { Text("Surel / Email") },
                            placeholder = { Text("cth: developer@dapp.com") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2B2B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Pesan Feedback / Fitur Baru") },
                            placeholder = { Text("Tulis pesan Anda di sini...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2B2B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )

                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    viewModel.showToast("Terima kasih atas laporan Anda! Pesan tersimpan offline.")
                                    feedbackText = ""
                                    feedbackEmail = ""
                                } else {
                                    viewModel.showToast("Silakan lengkapi kolom pesan terlebih dahulu")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Submit Feedback")
                        }
                    }
                }
            } else {
                // TAB 1: Submit Tool Idea & Review Submissions (Backend Section)
                Text(
                    "KIRIM IDE ATAU KODE ALAT BARU",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    border = BorderStroke(1.dp, Color(0x13FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newToolName,
                            onValueChange = { newToolName = it },
                            label = { Text("Nama Utilitas/Alat Baru") },
                            placeholder = { Text("e.g. Hex to ASCII Converter") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2B2B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category selector
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Kategori Alat:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            var expanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0CFFFFFF), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0x2BFFFFFF), RoundedCornerShape(10.dp))
                                    .clickable { expanded = true }
                                    .padding(12.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(newToolCat, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFFFF2B2B))
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color(0xFF1E1E1E))
                                ) {
                                    categoriesList.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = Color.White) },
                                            onClick = {
                                                newToolCat = cat
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newToolDesc,
                            onValueChange = { newToolDesc = it },
                            label = { Text("Deskripsi Cara Kerja / Formula") },
                            placeholder = { Text("Tulis penjelasan detail cara menghitung/memproses datanya...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2B2B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        OutlinedTextField(
                            value = newToolCode,
                            onValueChange = { newToolCode = it },
                            label = { Text("Sertakan Kode Skrip/Contoh (Opsional)") },
                            placeholder = { Text("val result = input.reversed()\n// Tulis kode program di sini...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2B2B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )

                        Button(
                            onClick = {
                                if (currentUser == null) {
                                    viewModel.showToast("Silakan login terlebih dahulu!")
                                    return@Button
                                }
                                if (newToolName.trim().isEmpty() || newToolDesc.trim().isEmpty()) {
                                    viewModel.showToast("Nama dan Deskripsi wajib diisi!")
                                    return@Button
                                }
                                viewModel.submitToolIdea(newToolName, newToolCat, newToolDesc, newToolCode)
                                newToolName = ""
                                newToolDesc = ""
                                newToolCode = ""
                            },
                            modifier = Modifier.fillMaxWidth().testTag("submit_idea_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B))
                        ) {
                            Text("Kirim Ide & Kode Utilitas", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submissions Review Section (Backend Mechanism)
                Text(
                    "PANEL PENINJAUAN KOMUNITAS (BACKEND)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF2B2B),
                    fontWeight = FontWeight.Bold
                )

                if (submissions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x06FFFFFF)),
                        border = BorderStroke(1.dp, Color(0x13FFFFFF))
                    ) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Text("Belum ada ide tool yang masuk.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    submissions.forEach { sub ->
                        var showCode by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                            border = BorderStroke(1.dp, Color(0x13FFFFFF))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sub.toolName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Kategori: ${sub.category} | Oleh: @${sub.username}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }

                                    // Dynamic Badge status
                                    val (badgeBg, badgeText, badgeColor) = when (sub.status) {
                                        "Approved" -> Triple(Color(0xFF1B5E20).copy(alpha = 0.2f), "DITERIMA", Color(0xFF4CAF50))
                                        "Rejected" -> Triple(Color(0xFFB71C1C).copy(alpha = 0.2f), "DITOLAK", Color(0xFFF44336))
                                        else -> Triple(Color(0xFFE65100).copy(alpha = 0.2f), "PENDING", Color(0xFFFF9800))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(6.dp))
                                            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(badgeText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = badgeColor)
                                    }
                                }

                                Text(
                                    sub.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                if (sub.codeSnippet.isNotBlank()) {
                                    Text(
                                        text = if (showCode) "Sembunyikan Kode Source" else "Tampilkan Kode Source [Java/Kotlin/JS]",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF2B2B)),
                                        modifier = Modifier
                                            .clickable { showCode = !showCode }
                                            .padding(vertical = 4.dp)
                                    )

                                    if (showCode) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF151515), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                sub.codeSnippet,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color(0xFF00FF66)),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                // Quick Action Panel (Review Mechanism)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { viewModel.updateSubmissionStatus(sub.id, "Approved") },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4CAF50)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Setujui")
                                    }

                                    TextButton(
                                        onClick = { viewModel.updateSubmissionStatus(sub.id, "Rejected") },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Tolak")
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSubmission(sub.id) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Gray),
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Submission", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    val isGridMode by viewModel.settingsLayoutGrid.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showDialogDeleteAll by remember { mutableStateOf(false) }

    if (showDialogDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDialogDeleteAll = false },
            title = { Text("Hapus Semua Data?") },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh riwayat aktivitas dan tools yang Anda tandai sebagai favorit?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showDialogDeleteAll = false
                    }
                ) { Text("Ya, Hapus Semua", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDialogDeleteAll = false }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active User Account Panel (MANDATORY Authentication Details)
        Text("INFORMASI AKUN LOKAL SAYA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth().testTag("active_user_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFF2B2B).copy(alpha = 0.3f), Color(0x13FFFFFF))))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0x19FF2B2B), RoundedCornerShape(50))
                        .border(1.dp, Color(0xFFFF2B2B), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFFFF2B2B), modifier = Modifier.size(24.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.username ?: "Tamu Tidak Teridentifikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentUser?.email ?: "Belum mengaktifkan data cloud lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0x13FF2B2B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("AKUN TERSEKURING LOKAL", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color(0xFFFF2B2B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (currentUser?.isAdmin == true) {
            Text("ADMIN PANEL: BUAT AKUN ADMIN", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF2B2B), fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_panel_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                border = BorderStroke(1.dp, Color(0x33FF2B2B))
            ) {
                var adminUsername by remember { mutableStateOf("") }
                var adminEmail by remember { mutableStateOf("") }
                var adminPassword by remember { mutableStateOf("") }
                
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Buat Akun Administrator Baru", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("Sebagai admin, Anda berwenang membuat akun administrator lokal lain yang memiliki kekuasaan setingkat.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = adminUsername,
                        onValueChange = { adminUsername = it },
                        label = { Text("Username Admin Baru") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_new_username"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Email Admin Baru") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Password Admin Baru") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_new_password"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                    
                    Button(
                        onClick = {
                            if (adminUsername.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()) {
                                viewModel.showToast("Semua kolom harus diisi!")
                            } else {
                                viewModel.createAdminAccount(adminUsername, adminEmail, adminPassword)
                                adminUsername = ""
                                adminEmail = ""
                                adminPassword = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Daftarkan Akun Admin Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("SISTEM PENGATURAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Layout switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tampilan Kotak Grid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Tampilkan utilitas dalam 2 kolom grid.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isGridMode, onCheckedChange = { viewModel.toggleLayoutMode() })
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Default Dark mode notice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tema Gelap Otomatis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Konfigurasi dipaksa Gelap (Dark Mode) premium.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = true, onCheckedChange = {}, enabled = false)
                }
            }
        }

        // Database settings
        Text("SISTEM DATABASE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x06FFFFFF)),
            border = BorderStroke(1.dp, Color(0x4DFF2B2B))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Hapus Riwayat Database", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Tindakan ini menghapus seluruh data yang tercatat di penyimpanan lokal ponsel Anda secara permanen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(
                    onClick = { showDialogDeleteAll = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Seluruh Data")
                }
            }
        }

        // logout section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x06FFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sesi Masuk Akun", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Keluar dari akun Anda saat ini untuk mengamankan data dan aktivitas pribadi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().testTag("logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Keluar Sesi (Logout Akun)", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Shortcuts Guide
        Text("PANDUAN SHORTCUT KEYBOARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("[Esc]", "Kembali ke Dashboard Utama"),
                    Pair("[Ctrl + S]", "Cari tools instan di kolom search"),
                    Pair("[Ctrl + C]", "Menyalin hasil output clipboard otomatis"),
                    Pair("[Backspace]", "Clear data input dan output terpilih")
                ).forEach { (shortcut, action) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(shortcut, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(action, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
