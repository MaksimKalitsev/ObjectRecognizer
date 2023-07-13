package ua.zp.smartscanfoods

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import ua.zp.smartscanfoods.ui.theme.SmartScanFoodsTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoUri: Uri

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowRecognizedText: MutableState<Boolean> = mutableStateOf(false)

    private var recognizedText: MutableState<String> = mutableStateOf("")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("", "Permission granted")
            shouldShowCamera.value = true
        } else {
            Log.i("", "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (shouldShowPhoto.value) {
                    shouldShowCamera.value = true
                    shouldShowPhoto.value = false
                    recognizedText.value = ""
                } else {
                    finish()
                }
            }
        })
        setContent {
            SmartScanFoodsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (shouldShowCamera.value) {
                        CameraView(
                            outputDirectory = outputDirectory,
                            executor = cameraExecutor,
                            onImageCaptured = ::handleImageCapture,
                            onError = { Log.e("", "View error:", it) }
                        )
                    }
                    if (shouldShowPhoto.value) {
                        Image(
                            painter = rememberImagePainter(photoUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (shouldShowRecognizedText.value) {
                        LaunchedEffect(Unit){
                            shouldShowRecognizedText.value = true
                            textRecognition(
                                context = this@MainActivity,
                                uri = photoUri,
                                onSuccess = {text-> recognizedText.value = text
                                shouldShowRecognizedText.value = false},
                                onFailure = {shouldShowRecognizedText.value = false}
                            )
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                            CircularProgressIndicator()
                        }
                    }
                    ResponseView(text = recognizedText.value)
                }
            }
        }
        requestCameraPermission()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("", "Permission previously granted ")
                shouldShowCamera.value = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("", "Image captured: $uri")
        shouldShowCamera.value = false

        photoUri = uri
        shouldShowPhoto.value = true
        shouldShowRecognizedText.value = true

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}


