package edu.ncssm.elyzkatz.geno2pheno

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import com.yalantis.ucrop.UCrop
import edu.ncssm.elyzkatz.geno2pheno.ui.theme.Geno2PhenoTheme
import java.io.File
import java.util.concurrent.ExecutionException


class MainActivity : ComponentActivity() {
    private var cameraView: PreviewView? = null

    // Creates cropping activity contract
    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(3f, 4f)

            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return UCrop.getOutput(intent!!)!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If I don't have Camera permissions, ask for them
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val cameraProvider: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(getBaseContext())
            requestPermissions(arrayOf<String>(Manifest.permission.CAMERA),
                MainActivity.CAMERA_REQUEST_CODE
            )
        }
        // Initialize Camera
        val cameraProvider: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(getBaseContext())

        setContent {
            Geno2PhenoTheme {
                // A surface container using the 'background' color from the theme
                var selectedImageUri by remember { // Empty uri
                    mutableStateOf<Uri?>(null)
                }
                var croppedImageUri by remember { // Empty uri
                    mutableStateOf<Uri?>(null)
                }

                val cropperLauncher = rememberLauncherForActivityResult(
                    contract = uCropContract,
                    onResult = { uri ->
                        croppedImageUri = uri
                        val starter = Intent(this@MainActivity, DNAEditor::class.java)
                        starter.putExtra("uri", croppedImageUri.toString())
                        startActivity(starter)
                        finish()
                    })

                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        selectedImageUri = uri
                        if (uri != null) {
                            val inputUri = uri
                            val outputUri = File(filesDir, "croppedImage.jpg").toUri()

                            val listUri = listOf(inputUri, outputUri)

                            cropperLauncher.launch(listUri) // launch cropper when photo selected
                        }
                    }
                )


                // UI
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {// Pick button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(5.dp),
                                onClick = {
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }) {
                                Text(text = "Select photo")
                            }
                            // Camera code goes here
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(5.dp),
                                onClick = {
                                    cameraProvider.addListener(CameraHandler(), ContextCompat.getMainExecutor(
                                        getBaseContext()))
                                }
                            ) {
                                Text(text = "Take photo")
                            }
                        }
                    }

                    item {
                        AndroidView(
                            modifier = Modifier
                                .padding(10.dp)
                                .absoluteOffset(0.dp, 100.dp),
                            factory = { context ->
                                cameraView = PreviewView(context)
                                cameraView!!
                            },
                            update = { view ->
                                // Update the view
                            }
                        )
                    }

                    item { // Display photo
                        AsyncImage(
                            model = croppedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Handle permission requests
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private inner class CameraHandler : Runnable {
        override fun run() {
            // The camera had been initialized!
            var cameraProvider: ProcessCameraProvider? = null
            try {
                // Get the camera and set the surface to be updated
                cameraProvider = ProcessCameraProvider.getInstance(getBaseContext()).get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(cameraView!!.getSurfaceProvider())
                val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this@MainActivity, cameraSelector, preview)
                } catch (e: Exception) {
                    Log.e("CAMERA", "Binding failed")
                }
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}