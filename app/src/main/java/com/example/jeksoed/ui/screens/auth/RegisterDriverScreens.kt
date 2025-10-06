package com.example.jeksoed.ui.screens.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// --- LANGKAH 1: DATA DASAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDriverStep1Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Hai, selamat datang!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Daftar dulu, yuk!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = viewModel.name, onValueChange = { viewModel.name = it }, label = { Text("Nama") }, placeholder = { Text("Masukan nama kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = viewModel.nim, onValueChange = { viewModel.nim = it }, label = { Text("NIM") }, placeholder = { Text("Masukan NIM kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = { Text("Email") }, placeholder = { Text("Masukan email kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = viewModel.phone, onValueChange = { viewModel.phone = it }, label = { Text("Nomor Hp") }, placeholder = { Text("Masukan nomor hp kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = { Text("Password") }, placeholder = { Text("Masukan password kamu") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Lanjut",
                onClick = { navController.navigate(Screen.RegisterDriverStep2.route) },
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))

            // Syarat & Ketentuan
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
                        .firstOrNull()?.let { navController.navigate(Screen.Tnc.route) }
                }
            )
        }
    }
}

// --- LANGKAH 2: UNGGAH DOKUMEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDriverStep2Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    val context = LocalContext.current

    // Launcher untuk memilih file gambar
    val ktmLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.ktmUri = uri }
    val stnkLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.stnkUri = uri }
    val motorLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.motorUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

            FileUploadField(label = "KTM", fileName = viewModel.getFileName(viewModel.ktmUri, context)) { ktmLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(16.dp))
            FileUploadField(label = "STNK", fileName = viewModel.getFileName(viewModel.stnkUri, context)) { stnkLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(16.dp))
            FileUploadField(label = "Motor (Plat terlihat)", fileName = viewModel.getFileName(viewModel.motorUri, context)) { motorLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = "Lanjut",
                onClick = { navController.navigate(Screen.RegisterDriverStep3.route) },
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            // Syarat & Ketentuan
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
                        .firstOrNull()?.let { navController.navigate(Screen.Tnc.route) }
                }
            )
        }
    }
}

// --- LANGKAH 3: KONFIRMASI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDriverStep3Screen(navController: NavController, viewModel: RegisterDriverViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                modifier = Modifier.clickable { viewModel.agreedToTerms = !viewModel.agreedToTerms }
            ) {
                Checkbox(checked = viewModel.agreedToTerms, onCheckedChange = { viewModel.agreedToTerms = it })
                Text("Saya setuju dengan persyaratan yang di berikan")
            }
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = "Daftar",
                onClick = {
                    scope.launch {
                        viewModel.registerDriver(
                            onSuccess = {
                                Toast.makeText(context, "Registrasi Driver Berhasil!", Toast.LENGTH_LONG).show()
                                // Navigasi ke CTA dan hapus semua backstack
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
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black,
                isEnabled = viewModel.agreedToTerms
            )
            Spacer(modifier = Modifier.weight(1f))

            // Syarat & Ketentuan
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
                        .firstOrNull()?.let { navController.navigate(Screen.Tnc.route) }
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep1ScreenPreview() {
    JekSoedTheme {
        // Kita menggunakan NavController palsu untuk preview
        RegisterDriverStep1Screen(navController = rememberNavController(), viewModel = RegisterDriverViewModel())
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep2ScreenPreview() {
    JekSoedTheme {
        RegisterDriverStep2Screen(navController = rememberNavController(), viewModel = RegisterDriverViewModel())
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterDriverStep3ScreenPreview() {
    JekSoedTheme {
        RegisterDriverStep3Screen(navController = rememberNavController(), viewModel = RegisterDriverViewModel())
    }
}