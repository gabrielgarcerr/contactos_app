package com.example.contactos_app.ui

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.contactos_app.R
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.viewmodel.ContactoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

fun guardarImagenInterna(
    context: Context,
    uri: Uri
): String {

    val archivo = File(
        context.filesDir,
        "contacto_${System.currentTimeMillis()}.jpg"
    )

    context.contentResolver.openInputStream(uri).use { input ->

        requireNotNull(input) {
            "No se pudo abrir la imagen seleccionada"
        }

        FileOutputStream(archivo).use { output ->
            input.copyTo(output)
        }
    }

    return archivo.absolutePath
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContacto(
    navController: NavController,
    viewModel: ContactoViewModel,
    id: Int
) {

    var nombre by remember(id) {
        mutableStateOf("")
    }

    var telefono by remember(id) {
        mutableStateOf("")
    }

    var correo by remember(id) {
        mutableStateOf("")
    }

    var fotoUri by remember(id) {
        mutableStateOf<Uri?>(null)
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var correoError by remember {
        mutableStateOf<String?>(null)
    }

    var telefonoError by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (uri != null) {
            fotoUri = uri
        }
    }

    val contactoExistente by viewModel.contacto.collectAsStateWithLifecycle()

    LaunchedEffect(id) {

        if (id == 0) {

            viewModel.limpiarContacto()

            nombre = ""
            telefono = ""
            correo = ""
            fotoUri = null

        } else {

            viewModel.cargarContacto(id)
        }
    }

    LaunchedEffect(contactoExistente, id) {

        if (id != 0) {

            contactoExistente?.let { contacto ->

                nombre = contacto.nombre
                telefono = contacto.telefono
                correo = contacto.correo

                fotoUri = if (contacto.foto.isNotBlank()) {
                    Uri.parse(contacto.foto)
                } else {
                    null
                }
            }
        }
    }

    val titulo = if (id == 0) {
        "Nuevo Contacto"
    } else {
        "Editar Contacto"
    }

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = titulo,
                        color = Color.White
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
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
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3F51B5))
                    .padding(vertical = 32.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {

                    AsyncImage(
                        model = fotoUri
                            ?: contactoExistente?.foto
                            ?: R.drawable.ic_launcher_foreground,

                        contentDescription = "Foto del contacto",

                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(alpha = 0.2f)
                            ),

                        contentScale = ContentScale.Crop
                    )

                    SmallFloatingActionButton(
                        onClick = {
                            launcher.launch("image/*")
                        },

                        containerColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cambiar foto",
                            tint = Color(0xFF3F51B5)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = nombre.ifBlank {
                        "Nombre del Contacto"
                    },

                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.padding(24.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = nombre,

                    onValueChange = { nuevoNombre ->

                        if (nuevoNombre.length <= 50) {

                            nombre = nuevoNombre

                            nombreError = if (nombre.isBlank()) {
                                "El nombre es requerido"
                            } else {
                                null
                            }
                        }
                    },

                    label = {
                        Text("Nombre")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },

                    trailingIcon = {
                        Text("${nombre.length}/50")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (
                                !focusState.isFocused &&
                                nombre.isNotBlank()
                            ) {

                                scope.launch {

                                    nombreError =
                                        if (
                                            viewModel.nombreExiste(
                                                nombre,
                                                id
                                            )
                                        ) {
                                            "Este nombre ya existe"
                                        } else {
                                            null
                                        }
                                }
                            }
                        },

                    isError = nombreError != null,

                    supportingText = {
                        nombreError?.let { error ->
                            Text(error)
                        }
                    }
                )

                OutlinedTextField(
                    value = correo,

                    onValueChange = { nuevoCorreo ->

                        correo = nuevoCorreo

                        correoError = when {

                            correo.isBlank() -> {
                                null
                            }

                            !Patterns.EMAIL_ADDRESS
                                .matcher(correo)
                                .matches() -> {
                                "Correo no válido"
                            }

                            else -> {
                                null
                            }
                        }
                    },

                    label = {
                        Text("Correo (opcional)")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (
                                !focusState.isFocused &&
                                correo.isNotBlank()
                            ) {

                                scope.launch {

                                    correoError =
                                        if (
                                            viewModel.correoExiste(
                                                correo,
                                                id
                                            )
                                        ) {
                                            "Este correo ya está registrado"
                                        } else if (
                                            !Patterns.EMAIL_ADDRESS
                                                .matcher(correo)
                                                .matches()
                                        ) {
                                            "Correo no válido"
                                        } else {
                                            null
                                        }
                                }
                            }
                        },

                    isError = correoError != null,

                    supportingText = {
                        correoError?.let { error ->
                            Text(error)
                        }
                    }
                )

                OutlinedTextField(
                    value = telefono,

                    onValueChange = { nuevoTelefono ->

                        if (
                            nuevoTelefono.length <= 10 &&
                            nuevoTelefono.all { caracter ->
                                caracter.isDigit()
                            }
                        ) {

                            telefono = nuevoTelefono

                            telefonoError = when {

                                telefono.isBlank() -> {
                                    "El teléfono es requerido"
                                }

                                telefono.length < 10 -> {
                                    "Debe tener 10 dígitos"
                                }

                                else -> {
                                    null
                                }
                            }
                        }
                    },

                    label = {
                        Text("Móvil")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null
                        )
                    },

                    trailingIcon = {
                        Text("${telefono.length}/10")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (
                                !focusState.isFocused &&
                                telefono.length == 10
                            ) {

                                scope.launch {

                                    telefonoError =
                                        if (
                                            viewModel.telefonoExiste(
                                                telefono,
                                                id
                                            )
                                        ) {
                                            "Este número ya está registrado"
                                        } else {
                                            null
                                        }
                                }
                            }
                        },

                    isError = telefonoError != null,

                    supportingText = {
                        telefonoError?.let { error ->
                            Text(error)
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {

                        scope.launch {

                            nombreError = null
                            correoError = null
                            telefonoError = null

                            var hayErrores = false

                            if (nombre.isBlank()) {

                                nombreError =
                                    "El nombre es requerido"

                                hayErrores = true
                            }

                            if (telefono.isBlank()) {

                                telefonoError =
                                    "El teléfono es requerido"

                                hayErrores = true

                            } else if (telefono.length != 10) {

                                telefonoError =
                                    "Debe tener 10 dígitos"

                                hayErrores = true
                            }

                            if (correo.isNotBlank()) {

                                if (
                                    !Patterns.EMAIL_ADDRESS
                                        .matcher(correo)
                                        .matches()
                                ) {

                                    correoError =
                                        "Correo no válido"

                                    hayErrores = true
                                }

                                if (
                                    viewModel.correoExiste(
                                        correo,
                                        id
                                    )
                                ) {

                                    correoError =
                                        "Este correo ya está registrado"

                                    hayErrores = true
                                }
                            }

                            if (
                                viewModel.nombreExiste(
                                    nombre,
                                    id
                                )
                            ) {

                                nombreError =
                                    "Este nombre ya existe"

                                hayErrores = true
                            }

                            if (
                                viewModel.telefonoExiste(
                                    telefono,
                                    id
                                )
                            ) {

                                telefonoError =
                                    "Este número ya está registrado"

                                hayErrores = true
                            }

                            if (hayErrores) {
                                return@launch
                            }

                            /*
                             * Solo las imágenes elegidas desde la galería
                             * usan content:// y deben copiarse internamente.
                             *
                             * Las URL http/https de los seeders se conservan.
                             * Las rutas locales también se conservan.
                             */
                            val rutaFoto = when {

                                fotoUri?.scheme == "content" -> {

                                    guardarImagenInterna(
                                        context = context,
                                        uri = fotoUri!!
                                    )
                                }

                                fotoUri != null -> {

                                    fotoUri.toString()
                                }

                                else -> {

                                    contactoExistente?.foto ?: ""
                                }
                            }

                            val contacto = Contacto(
                                id = id,
                                nombre = nombre.trim(),
                                telefono = telefono.trim(),
                                correo = correo.trim(),
                                foto = rutaFoto
                            )

                            if (id == 0) {

                                viewModel.guardar(contacto)

                            } else {

                                viewModel.actualizar(contacto)
                            }

                            navController.popBackStack()
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    ),

                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = if (id == 0) {
                            "Guardar"
                        } else {
                            "Actualizar"
                        },

                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}