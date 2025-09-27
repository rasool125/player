package com.example.blutoothapp.Data.dkfjdkfsfdjksfksdf

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class HomeActivity : AppCompatActivity() {

    private lateinit var btnPickImage: Button
    private lateinit var imageView: ImageView
    private lateinit var btnClear: ImageButton
    private lateinit var scrollView: ScrollView




    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnPickImage = findViewById(R.id.btnPickImage)
        imageView = findViewById(R.id.imageView)
        btnClear = findViewById(R.id.btnClear)
        scrollView = findViewById(R.id.scrollView)

        // Initially hide image and clear button
        imageView.setImageDrawable(null)
        btnClear.isVisible = false
        scrollView.isVisible = false

        // Register the image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val uri = data?.data
                if (uri != null) {
                    currentImageUri = uri
                    displayImageFromUri(uri)
                    btnClear.isVisible = true
                    scrollView.isVisible = true
                }
            }
        }

        btnPickImage.setOnClickListener {
            openImagePicker()
        }

        btnClear.setOnClickListener {
            clearSelectedImage()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun displayImageFromUri(uri: Uri) {
        // Decode and display the image; handle API differences
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        imageView.setImageBitmap(bitmap)
    }

    private fun clearSelectedImage() {
        currentImageUri = null
        imageView.setImageDrawable(null)
        btnClear.isVisible = false
        scrollView.isVisible = false
    }
}
