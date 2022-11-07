package com.karcz.piotr.camera_samples

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object StorageFacade {

    private const val SUBDIRECTORY = "pictures"
    private const val FILENAME = "picture"
    private const val SUFFIX = ".jpg"

    fun createPictureFile(
        applicationDirectory: File,
        bytes: ByteArray
    ): Boolean {
        val file = initializeFile(applicationDirectory) ?: return false

        val bitmapBytes = adjustBitmapBytes(bytes)

        return try {
            FileOutputStream(file).use { it.write(bitmapBytes) }
            true
        } catch (e: Exception) {
            Timber.d(e)
            false
        }
    }

    fun getPicture(applicationDirectory: File) =
        File(applicationDirectory.path + File.separator + SUBDIRECTORY + File.separator + FILENAME + SUFFIX)

    private fun adjustBitmapBytes(data: ByteArray): ByteArray {
        val bitmapData = BitmapFactory.decodeByteArray(data, 0, data.size)

        val matrix = Matrix()
        matrix.postRotate(90F)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmapData, bitmapData.width, bitmapData.height, true)
        val rotatedBitmap =
            Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        return outputStream.toByteArray()
    }

    private fun initializeFile(applicationDirectory: File): File? {
        val mediaStorageDir = File(applicationDirectory, SUBDIRECTORY)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory")
                return null
            }
        }

        val file = File(mediaStorageDir.path + File.separator + FILENAME + SUFFIX)

        if (file.exists() && !file.delete()) {
            return null
        }
        return file
    }
}

