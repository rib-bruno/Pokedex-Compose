package com.plcoding.jetpackcomposepokedex.util

sealed class Resource<T>(val data: T? = null, val message: String? = null ) {
    //definindo versoes diferentes dessa classe para cada estado de resposta

    class Success<T>(data: T) : Resource<T>(data) //passar um dado aqui eh mandatorio
    class Error<T>(message: String,data: T? = null) : Resource<T>(data, message) //dado nao eh mandatorio, porem a mensagem eh
    class Loading<T>(data: T? = null) : Resource<T>(data)

}
