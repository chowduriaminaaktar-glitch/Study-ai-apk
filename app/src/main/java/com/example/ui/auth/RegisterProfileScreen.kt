package com.example.ui.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AuthState
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProfileScreen(
    needsReg: AuthState.NeedsRegistration,
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var displayName by remember { mutableStateOf(needsReg.displayName) }
    var photoUriString by remember { mutableStateOf<String?>(needsReg.photoUrl) }
    var dateOfBirth by remember { mutableStateOf("") }
    var localValidationErr by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            localValidationErr = null
            val validation = validateAndEncodeImage(context, uri)
            if (validation.isSuccess) {
                photoUriString = validation.getOrNull()
            } else {
                localValidationErr = validation.exceptionOrNull()?.message
            }
        }
    }

    // DatePicker State (Restricted to past dates up to today)
    val maxCalendar = Calendar.getInstance()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() - 567648000000L // default ~18 years ago
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val formatted = sdf.format(Date(selectedMillis))
                            dateOfBirth = formatted
                            localValidationErr = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select Date", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Complete Your Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Set up your student profile to personalize your Study AI experience.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Profile Picture Uploader & Preview
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("btn_upload_photo")
            ) {
                if (!photoUriString.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUriString,
                        contentDescription = "Profile Picture Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF818CF8), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .border(2.dp, Color(0xFF64748B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default avatar",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // Camera overlay button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF6366F1),
                    modifier = Modifier
                        .size(36.dp)
                        .border(2.dp, Color(0xFF0F172A), CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!photoUriString.isNullOrBlank()) "Tap to replace photo" else "Upload photo (JPG, PNG, WEBP < 5MB)",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error display
            val activeError = localValidationErr ?: errorMessage
            AnimatedVisibility(visible = activeError != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeError ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                localValidationErr = null
                                viewModel.dismissError()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            // Google Email (Read-only)
            OutlinedTextField(
                value = needsReg.email,
                onValueChange = {},
                readOnly = true,
                label = { Text("Google Email (Verified)") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981))
                },
                trailingIcon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981))
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_email"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFCBD5E1),
                    unfocusedTextColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF334155),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Full Name (Pre-filled, Editable)
            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    displayName = it
                    localValidationErr = null
                },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Maya Chen") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF818CF8))
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_display_name"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedLabelColor = Color(0xFF818CF8),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Date of Birth (Mandatory & Functional Date Picker)
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth (YYYY-MM-DD) *") },
                placeholder = { Text("Tap calendar to select date") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFF59E0B))
                },
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.testTag("btn_select_dob")
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = Color(0xFFF59E0B))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .testTag("input_dob"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedLabelColor = Color(0xFFF59E0B),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    if (displayName.trim().isEmpty()) {
                        localValidationErr = "Please enter your full name."
                        return@Button
                    }
                    if (dateOfBirth.trim().isEmpty()) {
                        localValidationErr = "Date of birth is mandatory. Please select your birth date."
                        return@Button
                    }

                    viewModel.completeRegistration(
                        uid = needsReg.uid,
                        displayName = displayName.trim(),
                        email = needsReg.email,
                        dateOfBirth = dateOfBirth.trim(),
                        photoURL = photoUriString,
                        onSuccess = onRegistrationSuccess
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_create_account")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Creating Account...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Text("Create My STUDY AI Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Validates image size (<5MB) and MIME type (JPEG, PNG, WEBP), then compresses into a data URI.
 */
fun validateAndEncodeImage(context: Context, uri: Uri): Result<String> {
    return try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val validMimes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")

        if (!validMimes.any { mimeType.equals(it, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Unsupported file type ($mimeType). Only JPG, PNG, and WEBP are allowed."))
        }

        // Check file size limit: 5 MB = 5 * 1024 * 1024 bytes
        val fileDescriptor = resolver.openFileDescriptor(uri, "r")
        val sizeBytes = fileDescriptor?.statSize ?: 0L
        fileDescriptor?.close()

        if (sizeBytes > 5 * 1024 * 1024) {
            return Result.failure(IllegalArgumentException("Image exceeds the 5 MB limit (${sizeBytes / (1024 * 1024)} MB). Please select a smaller photo."))
        }

        // Decode and scale down to 320x320 for optimal Firestore and UI rendering
        val inputStream = resolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) {
            return Result.failure(IllegalArgumentException("Unable to decode the selected image file."))
        }

        val scaled = Bitmap.createScaledBitmap(
            originalBitmap,
            originalBitmap.width.coerceAtMost(320),
            originalBitmap.height.coerceAtMost(320),
            true
        )

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        val dataUri = "data:image/jpeg;base64,$base64"

        Result.success(dataUri)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
