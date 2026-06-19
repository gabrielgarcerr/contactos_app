package com.example.contactos_app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query(
        """
        SELECT * FROM contactos
        WHERE operacionPendiente != 'ELIMINAR'
        ORDER BY nombre ASC
        """
    )
    fun obtenerContactos(): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos WHERE id = :id")
    suspend fun obtenerContacto(id: Int): Contacto?

    @Query("SELECT * FROM contactos")
    suspend fun obtenerContactosLista(): List<Contacto>

    @Query("SELECT * FROM contactos WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<Contacto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(contacto: Contacto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(contactos: List<Contacto>)

    @Update
    suspend fun actualizar(contacto: Contacto)

    @Delete
    suspend fun eliminar(contacto: Contacto)

    @Query("DELETE FROM contactos")
    suspend fun borrarTodos()

    @Query("DELETE FROM contactos WHERE sincronizado = 1")
    suspend fun borrarSincronizados()

    @Query("DELETE FROM contactos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}