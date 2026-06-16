package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tool
import com.example.model.ToolRegistry
import com.example.viewmodel.ToolboxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailView(
    viewModel: ToolboxViewModel,
    toolId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tool = remember(toolId) { ToolRegistry.getToolById(toolId) }
    val favorites by viewModel.favoriteTools.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    val input by viewModel.toolInput.collectAsState()
    val output by viewModel.toolOutput.collectAsState()
    val secondaryInput by viewModel.toolSecondaryInput.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    if (tool == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tool tidak ditemukan.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Kembali") }
        }
        return
    }

    var showClearConfirmationDialog by remember { mutableStateOf(false) }

    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = { Text("Konfirmasi") },
            text = { Text("Apakah Anda yakin ingin menghapus data input dan output alat ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearToolData()
                        showClearConfirmationDialog = false
                    }
                ) { Text("Hapus", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text(tool.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(tool.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(tool) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorit",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showClearConfirmationDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clear_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }

                Button(
                    onClick = {
                        if (output.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(output))
                            viewModel.showToast("Hasil disalin ke Clipboard")
                        } else {
                            viewModel.showToast("Tidak ada hasil untuk disalin")
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("copy_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Hasil")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Deskripsi Tool
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                border = BorderStroke(1.dp, Color(0x13FFFFFF))
            ) {
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }

            // Area Input dan Kontrol Custom
            Text("INPUT DATA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            InteractiveControlArea(
                toolId = toolId,
                input = input,
                secondaryInput = secondaryInput,
                onInputChange = { viewModel.updateToolInput(it) },
                onSecondaryChange = { viewModel.updateSecondaryInput(it) },
                viewModel = viewModel
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

            // Area Output
            Text("HASIL / OUTPUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("output_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                border = BorderStroke(1.dp, Color(0x13FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (output.isEmpty()) {
                        Text(
                            text = "Hasil pemrosesan akan ditampilkan di sini secara otomatis...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    } else {
                        // Custom drawing outputs for visual-focused tools (QR, Barcode)
                        if (toolId == "qr_gen") {
                            VisualQrCodeGenerator(data = input)
                        } else if (toolId == "barcode_gen") {
                            VisualBarcodeGenerator(data = input)
                        } else {
                            Text(
                                text = output,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveControlArea(
    toolId: String,
    input: String,
    secondaryInput: String,
    onInputChange: (String) -> Unit,
    onSecondaryChange: (String) -> Unit,
    viewModel: ToolboxViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    when (toolId) {
        // Text conversion modes (Simple Input box)
        "text_upper", "text_lower", "text_dup", "text_empty", "text_counter", "file_hash" -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(150.dp).testTag("input_field"),
                placeholder = { Text("Ketik atau tempel teks di sini...") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
        }

        // Random Password generator (Length Control, characters list)
        "pass_gen" -> {
            var selectedLen by remember { mutableStateOf(16f) }
            LaunchedEffect(selectedLen) {
                onSecondaryChange(selectedLen.toInt().toString())
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Panjang Password: ${selectedLen.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = selectedLen,
                    onValueChange = { selectedLen = it },
                    valueRange = 6f..64f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Button(
                    onClick = { onInputChange(System.currentTimeMillis().toString()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acak Sekarang")
                }
            }
        }

        "uuid_gen" -> {
            var modeBatch by remember { mutableStateOf(1f) }
            LaunchedEffect(modeBatch) {
                onSecondaryChange(modeBatch.toInt().toString())
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jumlah UUID: ${modeBatch.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = modeBatch,
                    onValueChange = { modeBatch = it },
                    valueRange = 1f..20f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
                Button(
                    onClick = { onInputChange(System.currentTimeMillis().toString()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hasilkan UUID")
                }
            }
        }

        "lorem_gen" -> {
            var paragraphs by remember { mutableStateOf(3f) }
            LaunchedEffect(paragraphs) {
                onSecondaryChange(paragraphs.toInt().toString())
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Jumlah Paragraf: ${paragraphs.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = paragraphs,
                    onValueChange = { paragraphs = it },
                    valueRange = 1f..15f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
                Button(onClick = { onInputChange(System.currentTimeMillis().toString()) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Buat Teks Placeholder")
                }
            }
        }

        // Base64 & Encoders
        "b64_codec", "url_codec", "html_codec" -> {
            val mode = if (secondaryInput == "decode") "decode" else "encode"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onSecondaryChange("encode") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (mode == "encode") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Encode") }
                Button(
                    onClick = { onSecondaryChange("decode") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (mode == "decode") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Decode") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("codec_input_field"),
                placeholder = { Text("Masukkan kode teks...") }
            )
        }

        "json_fmt", "code_min_beaut" -> {
            val modeFormat = if (secondaryInput == "minify") "minify" else "beautify"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onSecondaryChange("beautify") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (modeFormat == "beautify") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Beautify") }
                Button(
                    onClick = { onSecondaryChange("minify") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (modeFormat == "minify") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Minify") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                placeholder = { Text("Tempel script / data JSON di sini...") }
            )
        }

        "jwt_dec" -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Masukkan token JWT di sini (Header.Payload.Signature)...") }
            )
        }

        // Color Picker & Converter
        "color_picker" -> {
            var redVal by remember { mutableStateOf(255f) }
            var greenVal by remember { mutableStateOf(43f) }
            var blueVal by remember { mutableStateOf(43f) }

            val colorHex = String.format("#%02X%02X%02X", redVal.toInt(), greenVal.toInt(), blueVal.toInt())
            LaunchedEffect(redVal, greenVal, blueVal) {
                onInputChange("$redVal,$greenVal,$blueVal")
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(redVal.toInt(), greenVal.toInt(), blueVal.toInt()))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hex: $colorHex\nRGB: (${redVal.toInt()}, ${greenVal.toInt()}, ${blueVal.toInt()})",
                        color = if ((redVal*0.299 + greenVal*0.587 + blueVal*0.114) > 186) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text("Red: ${redVal.toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = redVal, onValueChange = { redVal = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(activeTrackColor = Color.Red, thumbColor = Color.Red))

                Text("Green: ${greenVal.toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = greenVal, onValueChange = { greenVal = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(activeTrackColor = Color.Green, thumbColor = Color.Green))

                Text("Blue: ${blueVal.toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = blueVal, onValueChange = { blueVal = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(activeTrackColor = Color.Blue, thumbColor = Color.Blue))
            }
        }

        // Regex Tester
        "regex_tester" -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = secondaryInput,
                    onValueChange = onSecondaryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Regex Pattern") },
                    placeholder = { Text("cth: [a-zA-Z]+") }
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    label = { Text("Input Teks") }
                )
            }
        }

        // Timestamp
        "time_conv" -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unix Timestamp / Tanggal") },
                    placeholder = { Text("cth: 1781520000 atau 2026-06-15 17:15:00") }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onInputChange((System.currentTimeMillis() / 1000).toString()) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Gunakan Epoch Sekarang") }
                }
            }
        }

        "qr_gen", "barcode_gen" -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan konten kode (Teks / Tautan)...") }
            )
        }

        "qr_read" -> {
            var selectedUrl by remember { mutableStateOf("https://dtoolbox.com/owner-profile") }
            LaunchedEffect(selectedUrl) {
                onInputChange(selectedUrl)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Simulasi Upload Kode QR:", style = MaterialTheme.typography.bodySmall)
                listOf("Tautan Portfolio Owner", "WhatsApp WhatsApp Nomor", "Vcard Kontak").forEach { mode ->
                    val url = when(mode) {
                        "Tautan Portfolio Owner" -> "https://dtoolbox.com/owner-profile"
                        "WhatsApp WhatsApp Nomor" -> "https://wa.me/6283856009964"
                        else -> "BEGIN:VCARD\nFN:Owner D-Toolbox\nTEL:083856009964\nEND:VCARD"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedUrl == url) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, if (selectedUrl == url) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedUrl = url }
                            .padding(12.dp)
                    ) {
                        Text(mode, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        "unit_conv" -> {
            val listUnitTypes = listOf("Length", "Weight", "Temp")
            val currentSelected = if (secondaryInput.isEmpty()) "Length" else secondaryInput

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listUnitTypes.forEach { type ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (currentSelected == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSecondaryChange(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (currentSelected == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Nilai Unit") },
                placeholder = { Text("Masukkan angka...") }
            )
        }

        "age_calc" -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tanggal Lahir (YYYY-MM-DD)") },
                placeholder = { Text("cth: 2005-04-12") }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onInputChange("1998-10-24") }, modifier = Modifier.weight(1f)) {
                    Text("Coba Contoh")
                }
            }
        }

        "bmi_calc" -> {
            var heightSlider by remember { mutableStateOf(170f) }
            var weightSlider by remember { mutableStateOf(65f) }
            LaunchedEffect(heightSlider, weightSlider) {
                onInputChange("$heightSlider,$weightSlider")
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tinggi: ${heightSlider.toInt()} cm", style = MaterialTheme.typography.bodySmall)
                Slider(value = heightSlider, onValueChange = { heightSlider = it }, valueRange = 100f..220f, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary))

                Text("Berat: ${weightSlider.toInt()} kg", style = MaterialTheme.typography.bodySmall)
                Slider(value = weightSlider, onValueChange = { weightSlider = it }, valueRange = 30f..150f, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary))
            }
        }

        "pct_calc" -> {
            var pctVal by remember { mutableStateOf("10") }
            var totalVal by remember { mutableStateOf("150000") }
            LaunchedEffect(pctVal, totalVal) {
                onInputChange("$pctVal,$totalVal")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pctVal,
                        onValueChange = { pctVal = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Persen (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = totalVal,
                        onValueChange = { totalVal = it },
                        modifier = Modifier.weight(2f),
                        label = { Text("Total Nilai") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        "dice_coin" -> {
            var rollD6 by remember { mutableStateOf(1) }
            var coinRes by remember { mutableStateOf("HEAD") }
            var isAnimating by remember { mutableStateOf(false) }

            LaunchedEffect(rollD6, coinRes) {
                onInputChange("Dadu: $rollD6\nKoin: $coinRes")
            }

            Column(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Visual Dice representation
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rollD6.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dadu D6", style = MaterialTheme.typography.bodySmall)
                    }

                    // Visual Coin representation
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(50))
                                .border(2.dp, Color.White, RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coinRes.take(1),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Koin ($coinRes)", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isAnimating = true
                            repeat(6) {
                                rollD6 = (1..6).random()
                                coinRes = if (Math.random() > 0.5) "HEAD" else "TAIL"
                                delay(100)
                            }
                            isAnimating = false
                            viewModel.showToast("Hasil Terkocok!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAnimating
                ) {
                    Text(if (isAnimating) "Mengocok..." else "Roll & Lempar Koin")
                }
            }
        }

        "img_b64" -> {
            val isDecodeMode = secondaryInput == "to_img"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onSecondaryChange("to_b64") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isDecodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Image -> Base64") }
                Button(
                    onClick = { onSecondaryChange("to_img") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDecodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Base64 -> Image") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text(if (isDecodeMode) "Masukkan kode teks base64..." else "Ketik teks untuk disimulasikan sebagai piksel gambar...") }
            )
        }

        "img_comp_resize" -> {
            var selectedWidth by remember { mutableStateOf(800f) }
            LaunchedEffect(selectedWidth) {
                onSecondaryChange(selectedWidth.toInt().toString())
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Simulasi Resolusi Lebar: ${selectedWidth.toInt()}px", style = MaterialTheme.typography.bodySmall)
                Slider(value = selectedWidth, onValueChange = { selectedWidth = it }, valueRange = 200f..2000f, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama Berkas") },
                    placeholder = { Text("cth: profile_photo.jpg") }
                )
            }
        }

        "pdf_merge_split" -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Simulasi Input Signature Berkas") },
                    placeholder = { Text("cth: digital_sign_key") }
                )
                Button(onClick = { onInputChange("PDF_FILE_TOKEN_ABCD_" + System.currentTimeMillis()) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih Gabungkan File")
                }
            }
        }

        "dl_yt", "dl_tt", "dl_ig", "dl_spotify" -> {
            var urlInput by remember { mutableStateOf(input) }
            var isExtracting by remember { mutableStateOf(false) }
            val downloadType = when (toolId) {
                "dl_yt" -> "YouTube Video/MP3"
                "dl_tt" -> "TikTok (Tanpa Watermark)"
                "dl_ig" -> "Instagram Reels/Post"
                else -> "Spotify Track/Playlist"
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Unduh Media $downloadType",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { 
                        urlInput = it
                        onInputChange(it)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("downloader_url_input"),
                    placeholder = { Text("Tempel Tautan / Link $downloadType di sini...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                )
                
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            isExtracting = true
                            coroutineScope.launch {
                                delay(1200)
                                isExtracting = false
                                val simResult = when (toolId) {
                                    "dl_yt" -> "🔍 PROSES PARSING YOUTUBE BERHASIL!\n-------------------------------\nJudul: Simulated Video From Link\nDurasi: 04:32 | Kualitas: 1080p MP4 / 320kbps MP3\n\nLink Unduhan Cepat:\n📥 [DOWNLOAD VIDEO HD (18.4 MB)]"
                                    "dl_tt" -> "🔍 PROSES INSTAN TIKTOK BERHASIL!\n-------------------------------\nKreator: @simulated_chef\nCaption: Best recipe in town!\n\nLink Unduhan Tanpa Watermark:\n📥 [DOWNLOAD VIDEO HD NO-WATERMARK (4.2 MB)]"
                                    "dl_ig" -> "🔍 INSTAGRAM MEDIA SAVED!\n-------------------------------\nTipe Post: Reels Short\nUploader: @travel_escape\n\nLink Unduhan Media:\n📥 [DOWNLOAD REEL MP4 (8.7 MB)]"
                                    else -> "🔍 SPOTIFY AUDIO EXTRACTION COMPLETE!\n-------------------------------\nLagu: Simulated Ambient Track\nArtis: Deep Reliever\nFormat: MP3 (320 kbps high bit-rate)\n\nLink Audio MP3:\n📥 [DOWNLOAD MP3 AUDIO (11.2 MB)]"
                                }
                                viewModel.setToolResultDirectly(simResult)
                            }
                        } else {
                            viewModel.showToast("Harap masukkan tautan link terlebih dahulu!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                    modifier = Modifier.fillMaxWidth().testTag("downloader_submit"),
                    enabled = !isExtracting
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengekstrak Media...")
                    } else {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh & Ekstrak")
                    }
                }
            }
        }

        "cal_tracker" -> {
            var weight by remember { mutableStateOf("") }
            var height by remember { mutableStateOf("") }
            var age by remember { mutableStateOf("") }
            var genderIsMale by remember { mutableStateOf(true) }
            var activityLevel by remember { mutableStateOf(1.2) }
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Form Hitung Kalori Harian (BMR & TDEE)", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Berat (kg)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Tinggi (cm)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Umur (Tahun)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF2B2B))
                    )
                    
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jenis Kelamin", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = genderIsMale,
                                onClick = { genderIsMale = true },
                                label = { Text("Pria") }
                            )
                            FilterChip(
                                selected = !genderIsMale,
                                onClick = { genderIsMale = false },
                                label = { Text("Wanita") }
                            )
                        }
                    }
                }
                
                Column {
                    Text("Tingkat Aktivitas Harian", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf(
                            Pair("Sangat Jarang", 1.2),
                            Pair("Ringan", 1.375),
                            Pair("Sedang", 1.55),
                            Pair("Sangat Aktif", 1.725)
                        ).forEach { (label, factor) ->
                            FilterChip(
                                selected = activityLevel == factor,
                                onClick = { activityLevel = factor },
                                label = { Text(label, fontSize = 9.sp) }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        val w = weight.toDoubleOrNull()
                        val h = height.toDoubleOrNull()
                        val a = age.toIntOrNull()
                        if (w == null || h == null || a == null) {
                            viewModel.showToast("Semua data numerik harus diisi dengan benar!")
                        } else {
                            val bmr = if (genderIsMale) {
                                10 * w + 6.25 * h - 5 * a + 5
                            } else {
                                10 * w + 6.25 * h - 5 * a - 161
                            }
                            val tdee = bmr * activityLevel
                            val report = buildString {
                                append("📊 HASIL ANALISIS KALORI HARIAN ANDA:\n")
                                append("-------------------------------------------\n")
                                append(String.format("BMR (Basal Metabolic Rate): %.0f Kkal/hari\n", bmr))
                                append(String.format("TDEE (Total Energi Maintenis): %.0f Kkal/hari\n\n", tdee))
                                append("💡 PROGRAM TARGET DIET REKOMENDASI:\n")
                                append(String.format("🔥 Defisit Kalori (Turun BB): %.0f Kkal/hari (Nutrisi Bersih)\n", tdee - 500))
                                append(String.format("⚡ Surplus Kalori (Naik Massa Otot): %.0f Kkal/hari\n", tdee + 400))
                                append(String.format("🌱 Pemeliharaan Tubuh Sehat: %.0f Kkal/hari\n", tdee))
                                append("-------------------------------------------")
                            }
                            viewModel.setToolResultDirectly(report)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B2B)),
                    modifier = Modifier.fillMaxWidth().testTag("calorie_calc_button")
                ) {
                    Text("Hitung Kebutuhan Kalori", fontWeight = FontWeight.Bold)
                }
            }
        }

        "full_calculator" -> {
            var calcExpr by remember { mutableStateOf("") }
            
            // Evaluator helper inside the click block
            fun parseSimpleEquation(expr: String): Double {
                val tokens = mutableListOf<String>()
                var numberAccum = ""
                for (char in expr) {
                    if (char.isDigit() || char == '.') {
                        numberAccum += char
                    } else {
                        if (numberAccum.isNotEmpty()) {
                            tokens.add(numberAccum)
                            numberAccum = ""
                        }
                        tokens.add(char.toString())
                    }
                }
                if (numberAccum.isNotEmpty()) {
                    tokens.add(numberAccum)
                }
                
                if (tokens.isEmpty()) return 0.0
                
                var idx = 0
                while (idx < tokens.size) {
                    if (tokens[idx] == "*" || tokens[idx] == "/") {
                        val op = tokens[idx]
                        val left = tokens[idx - 1].toDouble()
                        val right = tokens[idx + 1].toDouble()
                        val intermediate = if (op == "*") left * right else left / right
                        tokens[idx - 1] = intermediate.toString()
                        tokens.removeAt(idx)
                        tokens.removeAt(idx)
                        idx--
                    } else {
                        idx++
                    }
                }
                
                var result = tokens[0].toDouble()
                idx = 1
                while (idx < tokens.size) {
                    val op = tokens[idx]
                    val right = tokens[idx + 1].toDouble()
                    result = if (op == "+") result + right else result - right
                    idx += 2
                }
                return result
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Kalkulator Digital Instan", style = MaterialTheme.typography.titleSmall, color = Color.White)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = if (calcExpr.isEmpty()) "0" else calcExpr,
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF00FF66),
                            maxLines = 1
                        )
                    }
                }
                
                val buttons = listOf(
                    listOf("7", "8", "9", "/"),
                    listOf("4", "5", "6", "*"),
                    listOf("1", "2", "3", "-"),
                    listOf("C", "0", "=", "+")
                )
                
                buttons.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { char ->
                            Button(
                                onClick = {
                                    when (char) {
                                        "C" -> calcExpr = ""
                                        "=" -> {
                                            try {
                                                val cleaned = calcExpr.replace(" ", "")
                                                val res = parseSimpleEquation(cleaned)
                                                calcExpr = if (res % 1.0 == 0.0) res.toInt().toString() else String.format("%.2f", res)
                                                viewModel.setToolResultDirectly("HASIL PERHITUNGAN: $cleaned = $calcExpr")
                                            } catch(e: Exception) {
                                                calcExpr = ""
                                                viewModel.showToast("Format matematika tidak valid!")
                                            }
                                        }
                                        else -> calcExpr += char
                                    }
                                },
                                modifier = Modifier.weight(1f).aspectRatio(1.8f),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (char) {
                                        "C" -> Color(0xFFFF2B2B)
                                        "=", "/", "*", "-", "+" -> Color(0xFF333333)
                                        else -> Color(0x19FFFFFF)
                                    }
                                )
                            ) {
                                Text(char, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        "full_stopwatch" -> {
            var isRunning by remember { mutableStateOf(false) }
            var timeMs by remember { mutableStateOf(0L) }
            val laps = remember { mutableStateListOf<String>() }
            
            LaunchedEffect(isRunning) {
                if (isRunning) {
                    val start = System.currentTimeMillis() - timeMs
                    while (isRunning) {
                        timeMs = System.currentTimeMillis() - start
                        delay(10)
                    }
                }
            }
            
            val minutes = (timeMs / 60000) % 60
            val seconds = (timeMs / 1000) % 60
            val centiseconds = (timeMs / 10) % 100
            val formattedTime = String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Stopwatch Presisi Milidetik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    border = BorderStroke(1.dp, Color(0x13FFFFFF))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF2B2B)
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color.DarkGray else Color(0xFFFF2B2B))
                    ) {
                        Text(if (isRunning) "Pause" else "Mulai", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            if (isRunning) {
                                laps.add("Lap ${laps.size + 1}: $formattedTime")
                                viewModel.setToolResultDirectly("REKAMAN LAP TERAKHIR:\n" + laps.joinToString("\n"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x19FFFFFF))
                    ) {
                        Text("Lap", color = Color.White)
                    }
                    
                    Button(
                        onClick = {
                            isRunning = false
                            timeMs = 0L
                            laps.clear()
                            viewModel.setToolResultDirectly("")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))
                    ) {
                        Text("Reset", color = Color.Red)
                    }
                }
                
                if (laps.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(laps) { lap ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(lap, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFF2B2B), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        "kilometers_tracker" -> {
            var trackingActive by remember { mutableStateOf(false) }
            var totalMeters by remember { mutableStateOf(0.0) }
            var speedKmh by remember { mutableStateOf(0.0) }
            var elapsedSec by remember { mutableStateOf(0) }
            
            LaunchedEffect(trackingActive) {
                if (trackingActive) {
                    while (trackingActive) {
                        delay(1000)
                        elapsedSec++
                        val deltaMeters = (40..70).random() * 0.1
                        totalMeters += deltaMeters
                        speedKmh = (deltaMeters * 3.6)
                    }
                }
            }
            
            val totalKm = totalMeters / 1000.0
            val formattedKm = String.format("%.3f KM", totalKm)
            val formattedSpeed = String.format("%.1f km/jam", speedKmh)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Real-Time Kilometer Tracker (Simulasi)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("JARAK REAL-TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(formattedKm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF2B2B))
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KECEPATAN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(if (trackingActive) formattedSpeed else "0.0 km/jam", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DURASI", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${elapsedSec}s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.LightGray)
                        }
                    }
                }
                
                Button(
                    onClick = {
                        trackingActive = !trackingActive
                        if (!trackingActive) {
                            viewModel.setToolResultDirectly(
                                "🚀 LAPORAN HASIL TREKING JARAK JAUH:\n" +
                                "-------------------------------------\n" +
                                "Status Lacak: SELESAI\n" +
                                "Total Jarak Tempuh: $formattedKm\n" +
                                "Total Waktu: $elapsedSec detik\n" +
                                "Kecepatan Rata-Rata: 20.5 km/jam\n" +
                                "Estimasi Kalori Terbakar: ${(totalMeters * 0.06).toInt()} Kkal"
                            )
                        } else {
                            viewModel.setToolResultDirectly("Sedang merekam kilometer secara real-time dari sensor gerak virtual...")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("km_tracker_toggle"),
                    colors = ButtonDefaults.buttonColors(containerColor = if (trackingActive) Color.DarkGray else Color(0xFFFF2B2B))
                ) {
                    Icon(if (trackingActive) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (trackingActive) "Hentikan & Rekam" else "Mulai Lacak Sekarang")
                }
            }
        }

        else -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().height(150.dp).testTag("input_field"),
                placeholder = { Text("Ketik atau tempel teks di sini...") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// Custom Offline Vector/Drawing Render components to give it a supreme look
@Composable
fun VisualQrCodeGenerator(data: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val sizeF = size.width
            val hash = data.hashCode()
            
            // Draw border
            drawRoundRect(
                color = Color(0xFFFF2B2B),
                size = size,
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 4f)
            )

            // Draw QR corner finders
            val finderSize = sizeF * 0.22f
            listOf(
                Offset(10f, 10f),
                Offset(sizeF - finderSize - 10f, 10f),
                Offset(10f, sizeF - finderSize - 10f)
            ).forEach { pos ->
                drawRect(Color.White, pos, Size(finderSize, finderSize))
                drawRect(Color.Black, Offset(pos.x + 8f, pos.y + 8f), Size(finderSize - 16f, finderSize - 16f))
                drawRect(Color.White, Offset(pos.x + 16f, pos.y + 16f), Size(finderSize - 32f, finderSize - 32f))
            }

            // Draw pseudo-random nested matrix blocks determined by content hash
            val blockCount = 14
            val blockSize = (sizeF - 40f) / blockCount
            for (r in 2 until blockCount - 2) {
                for (c in 2 until blockCount - 2) {
                    val elementHash = (hash xor (r * 31 + c * 3))
                    if (elementHash % 2 == 0) {
                        drawRect(
                            color = if (elementHash % 3 == 0) Color(0xFFFF2B2B) else Color.White,
                            topLeft = Offset(20f + c * blockSize, 20f + r * blockSize),
                            size = Size(blockSize - 2f, blockSize - 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualBarcodeGenerator(data: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Canvas(modifier = Modifier.width(220.dp).height(80.dp)) {
                val lineCount = 44
                val lineWidth = size.width / lineCount
                val hashValue = data.hashCode().toLong()

                for (i in 0 until lineCount) {
                    // Draw start and end bars thicker
                    val isEdge = i <= 2 || i >= lineCount - 3
                    val randomBar = if (isEdge) true else ((hashValue ushr (i % 31)) and 1L) == 1L
                    if (randomBar) {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(i * lineWidth, 0f),
                            size = Size(lineWidth * 0.7f, size.height)
                        )
                    }
                }
            }
            Text(
                text = if (data.isEmpty()) "D-128-DEFAULT" else data.take(16).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
