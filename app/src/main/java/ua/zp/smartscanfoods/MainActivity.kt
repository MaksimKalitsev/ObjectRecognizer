package ua.zp.smartscanfoods

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ua.zp.smartscanfoods.ui.theme.SmartScanFoodsTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoUri: Uri

    private val imageClassifierViewModel: ImageClassifierViewModel by viewModels()

    private var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)

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

        observeDetectionResults()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (shouldShowPhoto.value) {
                    shouldShowCamera.value = true
                    shouldShowPhoto.value = false
                    imageBitmap.value = null
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
                    } else if (shouldShowPhoto.value && imageBitmap.value != null) {
                        Image(
                            bitmap = imageBitmap.value!!,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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

        uri.path?.let {
            imageClassifierViewModel.runObjectDetection(uri.path!!)
        }
    }

    private fun observeDetectionResults() {
        imageClassifierViewModel.detections.observe(this) { resultBitmap ->
            imageBitmap.value = resultBitmap.asImageBitmap()

        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}



