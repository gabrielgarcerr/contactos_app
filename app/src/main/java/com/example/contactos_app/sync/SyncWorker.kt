package com.example.contactos_app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.database.AppDatabase
import com.example.contactos_app.network.CreateUserRequest
import com.example.contactos_app.network.RetrofitClient
import com.example.contactos_app.network.UpdateUserRequest

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            val dao = AppDatabase
                .getDatabase(applicationContext)
                .contactoDao()

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

                        dao.eliminarPorId(contacto.id)

                        dao.insertar(
                            Contacto(
                                id = user.id,
                                nombre = user.name,
                                telefono = user.phone ?: "",
                                correo = user.email,
                                foto = user.foto ?: contacto.foto,
                                sincronizado = true,
                                operacionPendiente = ""
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
                            Contacto(
                                id = user.id,
                                nombre = user.name,
                                telefono = user.phone ?: "",
                                correo = user.email,
                                foto = user.foto ?: contacto.foto,
                                sincronizado = true,
                                operacionPendiente = ""
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

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()
            Result.retry()
        }
    }
}