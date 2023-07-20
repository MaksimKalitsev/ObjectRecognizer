package ua.zp.smartscanfoods

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageClassifierViewModel(application: Application) : AndroidViewModel(application) {

    private val imageClassifier: ImageClassifier = ImageClassifier(application)

    private val _detections = MutableLiveData<Bitmap>()
    val detections: LiveData<Bitmap> get() = _detections

    fun runObjectDetection(currentPhotoPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = getCapturedImage(currentPhotoPath)
            val resultBitmap = imageClassifier.runObjectDetection(bitmap)
            _detections.postValue(resultBitmap)
        }
    }

    private fun getCapturedImage(currentPhotoPath: String): Bitmap {
        return imageClassifier.getCapturedImage(currentPhotoPath)
    }

    override fun onCleared() {
        imageClassifier.onDestroy()
        super.onCleared()

    }
}