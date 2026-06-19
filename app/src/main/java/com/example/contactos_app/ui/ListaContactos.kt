package com.example.contactos_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.contactos_app.viewmodel.ContactoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaContactos(
    navController: NavController,
    viewModel: ContactoViewModel
) {

    val contactosRaw by viewModel.contactos.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val lifecycleOwner =
        LocalLifecycleOwner.current

    /*
     * Consulta Laravel cada 5 segundos solamente
     * mientras esta pantalla está visible.
     *
     * Cuando sales de la pantalla, el ciclo se detiene.
     */
    LaunchedEffect(lifecycleOwner) {

        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {

            while (true) {

                viewModel.cargarDesdeApi()

                delay(5000)
            }
        }
    }

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    val contactosFiltrados =
        contactosRaw.filter { contacto ->

            contacto.operacionPendiente != "ELIMINAR" &&
                    (
                            contacto.nombre.contains(
                                textoBusqueda,
                                ignoreCase = true
                            ) ||
                                    contacto.telefono.contains(
                                        textoBusqueda
                                    ) ||
                                    contacto.correo.contains(
                                        textoBusqueda,
                                        ignoreCase = true
                                    )
                            )
        }

    val contactosOrdenados =
        contactosFiltrados.sortedBy { contacto ->
            contacto.nombre.lowercase()
        }

    val contactosAgrupados =
        contactosOrdenados.groupBy { contacto ->

            if (contacto.nombre.isNotBlank()) {
                contacto.nombre
                    .first()
                    .uppercaseChar()
            } else {
                '#'
            }
        }

    Scaffold(
        containerColor = Color(0xFFF2F3F7),

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        "formulario/0"
                    )
                },

                containerColor = Color(0xFF3F51B5),

                shape = RoundedCornerShape(14.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar contacto",
                    tint = Color.White
                )
            }
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            item {

                OutlinedTextField(
                    value = textoBusqueda,

                    onValueChange = {
                        textoBusqueda = it
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),

                    placeholder = {
                        Text("Buscar contacto")
                    },

                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.Search,

                            contentDescription =
                                "Buscar"
                        )
                    },

                    singleLine = true,

                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (contactosFiltrados.isEmpty()) {

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = if (
                                textoBusqueda.isBlank()
                            ) {
                                "No hay contactos"
                            } else {
                                "No se encontró el contacto"
                            },

                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            contactosAgrupados.forEach {
                    (inicial, lista) ->

                item {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,

                        modifier = Modifier.padding(
                            vertical = 6.dp
                        )
                    ) {

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(0xFFDDE3F5)
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text = inicial.toString(),
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 14.sp,
                                color =
                                    Color(0xFF1A237E)
                            )
                        }
                    }
                }

                items(
                    items = lista,
                    key = { contacto ->
                        contacto.id
                    }
                ) { contacto ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                navController.navigate(
                                    "detalle/${contacto.id}"
                                )
                            },

                        shape =
                            RoundedCornerShape(20.dp),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(16.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            if (
                                contacto.foto.isNotBlank()
                            ) {

                                AsyncImage(
                                    model = contacto.foto,

                                    contentDescription =
                                        "Foto de ${contacto.nombre}",

                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape),

                                    contentScale =
                                        ContentScale.Crop
                                )

                            } else {

                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Color(
                                                0xFFDDE3F5
                                            )
                                        ),

                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Text(
                                        text = contacto
                                            .nombre
                                            .firstOrNull()
                                            ?.uppercase()
                                            ?: "?",

                                        fontWeight =
                                            FontWeight.Bold,

                                        fontSize = 18.sp,

                                        color =
                                            Color(0xFF1A237E)
                                    )
                                }
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(16.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text = contacto.nombre,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 16.sp
                                )

                                Text(
                                    text = if (
                                        contacto.correo
                                            .isNotBlank()
                                    ) {
                                        contacto.correo
                                    } else {
                                        contacto.telefono
                                    },

                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )

                                if (!contacto.sincronizado) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                3.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            "Pendiente de sincronizar",

                                        fontSize = 11.sp,

                                        color =
                                            Color(0xFFF57C00)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {

                Spacer(
                    modifier =
                        Modifier.height(90.dp)
                )
            }
        }
    }
}