package com.example.drawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.os.AsyncTask
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }

        // Undo button implementation
        val imageButtonUndo = findViewById<ImageButton>(R.id.imageButtonUndo)
        imageButtonUndo.setOnClickListener {
            drawingView.onClickUndo()
        }

        // Save button Implementation
        val imageButtonSave = findViewById<ImageButton>(R.id.imageButtonSave)
        imageButtonSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                // If permission has been given for the gallery
                BitmapAsyncTask(getBitmapFromView(drawingViewContainer)).execute()
            } else {
                // If permission has not been given than request permission
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
                        Toast.makeText(
                            this@MainActivity,
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
    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return result == PackageManager.PERMISSION_GRANTED
    }

    // Convert the view to bitmap in order to save
    private fun getBitmapFromView(view: View): Bitmap {
        // Create the bitmap with the attributes of the painting
        val returnedBitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)

        // Check if there is any background to be appended to the canvas
        val backgroundDrawable = view.background
        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

    // Function to save the painting simultaneously as Bitmap in order to save it faster
    private inner class BitmapAsyncTask(val myBitmap: Bitmap) :
        AsyncTask<Any, Void, String>() {

        private lateinit var myProgressDialog: Dialog

        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            try {
                // Compress the file to be stored
                val bytes = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                // Create the file to be stored and name it
                val file = File(
                    externalCacheDir!!.absoluteFile.toString() +
                            File.separator + "DrawingApp" +
                            System.currentTimeMillis() / 1000 +
                            ".png"
                )
                // Create file and store it
                val fileOut = FileOutputStream(file)
                fileOut.write(bytes.toByteArray())
                fileOut.close()
                result = file.absolutePath
            } catch (e: Exception) {
                result = ""
                e.printStackTrace()
            }

            return result
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (result!!.isNotEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully. $result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "File could not be saved.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Function to display the progress bar while waiting
        private fun showProgressDialog() {
            myProgressDialog = Dialog(this@MainActivity)
            myProgressDialog.setContentView(R.layout.dialog_custom_process)
            myProgressDialog.show()
        }

        // Function to close the progress bar
        private fun cancelProgressDialog() {
            myProgressDialog.dismiss()
        }

    }

    // Request Codes for the Permissions
    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}