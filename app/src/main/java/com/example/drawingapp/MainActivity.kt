package com.example.drawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var myCurrentBrushColor: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set Brush Attributes
        drawingView.setSizeBrush(20.toFloat())
        myCurrentBrushColor = colorPallet[1] as ImageButton
        myCurrentBrushColor!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        imageButtonBrush.setOnClickListener {
            brushSizeChooserDialog()
        }

        // Gallery button implementation
        val imageButtonGallery = findViewById<ImageButton>(R.id.imageButtonGallery)
        imageButtonGallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                // Intent for the gallery to pick the images
                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }
    }

    // Function to change the background with the picked gallery image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        // Change the background of the view with the chosen data from the user
                        val background = findViewById<ImageView>(R.id.backgroundImage)
                        background.visibility = View.VISIBLE
                        background.setImageURI(data.data)
                    } else {
                        // If couldn't changed the background...
                        Toast.makeText(this@MainActivity,
                            "Error in changing the background image please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Function to choose the brush size
    private fun brushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        brushDialog.setTitle("Brush Size: ")
        val smallButton = brushDialog.imageButtonSmall
        val mediumButton = brushDialog.imageButtonMedium
        val largeButton = brushDialog.imageButtonLarge

        smallButton.setOnClickListener {
            drawingView.setSizeBrush(10.toFloat())
            brushDialog.dismiss()
        }

        mediumButton.setOnClickListener {
            drawingView.setSizeBrush(20.toFloat())
            brushDialog.dismiss()
        }

        largeButton.setOnClickListener {
            drawingView.setSizeBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    // Function to change the view of the color pallet when it is clicked
    fun paintClicked(view: View) {
        if (view != myCurrentBrushColor) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()

            // Set newly chosen color pallet to pressed
            drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_pressed
                )
            )

            // Set the changed color pallet to normal
            myCurrentBrushColor!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            myCurrentBrushColor = view
        }
    }

    // Function to request the permission code from the user for the gallery
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(
                this,
                "Need permission to change the background.",
                Toast.LENGTH_SHORT
            ).show()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    // Function to handle the request for the storage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            // If request is already granted
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted, you can read the storage.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // If the request has been denied
                Toast.makeText(
                    this@MainActivity,
                    "Oops, you have just denied the permission.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // To check if the storage permission has been given or not
    private fun isReadStorageAllowed (): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    // Request Codes for the Permissions
    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val  GALLERY = 2
    }

}