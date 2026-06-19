package com.example.contactos_app.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ApiResponse(
    val data: List<ApiUser>
)

data class UserResponse(
    val data: ApiUser
)

data class ApiUser(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val foto: String?
)
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String = "12345678",
    val phone: String
)

data class UpdateUserRequest(
    val name: String,
    val email: String,
    val phone: String
)

interface ApiService {

    @GET("api/users")
    suspend fun getUsers(): ApiResponse

    @POST("api/users")
    suspend fun createUser(
        @Body user: CreateUserRequest
    ): UserResponse

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: UpdateUserRequest
    ): UserResponse

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    )
}