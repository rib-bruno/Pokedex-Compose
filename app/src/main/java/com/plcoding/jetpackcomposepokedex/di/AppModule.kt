package com.plcoding.jetpackcomposepokedex.di

import com.plcoding.jetpackcomposepokedex.data.remote.PokeApi
import com.plcoding.jetpackcomposepokedex.repository.PokemonRepository
import com.plcoding.jetpackcomposepokedex.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class) //instalar esse modulo na singletoncomponent; vive enquanto a application estiver viva
object AppModule {
@Singleton
@Provides
    fun providePokemonRepository (
        api: PokeApi
    ) = PokemonRepository(api)


    @Singleton
    @Provides
    //Fornecer a api a uma instancia do retrofit na qual podemos fazer as solicitacoes de rede
    fun providePokeAPi() : PokeApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PokeApi::class.java)

    }
}
