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
package com.MedI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import android.graphics.Rect
import android.graphics.RectF
import java.util.LinkedList
import org.tensorflow.lite.task.vision.detector.Detection




class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var resultsHl: HandLandmarkerResult? = null
    private var resultsOd: List<Detection> = LinkedList<Detection>()

    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var tipPaint = Paint()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        resultsHl = null

        linePaint.reset()
        pointPaint.reset()
        tipPaint.reset()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        tipPaint.color = Color.RED
        tipPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        tipPaint.style = Paint.Style.FILL
        tipPaint.strokeWidth = 40f

        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        resultsHl?.let { handLandmarkerResult ->
            val paint = Paint().apply {
                color = Color.RED // 텍스트 색상
                textSize = 30f // 텍스트 크기
                strokeWidth = 8f // 선의 두께
            }
            for (landmark in handLandmarkerResult.landmarks()) {
                for ((idx,normalizedLandmark) in landmark.withIndex()) {
                    val x = normalizedLandmark.x() * imageWidth * scaleFactor
                    val y = normalizedLandmark.y() * imageHeight * scaleFactor

                    if (idx == HandLandmark.INDEX_FINGER_TIP) {
                        val coordinateText = "(${String.format("%.2f", x)}, ${String.format("%.2f", y)})"
                        canvas.drawText(coordinateText, x, y+40f, paint)
                        canvas.drawPoint(x,y,tipPaint)
                    } else {
                        canvas.drawPoint(x,y,pointPaint)
                    }
                }

                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        landmark.get(it!!.start())
                            .x() * imageWidth * scaleFactor,
                        landmark.get(it.start())
                            .y() * imageHeight * scaleFactor,
                        landmark.get(it.end())
                            .x() * imageWidth * scaleFactor,
                        landmark.get(it.end())
                            .y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
        }

        for (result in resultsOd) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // Draw bounding box around detected objects
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            // Create text to display alongside detected objects
            //라벨 수정 제발 한 번에 되라!!!!
            val drawableText = result.categories[0].label

            // Draw rect behind display text
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + Companion.BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            // Draw text for detected object
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }
    }

    fun setResultsHl(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        resultsHl = handLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    fun setResultsOd(
        detectionResults: MutableList<Detection>,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        resultsOd = detectionResults

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
