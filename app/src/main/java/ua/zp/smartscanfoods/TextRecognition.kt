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
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun ResponseViewTextRecognition(text: String) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Recognized Text:")
        Text(text = text)
    }
}

fun textRecognition(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            Log.i("MLKit", "Text Recognition succeeded.")
            val resultText = visionText.text
            onSuccess(resultText)
            for (block in visionText.textBlocks) {
                val blockText = block.text
                val blockCornerPoints = block.cornerPoints
                Log.i("MLKit", "Block text: $blockText")
                Log.i("MLKit", "Block corner points: $blockCornerPoints")
            }
        }
        .addOnFailureListener { e ->
            Log.e("MLKit", "Text Recognition failed.", e)
            onFailure(e)
        }
}