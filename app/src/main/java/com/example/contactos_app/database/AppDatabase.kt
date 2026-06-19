package com.example.contactos_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.data.ContactoDao

@Database(
    entities = [Contacto::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactoDao(): ContactoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contactos_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}