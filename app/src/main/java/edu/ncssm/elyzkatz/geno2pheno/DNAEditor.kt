package edu.ncssm.elyzkatz.geno2pheno

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import edu.ncssm.elyzkatz.geno2pheno.ui.theme.BackboneColor
import edu.ncssm.elyzkatz.geno2pheno.ui.theme.Geno2PhenoTheme
import edu.ncssm.elyzkatz.geno2pheno.ui.theme.yellow

class DNAEditor : ComponentActivity() {
    val backboneColor : Color = Color(0x3373c0)
    @OptIn(ExperimentalTextApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val faceUri = Uri.parse(getIntent().getStringExtra("uri"))
        val configuration = LocalConfiguration

        setContent {
            Geno2PhenoTheme {

                val splitLocation = width()-height()*3/4
                val topMerge = arrayOf(Nucleotide("T"), Nucleotide("A"),
                    Nucleotide("C"), Nucleotide("G"), Nucleotide("G"), Nucleotide("A"))
                var topNucleotides by remember {mutableStateOf(topMerge)}

                var hairID by remember{mutableStateOf(0)}
                var hairImg by remember {mutableStateOf(R.drawable.hairblonde)}

                var bottomNucleotides = Array(6){Nucleotide("A")}
                for (i in topNucleotides.indices) {
                    bottomNucleotides[i] = topNucleotides[i].getMatchingNucleotide()
                }
                val height = height().toFloat()
                val verticalOffset = 10f
                val backboneHeight = height/10
                val nucHeight = (height - 2*backboneHeight)/2 - verticalOffset
                val nucWidth = splitLocation*1/8
                val spaceWidth = (splitLocation - 6*nucWidth)/7

                Row(modifier = Modifier.fillMaxHeight()) {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .width(splitLocation.dp)
                    ) {
                        for (i in topNucleotides.indices) {
                            var nuc by remember { mutableStateOf(topNucleotides[i]) }
                            var bottomNuc by remember { mutableStateOf(nuc.getMatchingNucleotide()) }

                            val nucX = (spaceWidth + i * (spaceWidth + nucWidth)).toFloat()

                            // Rectangles and buttons define their sizes differently
                            // So we are using buttons in place of rectangles for the backbone
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .offset(0.dp, 0.dp)
                                    .width(splitLocation.dp)
                                    .height(backboneHeight.dp)
                                    .padding(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = BackboneColor
                                )
                            ) {}
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .offset(0.dp, (height - backboneHeight).dp)
                                    .width(splitLocation.dp)
                                    .height(backboneHeight.dp)
                                    .padding(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = BackboneColor
                                )
                            ) {}

                            // Top nucleotides
                            Button(
                                onClick = {
                                    nuc = nuc.switchToRandom()
                                    bottomNuc = nuc.getMatchingNucleotide()
                                    Log.d("lol", topNucleotides[i].getBase())
                                },
                                modifier = Modifier
                                    .offset(nucX.dp, backboneHeight.dp)
                                    .width(nucWidth.dp)
                                    .height(nucHeight.dp)
                                    .padding(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = nuc.getColor()
                                )
                            ) {
                                Text(
                                    nuc.getBase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            // bottom nucleotides
                            Button(
                                onClick = {
                                    bottomNuc = bottomNuc.switchToRandom()
                                    nuc = bottomNuc.getMatchingNucleotide()
                                },
                                modifier = Modifier
                                    .offset(nucX.dp, (height / 2 + verticalOffset).dp)
                                    .width(nucWidth.dp)
                                    .height(nucHeight.dp)
                                    .padding(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = bottomNuc.getColor()
                                )
                            ) {
                                Text(
                                    bottomNuc.getBase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
//
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Green)
                    ) {
                        AsyncImage(
                            model = faceUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.hairmedium),
                            contentDescription = "Hair",
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun height(): Int {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp
}

@Composable
fun width(): Int {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp
}

@Composable
fun nucleotide() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(yellow)
    ) {
        Text(
            text = "T",
            color = Color.Black,
        )
    }
}

