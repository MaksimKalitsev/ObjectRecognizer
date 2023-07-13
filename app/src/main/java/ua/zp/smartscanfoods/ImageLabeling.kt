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
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

@Composable
fun ResponseViewImageLabeling(labels: List<String>) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Detected objects:")
        for (label in labels) {
            Text(text = label)
        }
    }
}

fun imageLabeling(
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

    val labeler = ImageLabeling.getClient(com.google.mlkit.vision.label.defaults.ImageLabelerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromFilePath(context, uri)

    labeler.process(image)
        .addOnSuccessListener { labels ->
            val labelList = labels.map {label ->
                "Text: ${label.text}, Index: ${label.index}, Confidence: ${label.confidence}"
            }
            onSuccess(labelList)
        }
        .addOnFailureListener { e ->
            Log.e("ML Kit", "Object Detection Failed", e)
            onFailure(e)
        }
}
