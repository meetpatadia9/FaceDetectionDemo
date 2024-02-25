package com.codewithzebru.facedetectiondemo

import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.codewithzebru.facedetectiondemo.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isTextDone = false
    private var isFaceDone = false

    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private var currentLensFacing = CameraSelector.LENS_FACING_FRONT

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    private var remainingTimeSeconds = 30
    private var mediaRecorder: MediaRecorder? = null
    private val stopRecordingHandler = Handler(Looper.getMainLooper())

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(currentLensFacing)
            .build()

        cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyserForTextRecognition()
            bindInputAnalyserForFaceDetection()
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        updateTimerText()

        binding.videoControlButton.setOnClickListener {
            if (!isRecording && isFaceDone) {
                startRecording()
            } else if (!isFaceDone) {
                Toast.makeText(this, "Face is not visible!", Toast.LENGTH_SHORT).show()
            } else if (isRecording) {
                stopRecording()

                /*if (isTextDone) {
                    stopRecording()
                } else {
                    Toast.makeText(this, "Weight verification is due!", Toast.LENGTH_SHORT).show()
                }*/
            }
        }

        binding.flipCamera.setOnClickListener { flipCamera() }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()

        cameraPreview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    private fun bindInputAnalyserForTextRecognition() {
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) {
            processImageProxyForTextRecognition(it)
        }

        processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxyForTextRecognition(imageProxy: ImageProxy) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(inputImage)
            .addOnSuccessListener {
                Log.d("processImageProxyForTextRecognition", it.text)
                if (it.text.contains("54")) {
                    Toast.makeText(this, "Weight is verified!", Toast.LENGTH_SHORT).show()
                    isTextDone = true
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun bindInputAnalyserForFaceDetection() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxyForFaceDetection(detector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.printStackTrace()
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxyForFaceDetection(detector: FaceDetector, imageProxy: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage).addOnSuccessListener { faces ->
            binding.graphicOverlay.clear()
            faces.forEach { face ->
                val faceBox = FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                binding.graphicOverlay.add(faceBox)

                isFaceDone = true
                binding.videoControlButton.apply {
                    alpha = 1f
                    isEnabled = true
                }
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    private fun startRecording() {
        isRecording = true

        binding.videoControlButton.setImageResource(R.drawable.round_stop_24)

        val stopRecordingRunnable = Runnable {
            stopRecording()
        }

        try {
            val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val outputFile = File(outputDir, "verification_${System.currentTimeMillis()}.mp4")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile.absolutePath)
                setVideoEncodingBitRate(1000000)
                setVideoFrameRate(30)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }

            // Schedule a task to stop recording after 30 seconds
            stopRecordingHandler.postDelayed(stopRecordingRunnable, 30000)

            // Start countdown timer
            val timer = object : CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    remainingTimeSeconds--
                    updateTimerText()
                }

                override fun onFinish() {
                    stopRecording()
                }
            }
            timer.start()
        } catch (e: Exception) {
            Log.e("startRecording: Failed", e.message.toString())
        }
    }

    private fun updateTimerText() {
        binding.txtTimer.text = remainingTimeSeconds.toString()
    }

    private fun stopRecording() {
        binding.videoControlButton.setImageResource(0)

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("stopRecording: ", e.message.toString())
        }

        mediaRecorder = null
        isRecording = false
    }

    private fun flipCamera() {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(currentLensFacing)
            .build()

        processCameraProvider.unbindAll()
        bindCameraPreview()
        bindInputAnalyserForTextRecognition()
        bindInputAnalyserForFaceDetection()
    }

}