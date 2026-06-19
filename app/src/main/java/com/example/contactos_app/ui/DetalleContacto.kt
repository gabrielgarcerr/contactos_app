package com.example.contactos_app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.contactos_app.viewmodel.ContactoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContacto(
    navController: NavController,
    viewModel: ContactoViewModel,
    id: Int
) {

    val context = LocalContext.current
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(id) { viewModel.cargarContacto(id) }

    val contacto by viewModel.contacto.collectAsStateWithLifecycle(initialValue = null)

    contacto?.let { c ->

        if (mostrarDialogoEliminar) {

            AlertDialog(

                onDismissRequest = { mostrarDialogoEliminar = false },

                title = { Text("Eliminar contacto") },

                text = {
                    Text("¿Estás seguro de que deseas eliminar este contacto?")
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            viewModel.eliminar(c)
                            mostrarDialogoEliminar = false
                            navController.popBackStack()

                        }

                    ) {

                        Text("Aceptar")

                    }

                },

                dismissButton = {

                    TextButton(
                        onClick = { mostrarDialogoEliminar = false }
                    ) {

                        Text("Cancelar")

                    }

                }

            )

        }

        Scaffold(

            topBar = {

                TopAppBar(

                    title = { },

                    navigationIcon = {

                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {

                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)

                        }

                    },

                    actions = {

                        IconButton(
                            onClick = {
                                navController.navigate("formulario/${c.id}")
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(Color.White, CircleShape)
                                .size(36.dp)
                        ) {

                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = Color(0xFF3F51B5),
                                modifier = Modifier.size(20.dp)
                            )

                        }

                        IconButton(
                            onClick = { mostrarDialogoEliminar = true },
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(Color(0xFFB71C1C), CircleShape)
                                .size(36.dp)
                        ) {

                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )

                        }

                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF3F51B5)
                    )

                )

            }

        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F51B5))
                        .padding(bottom = 30.dp),

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    AsyncImage(

                        model = c.foto.ifEmpty { "https://via.placeholder.com/150" },

                        contentDescription = null,

                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),

                        contentScale = ContentScale.Crop

                    )

                    Text(
                        text = c.nombre,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                    )

                    Row(
                        modifier = Modifier.padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            IconButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${c.telefono}")
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .background(Color(0xFFD9E2FF), CircleShape)
                                    .size(48.dp)
                            ) {

                                Icon(
                                    Icons.Default.Call,
                                    null,
                                    tint = Color(0xFF001945)
                                )

                            }

                            Text("Llamar", color = Color.White, fontSize = 12.sp)

                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            IconButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse("mailto:${c.correo}")
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .background(Color(0xFFD9E2FF), CircleShape)
                                    .size(48.dp)
                            ) {

                                Icon(
                                    Icons.Default.Email,
                                    null,
                                    tint = Color(0xFF001945)
                                )

                            }

                            Text("Correo", color = Color.White, fontSize = 12.sp)

                        }

                    }

                }

                Card(

                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),

                    shape = RoundedCornerShape(24.dp),

                    colors = CardDefaults.cardColors(containerColor = Color.White),

                    elevation = CardDefaults.cardElevation(2.dp)

                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        InfoRow(Icons.Default.Email, "Correo Electrónico", c.correo)

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF0F0F0)
                        )

                        InfoRow(Icons.Default.Phone, "Teléfono", c.telefono)

                    }

                }

            }

        }

    }

}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            icon,
            null,
            tint = Color(0xFF3F51B5),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {

            Text(label, color = Color.Gray, fontSize = 12.sp)

            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)

        }

    }

}