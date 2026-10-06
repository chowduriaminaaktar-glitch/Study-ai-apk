package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class AttachedFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri? = null,
    val name: String,
    val mimeType: String,
    val sizeText: String = "",
    val bitmap: Bitmap? = null,
    val isVideo: Boolean = false,
    val isDocument: Boolean = false
)

fun queryFileDetails(context: Context, uri: Uri): Pair<String, String> {
    var name = "Document"
    var sizeStr = ""
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex != -1) {
                    val size = cursor.getLong(sizeIndex)
                    sizeStr = when {
                        size > 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", size / (1024f * 1024f))
                        size > 1024 -> "${size / 1024} KB"
                        else -> "$size B"
                    }
                }
            }
        }
    } catch (_: Exception) {}
    return Pair(name, sizeStr)
}

fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // downsample for memory efficiency
            }
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (_: Exception) {
        null
    }
}

fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val frame = retriever.getFrameAtTime(1_000_000) // 1 second
        retriever.release()
        frame
    } catch (_: Exception) {
        null
    }
}

fun serializeAttachedFiles(files: List<AttachedFile>): String {
    val array = JSONArray()
    for (f in files) {
        val obj = JSONObject()
        obj.put("id", f.id)
        obj.put("name", f.name)
        obj.put("mimeType", f.mimeType)
        obj.put("sizeText", f.sizeText)
        obj.put("isVideo", f.isVideo)
        obj.put("isDocument", f.isDocument)
        if (f.uri != null) {
            obj.put("uri", f.uri.toString())
        }
        if (f.bitmap != null) {
            try {
                val stream = ByteArrayOutputStream()
                val targetW = 120.coerceAtMost(f.bitmap.width)
                val targetH = (120 * f.bitmap.height / f.bitmap.width.coerceAtLeast(1)).coerceAtMost(120)
                val scaled = Bitmap.createScaledBitmap(f.bitmap, targetW, targetH, true)
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                obj.put("base64Thumb", Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP))
            } catch (_: Exception) {}
        }
        array.put(obj)
    }
    return array.toString()
}

fun deserializeAttachedFiles(json: String?): List<AttachedFile> {
    if (json.isNullOrBlank()) return emptyList()
    val list = mutableListOf<AttachedFile>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            var bmp: Bitmap? = null
            val base64 = obj.optString("base64Thumb", "")
            if (base64.isNotBlank()) {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            val uriStr = obj.optString("uri", "")
            list.add(
                AttachedFile(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    uri = if (uriStr.isNotBlank()) Uri.parse(uriStr) else null,
                    name = obj.optString("name", "Attachment"),
                    mimeType = obj.optString("mimeType", "application/octet-stream"),
                    sizeText = obj.optString("sizeText", ""),
                    bitmap = bmp,
                    isVideo = obj.optBoolean("isVideo", false),
                    isDocument = obj.optBoolean("isDocument", false)
                )
            )
        }
    } catch (_: Exception) {}
    return list
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentOptionsBottomSheet(
    onDismiss: () -> Unit,
    onPhotoTaken: (Bitmap) -> Unit,
    onVideoRecorded: (Uri?, Bitmap?) -> Unit = { _, _ -> },
    onMediaPicked: (Uri, Boolean) -> Unit, // uri, isVideo
    onDocumentPicked: (Uri) -> Unit
) {
    val context = LocalContext.current

    // 1. Camera Photo Launcher
    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onPhotoTaken(bitmap)
            onDismiss()
        }
    }

    val cameraPhotoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraPhotoLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Camera Video Launcher
    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val thumb = getVideoThumbnail(context, uri)
            onVideoRecorded(uri, thumb)
            onDismiss()
        } else {
            val thumb = result.data?.extras?.get("data") as? Bitmap
            if (thumb != null) {
                onVideoRecorded(null, thumb)
                onDismiss()
            }
        }
    }

    val cameraVideoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val camGranted = permissions[Manifest.permission.CAMERA] == true
        if (camGranted) {
            try {
                recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
            } catch (e: Exception) {
                Toast.makeText(context, "No camera application found to record video", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to record video", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val isVideo = mime.startsWith("video")
            onMediaPicked(uri, isVideo)
            onDismiss()
        }
    }

    // 4. Video Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onMediaPicked(uri, true)
            onDismiss()
        }
    }

    // 5. Document Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onDocumentPicked(uri)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("attachment_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Upload to Study AI",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Click photos, record video, or upload files from your device",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options Row (ChatGPT style with 5 distinct options)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Camera Photo
                AttachmentOptionItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    subtitle = "Click Photo",
                    bgColor = Color(0xFF4F46E5),
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            cameraPhotoLauncher.launch(null)
                        } else {
                            cameraPhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    tag = "option_camera_photo"
                )

                // 2. Camera Video
                AttachmentOptionItem(
                    icon = Icons.Default.Videocam,
                    title = "Record",
                    subtitle = "Record Video",
                    bgColor = Color(0xFFEF4444),
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (hasCam && hasMic) {
                            try {
                                recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Unable to launch video recorder", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            cameraVideoPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                        }
                    },
                    tag = "option_camera_video"
                )

                // 3. Photos (Gallery)
                AttachmentOptionItem(
                    icon = Icons.Default.Image,
                    title = "Photos",
                    subtitle = "From Gallery",
                    bgColor = Color(0xFF0EA5E9),
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    tag = "option_gallery_photo"
                )

                // 4. Videos (Gallery)
                AttachmentOptionItem(
                    icon = Icons.Default.VideoLibrary,
                    title = "Videos",
                    subtitle = "Gallery Clips",
                    bgColor = Color(0xFF10B981),
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    tag = "option_gallery_video"
                )

                // 5. Files / Documents
                AttachmentOptionItem(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    title = "Files",
                    subtitle = "PDFs & Docs",
                    bgColor = Color(0xFFF59E0B),
                    onClick = {
                        documentPickerLauncher.launch("*/*")
                    },
                    tag = "option_files_doc"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AttachedFilesPreviewRow(
    attachedFiles: List<AttachedFile>,
    onRemove: (AttachedFile) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = attachedFiles.isNotEmpty(),
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachedFiles, key = { it.id }) { file ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.testTag("attached_item_${file.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (file.bitmap != null) {
                            Box {
                                Image(
                                    bitmap = file.bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                if (file.isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            file.isVideo -> Color(0xFF10B981).copy(alpha = 0.2f)
                                            file.isDocument -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        file.isVideo -> Icons.Default.Videocam
                                        file.isDocument -> Icons.Default.Description
                                        else -> Icons.Default.Image
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        file.isVideo -> Color(0xFF059669)
                                        file.isDocument -> Color(0xFFD97706)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.width(96.dp)) {
                            Text(
                                text = file.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    file.isVideo -> "Video Clip"
                                    file.sizeText.isNotBlank() -> file.sizeText
                                    file.isDocument -> "Document"
                                    else -> "Photo"
                                },
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(
                            onClick = { onRemove(file) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove file",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders attached media chips inside user chat bubbles (photos, videos, and documents).
 */
@Composable
fun ChatAttachmentsGrid(
    attachments: List<AttachedFile>,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return
    val context = LocalContext.current
    var previewPhoto by remember { mutableStateOf<Bitmap?>(null) }

    if (previewPhoto != null) {
        Dialog(onDismissRequest = { previewPhoto = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { previewPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Image(
                        bitmap = previewPhoto!!.asImageBitmap(),
                        contentDescription = "Expanded preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        attachments.forEach { file ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .clickable {
                        if (file.bitmap != null && !file.isVideo) {
                            previewPhoto = file.bitmap
                        } else if (file.uri != null) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(file.uri, file.mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Opening: ${file.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .testTag("chat_attachment_${file.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (file.bitmap != null) {
                        Box {
                            Image(
                                bitmap = file.bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            if (file.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = when {
                                file.isVideo -> Icons.Default.Videocam
                                file.isDocument -> Icons.Default.Description
                                else -> Icons.Default.AttachFile
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = file.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                file.isVideo -> "Video Clip"
                                file.sizeText.isNotBlank() -> file.sizeText
                                file.isDocument -> "Document"
                                else -> "Photo"
                            },
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
