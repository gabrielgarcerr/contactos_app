package com.example.contactos_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos")
data class Contacto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    val telefono: String,

    val correo: String,

    val foto: String = "",

    val sincronizado: Boolean = false,

    val operacionPendiente: String = ""
)