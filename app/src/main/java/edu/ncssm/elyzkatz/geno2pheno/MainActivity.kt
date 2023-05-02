package edu.ncssm.elyzkatz.geno2pheno

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import edu.ncssm.elyzkatz.geno2pheno.ui.theme.Geno2PhenoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Geno2PhenoTheme {
                // A surface container using the 'background' color from the theme
                var selectedImageUri by remember {
                    mutableStateOf<Uri?>(null)
                }
                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = {uri -> selectedImageUri = uri}
                )
                UploadBtn(singlePhotoPickerLauncher, selectedImageUri)
            }
        }
    }
}

@Composable
fun UploadBtn(photoLauncher : ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>, uri : Uri?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {// Pick button
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(text = "Pick one photo")
                }
            }
        }

        item { // Display photo
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
