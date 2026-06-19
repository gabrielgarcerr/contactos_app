package com.example.contactos_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.database.AppDatabase
import com.example.contactos_app.network.CreateUserRequest
import com.example.contactos_app.network.RetrofitClient
import com.example.contactos_app.network.UpdateUserRequest
import com.example.contactos_app.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ContactoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        AppDatabase.getDatabase(application)

    private val dao =
        database.contactoDao()

    /*
     * Evita que se ejecuten varias sincronizaciones
     * simultáneamente.
     */
    private val syncMutex = Mutex()

    /*
     * Room notifica automáticamente a Compose cuando
     * cambia la tabla.
     */
    val contactos = dao.obtenerContactos()

    private val _contacto =
        MutableStateFlow<Contacto?>(null)

    val contacto: StateFlow<Contacto?> = _contacto

    init {
        SyncManager.programarSincronizacion(
            getApplication()
        )

        cargarDesdeApi()
    }

    private fun contactoDesdeApi(
        id: Int,
        nombre: String,
        telefono: String?,
        correo: String,
        foto: String?
    ): Contacto {

        return Contacto(
            id = id,
            nombre = nombre,
            telefono = telefono ?: "",
            correo = correo,
            foto = foto ?: "",
            sincronizado = true,
            operacionPendiente = ""
        )
    }

    /*
     * Envía las operaciones realizadas sin Internet.
     */
    private suspend fun sincronizarPendientes() {

        val pendientes = dao.obtenerPendientes()

        pendientes.forEach { contacto ->

            when (contacto.operacionPendiente) {

                "CREAR" -> {

                    val respuesta =
                        RetrofitClient.apiService.createUser(
                            CreateUserRequest(
                                name = contacto.nombre,
                                email = contacto.correo,
                                phone = contacto.telefono
                            )
                        )

                    val user = respuesta.data

                    /*
                     * El contacto creado sin Internet usa un ID local.
                     * Se elimina para insertar el ID real de Laravel.
                     */
                    dao.eliminarPorId(contacto.id)

                    dao.insertar(
                        contactoDesdeApi(
                            id = user.id,
                            nombre = user.name,
                            telefono = user.phone,
                            correo = user.email,
                            foto = user.foto ?: contacto.foto
                        )
                    )
                }

                "EDITAR" -> {

                    val respuesta =
                        RetrofitClient.apiService.updateUser(
                            id = contacto.id,
                            user = UpdateUserRequest(
                                name = contacto.nombre,
                                email = contacto.correo,
                                phone = contacto.telefono
                            )
                        )

                    val user = respuesta.data

                    dao.insertar(
                        contactoDesdeApi(
                            id = user.id,
                            nombre = user.name,
                            telefono = user.phone,
                            correo = user.email,
                            foto = user.foto ?: contacto.foto
                        )
                    )
                }

                "ELIMINAR" -> {

                    RetrofitClient.apiService.deleteUser(
                        contacto.id
                    )

                    dao.eliminarPorId(contacto.id)
                }
            }
        }
    }

    /*
     * Descarga los usuarios del servidor.
     *
     * El borrado y la inserción se realizan dentro
     * de una sola transacción. Por eso Compose no
     * alcanza a mostrar la lista vacía y no parpadea.
     */
    private suspend fun actualizarCacheDesdeApi() {

        val respuesta =
            RetrofitClient.apiService.getUsers()

        val contactosServidor =
            respuesta.data.map { user ->

                contactoDesdeApi(
                    id = user.id,
                    nombre = user.name,
                    telefono = user.phone,
                    correo = user.email,
                    foto = user.foto
                )
            }

        database.withTransaction {

            /*
             * Solo se borran los contactos que ya estaban
             * sincronizados.
             *
             * Los pendientes sin Internet permanecen.
             */
            dao.borrarSincronizados()

            dao.insertarTodos(contactosServidor)
        }
    }

    /*
     * Realiza la sincronización completa.
     *
     * Mutex evita que dos consultas de los 5 segundos
     * se ejecuten al mismo tiempo.
     */
    private suspend fun sincronizarTodo() {

        syncMutex.withLock {

            try {

                sincronizarPendientes()

            } catch (e: Exception) {

                e.printStackTrace()

                SyncManager.programarSincronizacion(
                    getApplication()
                )
            }

            try {

                actualizarCacheDesdeApi()

            } catch (e: Exception) {

                /*
                 * Si no existe conexión, se conservan
                 * los datos actuales de Room.
                 */
                e.printStackTrace()
            }
        }
    }

    fun cargarDesdeApi() {

        viewModelScope.launch {

            sincronizarTodo()
        }
    }

    fun cargarContacto(id: Int) {

        viewModelScope.launch {

            _contacto.value =
                dao.obtenerContacto(id)
        }
    }

    fun limpiarContacto() {

        _contacto.value = null
    }

    /*
     * Crear contacto.
     */
    fun guardar(contacto: Contacto) {

        viewModelScope.launch {

            try {

                val respuesta =
                    RetrofitClient.apiService.createUser(
                        CreateUserRequest(
                            name = contacto.nombre,
                            email = contacto.correo,
                            phone = contacto.telefono
                        )
                    )

                val user = respuesta.data

                dao.insertar(
                    contactoDesdeApi(
                        id = user.id,
                        nombre = user.name,
                        telefono = user.phone,
                        correo = user.email,
                        foto = user.foto ?: contacto.foto
                    )
                )

                actualizarCacheDesdeApi()

            } catch (e: Exception) {

                e.printStackTrace()

                /*
                 * Sin conexión: se guarda localmente
                 * como operación pendiente.
                 */
                dao.insertar(
                    contacto.copy(
                        sincronizado = false,
                        operacionPendiente = "CREAR"
                    )
                )

                SyncManager.programarSincronizacion(
                    getApplication()
                )
            }
        }
    }

    /*
     * Editar contacto.
     */
    fun actualizar(contacto: Contacto) {

        viewModelScope.launch {

            try {

                val respuesta =
                    RetrofitClient.apiService.updateUser(
                        id = contacto.id,
                        user = UpdateUserRequest(
                            name = contacto.nombre,
                            email = contacto.correo,
                            phone = contacto.telefono
                        )
                    )

                val user = respuesta.data

                val contactoActualizado =
                    contactoDesdeApi(
                        id = user.id,
                        nombre = user.name,
                        telefono = user.phone,
                        correo = user.email,
                        foto = user.foto ?: contacto.foto
                    )

                dao.insertar(contactoActualizado)

                _contacto.value =
                    contactoActualizado

                actualizarCacheDesdeApi()

            } catch (e: Exception) {

                e.printStackTrace()

                /*
                 * Sin conexión: se guarda la edición
                 * como pendiente.
                 */
                dao.insertar(
                    contacto.copy(
                        sincronizado = false,
                        operacionPendiente = "EDITAR"
                    )
                )

                SyncManager.programarSincronizacion(
                    getApplication()
                )
            }
        }
    }

    /*
     * Eliminar contacto.
     */
    fun eliminar(contacto: Contacto) {

        viewModelScope.launch {

            try {

                RetrofitClient.apiService.deleteUser(
                    contacto.id
                )

                dao.eliminarPorId(contacto.id)

                actualizarCacheDesdeApi()

            } catch (e: Exception) {

                e.printStackTrace()

                /*
                 * Sin conexión: se marca para eliminar
                 * cuando vuelva Internet.
                 */
                dao.insertar(
                    contacto.copy(
                        sincronizado = false,
                        operacionPendiente = "ELIMINAR"
                    )
                )

                SyncManager.programarSincronizacion(
                    getApplication()
                )
            }
        }
    }

    fun recargarContactos() {

        cargarDesdeApi()
    }

    fun telefonoValido(
        telefono: String
    ): Boolean {

        return telefono.length == 10 &&
                telefono.all { caracter ->
                    caracter.isDigit()
                }
    }

    suspend fun nombreExiste(
        nombre: String,
        idActual: Int
    ): Boolean {

        val lista =
            dao.obtenerContactosLista()

        return lista.any { contacto ->

            contacto.operacionPendiente != "ELIMINAR" &&
                    contacto.nombre.equals(
                        nombre,
                        ignoreCase = true
                    ) &&
                    contacto.id != idActual
        }
    }

    suspend fun telefonoExiste(
        telefono: String,
        idActual: Int
    ): Boolean {

        val lista =
            dao.obtenerContactosLista()

        return lista.any { contacto ->

            contacto.operacionPendiente != "ELIMINAR" &&
                    contacto.telefono == telefono &&
                    contacto.id != idActual
        }
    }

    suspend fun correoExiste(
        correo: String,
        idActual: Int
    ): Boolean {

        val lista =
            dao.obtenerContactosLista()

        return lista.any { contacto ->

            contacto.operacionPendiente != "ELIMINAR" &&
                    contacto.correo.equals(
                        correo,
                        ignoreCase = true
                    ) &&
                    contacto.id != idActual
        }
    }

    suspend fun validarContacto(
        nombre: String,
        telefono: String,
        correo: String,
        idActual: Int
    ): Map<String, String> {

        val errores =
            mutableMapOf<String, String>()

        if (nombre.isBlank()) {

            errores["nombre"] =
                "El nombre es obligatorio"

        } else if (
            nombreExiste(nombre, idActual)
        ) {

            errores["nombre"] =
                "El nombre ya existe"
        }

        if (!telefonoValido(telefono)) {

            errores["telefono"] =
                "El teléfono debe tener 10 dígitos"

        } else if (
            telefonoExiste(telefono, idActual)
        ) {

            errores["telefono"] =
                "El teléfono ya existe"
        }

        if (correo.isBlank()) {

            errores["correo"] =
                "El correo es obligatorio"

        } else if (
            correoExiste(correo, idActual)
        ) {

            errores["correo"] =
                "El correo ya existe"
        }

        return errores
    }
}