package com.vanshpunia.take_image_and_view_on_screen

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.SensorManager.getOrientation
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var cameraOpenId: Button
    lateinit var clickImageId: ImageView
    private var imageUri: Uri? = null // Declare imageUri here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraOpenId = findViewById(R.id.camera_button)
        clickImageId = findViewById(R.id.click_image)

        cameraOpenId.setOnClickListener {
            imageUri = createImageUri()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(cameraIntent, pic_id)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pic_id && resultCode == RESULT_OK) {
            imageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                val orientation = getOrientation(it)
                val rotatedBitmap = rotateBitmap(bitmap, orientation)
                clickImageId.setImageBitmap(rotatedBitmap)
            }
        }
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    @Throws(IOException::class)
    private fun getOrientation(uri: Uri): Int {
        val inputStream = contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        inputStream.close()
        return orientation
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        private const val pic_id = 123
    }
}