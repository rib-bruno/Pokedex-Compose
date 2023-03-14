package com.plcoding.jetpackcomposepokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.MutatePriority
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import com.plcoding.jetpackcomposepokedex.data.models.PokeDexListEntry
import com.plcoding.jetpackcomposepokedex.repository.PokemonRepository
import com.plcoding.jetpackcomposepokedex.util.Constants.PAGE_SIZE
import com.plcoding.jetpackcomposepokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    //pagination eh business logic; escrever a logica para carregar 20 entradas de uma vez
    private var curPage = 0

    //poderia fazer um flow primeiro e depois converter pra state

    var pokemonList = mutableStateOf<List<PokeDexListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false) //ao chegar no final da lista, parar a paginacao

    private var cachedPokemonList = listOf<PokeDexListEntry>()
    private var isSearchStarting = true //so sera true  enquanto o campo de pesquisa estiver vazio
    var isSearching = mutableStateOf(false) //enquanto o resultado de pesquisa estiver ativo


    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String) {
        val listToSearch = if (isSearchStarting) {
            pokemonList.value
        } else{
            cachedPokemonList
        }
        //pesquisando em uma lista potencialmente longa
        viewModelScope.launch (Dispatchers.Default){
            if (query.isEmpty()) {
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }
            //pesquisando algo/search nao esta vazio
            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                    it.number.toString() == query.trim() //permite tambem pesquisar pelo numero do pokemon
            }
            //verificar se a pesquisa esta comecando; nesse caso armazenar em cache nossa lista de pokemon
            if (isSearchStarting) {
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }
            pokemonList.value = results //resultado agora eh a lista de pokemon filtrada; setando o valor do pokemon list para esse resultado
            //ele sera automaticamente exibido na lazy colum
            isSearching.value = true

        }
    }


    //corrotina pois sera feita uma solicitacao de api aqui
fun loadPokemonPaginated() {
    viewModelScope.launch {
        isLoading.value = true
        val result = repository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)
        when(result) {
            is Resource.Success -> {
                endReached.value = curPage * PAGE_SIZE >= result.data!!.count //atualmente carregamos mais entradas do que
                //nosos conjunto de resultados realmente contem, portanto esse count eh quantos pokemons existem no total

                val pokedexEntries = result.data.results.mapIndexed { index, entry ->
                    val number = if(entry.url.endsWith("/")) {
                        entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                    } else {
                        entry.url.takeLastWhile { it.isDigit() }
                    }
                    val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"
                //construindo uma entrada na lista pokedex para mapear essas entradas de resultado para nossa classe de entrada
                    //da lista pokedex
                    PokeDexListEntry(entry.name.capitalize(Locale.ROOT), url, number.toInt() )
                }
                //aumentar a pag atual pois agora, para carregar a prox pagina quando chamams essa funcao, queremos redefinir
                // o loadError
                curPage++

                loadError.value = ""
                isLoading.value = false
                pokemonList.value += pokedexEntries //adicionando essas entradas carregadas à nossa lista de pokemon, que sera
                //a lista com todos os pokemon  e esta(pokedexentries) eh a lista apenas com  o pokemon recem paginado

            }
            is Resource.Error -> {

                loadError.value = result.message!!
                isLoading.value = false //se recebemos um erro, nao estamos carregando mais

            }
        }
    }

}

        //assincrono, pode levar tempo
        fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit ) {

            //convertendo; tornar esse bitmap mutavel e converte-lo em em um tipo de config q funcione
            val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            Palette.from(bmp).generate { palette ->
                palette?.dominantSwatch?.rgb?.let { colorValue ->
                    onFinish(Color(colorValue))
                } //verificar se o valor nao eh nulo
            }
        }
}