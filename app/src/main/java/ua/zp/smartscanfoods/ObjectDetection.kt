package ua.zp.smartscanfoods

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.IOException

@Composable
fun ResponseViewObjectDetection(objects: List<String>) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Detected objects:")
        for (obj in objects) {
            Text(text = obj)
        }
    }
}

fun objectDetection(
    context: Context,
    uri: Uri,
    onSuccess: (List<String>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val objectDetectorOptions = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectDetector = ObjectDetection.getClient(objectDetectorOptions)
    val image = InputImage.fromFilePath(context, uri)

    objectDetector.process(image)
        .addOnSuccessListener { objects ->
            val objectList = mutableListOf<String>()
            for (obj in objects) {
                val boundingBox = obj.boundingBox
                val labels = obj.labels.map { label ->
                    "Text: ${label.text}, Index: ${label.index}, Confidence: ${label.confidence}"
                }
                val info = """
                BoundingBox: $boundingBox,
                Lables: $labels

            """.trimIndent()
                objectList.add(info)
            }
            onSuccess(objectList)
        }
        .addOnFailureListener { e ->
            Log.e("ML Kit", "Object Detection Failed", e)
            onFailure(e)
        }
}
