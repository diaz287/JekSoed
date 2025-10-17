package com.example.jeksoed.ui.screens.auth

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import kotlinx.coroutines.launch

// =================================================================================
// LANGKAH 1: DATA DASAR
// =================================================================================

// --- Smart Composable (Dengan ViewModel) ---
@Composable
fun RegisterDriverStep1Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    RegisterDriverStep1ScreenUI(
        name = viewModel.name,
        nim = viewModel.nim,
        email = viewModel.email,
        phone = viewModel.phone,
        licensePlate = viewModel.licensePlate,
        password = viewModel.password,
        onNameChange = { viewModel.name = it },
        onNimChange = { viewModel.nim = it },
        onEmailChange = { viewModel.email = it },
        onPhoneChange = { viewModel.phone = it },
        onLicensePlateChange = { viewModel.licensePlate = it },
        onPasswordChange = { viewModel.password = it },
        onNextClick = { navController.navigate(Screen.RegisterDriverStep2.route) },
        onBackClick = { navController.popBackStack() },
        onTncClick = { navController.navigate(Screen.Tnc.route) }
    )
}

// --- Dumb Composable (Hanya Tampilan) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterDriverStep1ScreenUI(
    name: String, nim: String, email: String, phone: String, licensePlate: String, password: String,
    onNameChange: (String) -> Unit,
    onNimChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onLicensePlateChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onTncClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Hai, selamat datang!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Daftar dulu, yuk!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nama") }, placeholder = { Text("Masukan nama kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = nim, onValueChange = onNimChange, label = { Text("NIM") }, placeholder = { Text("Masukan NIM kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, placeholder = { Text("Masukan email kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Nomor Hp") }, placeholder = { Text("Masukan nomor hp kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = licensePlate, onValueChange = onLicensePlateChange, label = { Text("Plat Nomor") }, placeholder = { Text("Contoh: R 1234 AB") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Password") }, placeholder = { Text("Masukan password kamu") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(text = "Lanjut", onClick = onNextClick, containerColor = Color(0xFFFFC107), contentColor = Color.Black)
            Spacer(modifier = Modifier.weight(1f))

            val tncAnnotatedString = buildAnnotatedString {
                append("Aku setuju sama ")
                pushStringAnnotation(tag = "TNC", annotation = "tnc_link")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Syarat & Ketentuan Privasi JEKSOED.")
                }
                pop()
            }
            ClickableText(
                text = tncAnnotatedString,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center, color = Color.Gray),
                onClick = { offset ->
                    tncAnnotatedString.getStringAnnotations(tag = "TNC", start = offset, end = offset)
                        .firstOrNull()?.let { onTncClick() }
                }
            )
        }
    }
}

// =================================================================================
// LANGKAH 2: UNGGAH DOKUMEN
// =================================================================================

// --- Smart Composable (Dengan ViewModel) ---
@Composable
fun RegisterDriverStep2Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    val context = LocalContext.current
    val ktmLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.ktmUri = uri }
    val stnkLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.stnkUri = uri }
    val motorLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.motorUri = uri }

    RegisterDriverStep2ScreenUI(
        ktmFileName = viewModel.getFileName(viewModel.ktmUri, context),
        stnkFileName = viewModel.getFileName(viewModel.stnkUri, context),
        motorFileName = viewModel.getFileName(viewModel.motorUri, context),
        onKtmClick = { ktmLauncher.launch("image/*") },
        onStnkClick = { stnkLauncher.launch("image/*") },
        onMotorClick = { motorLauncher.launch("image/*") },
        onNextClick = { navController.navigate(Screen.RegisterDriverStep3.route) },
        onBackClick = { navController.popBackStack() },
        onTncClick = { navController.navigate(Screen.Tnc.route) }
    )
}

// --- Dumb Composable (Hanya Tampilan) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterDriverStep2ScreenUI(
    ktmFileName: String, stnkFileName: String, motorFileName: String,
    onKtmClick: () -> Unit,
    onStnkClick: () -> Unit,
    onMotorClick: () -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onTncClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(text = "Lengkapin dulu, Bung!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            FileUploadField(label = "KTM", fileName = ktmFileName, onClick = onKtmClick)
            Spacer(modifier = Modifier.height(16.dp))
            FileUploadField(label = "STNK", fileName = stnkFileName, onClick = onStnkClick)
            Spacer(modifier = Modifier.height(16.dp))
            FileUploadField(label = "Motor (Plat terlihat)", fileName = motorFileName, onClick = onMotorClick)
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Lanjut", onClick = onNextClick, containerColor = Color(0xFFFFC107), contentColor = Color.Black)

            Spacer(modifier = Modifier.weight(1f))

            val tncAnnotatedString = buildAnnotatedString {
                append("Aku setuju sama ")
                pushStringAnnotation(tag = "TNC", annotation = "tnc_link")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Syarat & Ketentuan Privasi JEKSOED.")
                }
                pop()
            }
            ClickableText(
                text = tncAnnotatedString,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center, color = Color.Gray),
                onClick = { offset ->
                    tncAnnotatedString.getStringAnnotations(tag = "TNC", start = offset, end = offset)
                        .firstOrNull()?.let { onTncClick() }
                }
            )
        }
    }
}

// =================================================================================
// LANGKAH 3: KONFIRMASI
// =================================================================================

// --- Smart Composable (Dengan ViewModel) ---
@Composable
fun RegisterDriverStep3Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    RegisterDriverStep3ScreenUI(
        agreedToTerms = viewModel.agreedToTerms,
        isLoading = viewModel.isLoading,
        onAgreementChange = { viewModel.agreedToTerms = it },
        onRegisterClick = {
            scope.launch {
                viewModel.registerDriver(
                    onSuccess = {
                        Toast.makeText(context, "Registrasi Driver Berhasil!", Toast.LENGTH_LONG).show()
                        navController.navigate(Screen.Cta.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                )
            }
        },
        onBackClick = { navController.popBackStack() },
        onTncClick = { navController.navigate(Screen.Tnc.route) }
    )
}

// --- Dumb Composable (Hanya Tampilan) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterDriverStep3ScreenUI(
    agreedToTerms: Boolean,
    isLoading: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onTncClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(text = "Terakhir banget!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Dengan mendaftar menjadi driver di JEKSOED, berarti kamu setuju untuk:")
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Memberikan data diri yang benar, valid, dan sesuai identitas mahasiswa Unsoed.")
                Text("• Menggunakan aplikasi sesuai aturan dan etika yang berlaku.")
                Text("• Menjaga sikap profesional serta menghormati penumpang.")
                Text("• Tidak menyalahgunakan akun untuk tindakan ilegal atau merugikan pihak lain.")
                Text("• Mematuhi Syarat, Ketentuan, dan Kebijakan Privasi JEKSOED.")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAgreementChange(!agreedToTerms) }
            ) {
                Checkbox(checked = agreedToTerms, onCheckedChange = onAgreementChange)
                Text("Saya setuju dengan persyaratan yang di berikan")
            }
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                PrimaryButton(
                    text = "Daftar",
                    onClick = onRegisterClick,
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black,
                    isEnabled = agreedToTerms
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val tncAnnotatedString = buildAnnotatedString {
                append("Aku setuju sama ")
                pushStringAnnotation(tag = "TNC", annotation = "tnc_link")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Syarat & Ketentuan Privasi JEKSOED.")
                }
                pop()
            }
            ClickableText(
                text = tncAnnotatedString,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center, color = Color.Gray),
                onClick = { offset ->
                    tncAnnotatedString.getStringAnnotations(tag = "TNC", start = offset, end = offset)
                        .firstOrNull()?.let { onTncClick() }
                }
            )
        }
    }
}

// Composable bantuan untuk field upload file
@Composable
private fun FileUploadField(label: String, fileName: String, onClick: () -> Unit) {
    Column {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(32.dp))
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp))
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Pilih File", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pilih File", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fileName,
                color = if (fileName.startsWith("Belum")) Color.Gray else Color.Black,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// =================================================================================
// PREVIEWS (Sekarang memanggil versi UI)
// =================================================================================

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep1ScreenPreview() {
    JekSoedTheme {
        RegisterDriverStep1ScreenUI(
            name = "", nim = "", email = "", phone = "", licensePlate = "", password = "",
            onNameChange = {}, onNimChange = {}, onEmailChange = {}, onPhoneChange = {}, onLicensePlateChange = {}, onPasswordChange = {},
            onNextClick = {}, onBackClick = {}, onTncClick = {}
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep2ScreenPreview() {
    JekSoedTheme {
        RegisterDriverStep2ScreenUI(
            ktmFileName = "ktm_saya.jpg", stnkFileName = "stnk_motor.png", motorFileName = "Belum ada foto yang dipilih",
            onKtmClick = {}, onStnkClick = {}, onMotorClick = {},
            onNextClick = {}, onBackClick = {}, onTncClick = {}
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep3ScreenPreview() {
    JekSoedTheme {
        RegisterDriverStep3ScreenUI(
            agreedToTerms = true, isLoading = false,
            onAgreementChange = {}, onRegisterClick = {}, onBackClick = {}, onTncClick = {}
        )
    }
}