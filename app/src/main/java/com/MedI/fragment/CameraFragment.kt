/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.MedI.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Camera
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

import com.MedI.OverlayView
import com.MedI.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import com.MedI.HandLandmarkerHelper
import java.util.LinkedList
import com.MedI.ObjectDetectorHelper
import com.MedI.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import org.tensorflow.lite.task.vision.detector.Detection
import java.nio.ByteBuffer

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener, ObjectDetectorHelper.DetectorListener{

    companion object {
        private const val TAG = "Hand Landmarker + Object Detection"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK //수정

    /** Blocking ML operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handLandmarkerHelper = HandLandmarkerHelper(
            context = requireContext(),
            runningMode = RunningMode.LIVE_STREAM,
            handLandmarkerHelperListener = this
        )

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()
                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            bitmapBuffer = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                        }
                        try {
                            processImage(image)
                        } catch (e: IllegalStateException) {
                            Log.e(TAG, "Image processing error: ${e.message}")
                        } finally {
                            image.close()
                        }
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)

        } catch (exc: Exception) {
            Toast.makeText(requireContext(),"bindCameraUseCases: Use case binding failed",Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImage(image: ImageProxy) {
        synchronized(this) {
            try {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(),"ProcessImage: called",Toast.LENGTH_SHORT).show()
                }
                val imageWidth = image.width
                val imageHeight = image.height

                if (!::bitmapBuffer.isInitialized || bitmapBuffer.width != imageWidth || bitmapBuffer.height != imageHeight) {
                    bitmapBuffer = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
                }
                val buffer = image.planes[0].buffer
                val pixels = ByteArray(buffer.remaining())
                buffer.get(pixels)
                bitmapBuffer.copyPixelsFromBuffer(ByteBuffer.wrap(pixels))
                val imageRotation = image.imageInfo.rotationDegrees

                objectDetectorHelper.detect(bitmapBuffer, imageRotation)
                buffer.rewind()

                handLandmarkerHelper.detectLiveStream(
                    imageProxy = image,
                    isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
                )

            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Image processing error: Buffer not large enough for pixels", e)

            } catch (e: Exception) {
                Log.e(TAG, "Image processing error: ${e.message}", e)

            } finally {
                try {
                    image.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to close ImageProxy: ${e.message}")
                }
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after hand have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: HandLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResultsHl(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )
                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }


    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    // ObjectDetector results
    override fun onResults(results: MutableList<Detection>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int) {
        activity?.runOnUiThread {
            fragmentCameraBinding.overlay.setResultsOd(results ?: LinkedList<Detection>(), imageHeight, imageWidth)
            fragmentCameraBinding.overlay.invalidate()
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }


}
