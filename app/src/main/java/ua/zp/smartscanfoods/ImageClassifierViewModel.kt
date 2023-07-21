package ua.zp.smartscanfoods

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ImageClassifierViewModel @Inject constructor(
    application: Application,
    private val imageClassifier: ImageClassifier
) : AndroidViewModel(application) {

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