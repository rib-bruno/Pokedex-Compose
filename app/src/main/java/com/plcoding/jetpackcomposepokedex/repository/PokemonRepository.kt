package com.plcoding.jetpackcomposepokedex.repository

import com.plcoding.jetpackcomposepokedex.data.remote.PokeApi
import com.plcoding.jetpackcomposepokedex.data.remote.response.Pokemon
import com.plcoding.jetpackcomposepokedex.data.remote.response.PokemonList
import com.plcoding.jetpackcomposepokedex.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject

@ActivityScoped
class PokemonRepository @Inject constructor(
    private val api: PokeApi
) {

    suspend fun getPokemonList(limit: Int, offset: Int) : Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit,offset)
        } catch (e: Exception) {
            //informar ao viewmodel/composables sobre erros que acontecem aqui
            return Resource.Error("An unknown error occurred.")

        }
        return Resource.Success(response)
    }

    suspend fun getPokemonInfo(pokemonName: String) : Resource<Pokemon> {
        val response = try {
            api.getPokemonInfo(pokemonName)
        } catch (e: Exception) {
            //informar ao viewmodel/composables sobre erros que acontecem aqui
            return Resource.Error("An unknown error occurred.")

        }
        return Resource.Success(response)
    }
}