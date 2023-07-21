package ua.zp.smartscanfoods.di

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideObjectDetector(
        context: Context,
        options: ObjectDetector.ObjectDetectorOptions
    ): ObjectDetector {
        return ObjectDetector.createFromFileAndOptions(
            context,
            "Test_TensorFlow_Lite_Model.tflite",
            options
        )
    }

    @Provides
    fun provideObjectDetectorOptions(): ObjectDetector.ObjectDetectorOptions {
        return ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .build()
    }

    @Provides
    @Singleton
    fun provideTextToSpeech(context: Context): TextToSpeech {
        return TextToSpeech(context, null)
    }

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideExecutorService(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
}