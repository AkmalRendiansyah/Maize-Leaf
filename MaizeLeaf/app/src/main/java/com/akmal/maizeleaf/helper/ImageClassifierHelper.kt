package com.akmal.maizeleaf.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.ml.DeteksiPenyakitJagung
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ImageClassifierHelper(
    val context: Context,
    val classifierListener: ClassifierListener?
) {

    fun classifyStaticImage(imageUri: Uri) {
        try {

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }


            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val argbBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)


            val model = DeteksiPenyakitJagung.newInstance(context)


            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(argbBitmap)

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .add(CastOp(DataType.FLOAT32))
                .build()

            val processedImage = imageProcessor.process(tensorImage)


            val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputTensor.loadBuffer(processedImage.buffer)


            val outputs = model.process(inputTensor)
            val outputTensor = outputs.outputFeature0AsTensorBuffer
            val outputArray = outputTensor.floatArray


            val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
            val confidence = outputArray[maxIndex]
            val label = getLabelFromIndex(maxIndex)

            val resultText = label
            classifierListener?.onResults(resultText, confidence)

            model.close()

        } catch (e: Exception) {
            val errorMessage = "Gagal memproses gambar: ${e.message}"
            classifierListener?.onError(errorMessage)
            Log.e(TAG, errorMessage, e)
        }
    }

    private fun getLabelFromIndex(index: Int): String {
        val labels = listOf(
            "Blight",
            "Common_Rust",
            "Gray_Leaf_Spot",
            "Healthy"
        )
        return if (index in labels.indices) labels[index] else "Tidak diketahui"
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(resultText: String, confidence: Float)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}
