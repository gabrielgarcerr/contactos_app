package com.example.contactos_app.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.contactos_app.ui.DetalleContacto
import com.example.contactos_app.ui.FormularioContacto
import com.example.contactos_app.ui.ListaContactos
import com.example.contactos_app.viewmodel.ContactoViewModel

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    // Contexto para crear el AndroidViewModel correctamente
    val context = LocalContext.current

    val viewModel: ContactoViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    NavHost(
        navController = navController,
        startDestination = "lista"
    ) {

        // Pantalla principal
        composable("lista") {
            ListaContactos(
                navController = navController,
                viewModel = viewModel
            )
        }

        // Pantalla detalle
        composable(
            route = "detalle/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.getInt("id") ?: 0

            DetalleContacto(
                navController = navController,
                viewModel = viewModel,
                id = id
            )
        }

        // Pantalla formulario
        composable(
            route = "formulario/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.getInt("id") ?: 0

            FormularioContacto(
                navController = navController,
                viewModel = viewModel,
                id = id
            )
        }
    }
}