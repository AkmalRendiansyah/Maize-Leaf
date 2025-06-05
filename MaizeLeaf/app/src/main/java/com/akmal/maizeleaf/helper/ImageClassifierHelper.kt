package com.akmal.maizeleaf.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.ml.DeteksiPenyakitJagung
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResults: Int = 3,
    val modelName: String = "Deteksi_Penyakit_Jagung.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        try {
            // Decode image to Bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // Resize bitmap to 224x224 (model input size)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val argbBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            // Load model
            val model = DeteksiPenyakitJagung.newInstance(context)

            // Prepare TensorImage and preprocess it
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(argbBitmap)

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) // Normalisasi sesuai model kamu
                .add(CastOp(DataType.FLOAT32))
                .build()

            val processedImage = imageProcessor.process(tensorImage)

            // Create input tensor
            val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputTensor.loadBuffer(processedImage.buffer)

            // Run model
            val outputs = model.process(inputTensor)
            val outputTensor = outputs.outputFeature0AsTensorBuffer
            val outputArray = outputTensor.floatArray

            // Ambil hasil dengan confidence tertinggi
            val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
            val confidence = outputArray[maxIndex]
            val label = getLabelFromIndex(maxIndex)

            val resultText = "$label: ${"%.2f".format(confidence * 100)}%"

            // Callback hasil ke UI
            classifierListener?.onResults(resultText)

            model.close()

        } catch (e: Exception) {
            val errorMessage = "Gagal memproses gambar: ${e.message}"
            classifierListener?.onError(errorMessage)
            Log.e(TAG, errorMessage, e)
        }
    }

    // Implementasi sederhana label sesuai urutan output model
    private fun getLabelFromIndex(index: Int): String {
        val labels = listOf(
            "Blight",
            "Common_Rust",
            "Gray_Leaf_Spot",
            "Healthy" // Tambahkan sesuai jumlah kelas
        )
        return if (index in labels.indices) labels[index] else "Tidak diketahui"
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(resultText: String)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}