package com.plcoding.jetpackcomposepokedex.pokemonlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import coil.request.ImageRequest
import com.google.accompanist.coil.CoilImage
import com.plcoding.jetpackcomposepokedex.R
import com.plcoding.jetpackcomposepokedex.data.models.PokeDexListEntry
import com.plcoding.jetpackcomposepokedex.ui.theme.RobotoCondensed


@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltNavGraphViewModel()
) {
    //elemento raiz de uma tela que pode ser usada dar uma cor de fundo, elevacao e entao o conteudo eh adaptado
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_international_pok_mon_logo),
                contentDescription = "Pokemon",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
            )
            SearchBar(
                hint = "Search...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                viewModel.searchPokemonList(it)

            }
            //Adicionando a pokemonlist a screen
            Spacer(modifier = Modifier.height(16.dp))
            PokemonList(navController = navController)

        }
    }
}


@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = " ",
    onSearch: (String) -> Unit = {}
) {
    var text by remember {
        mutableStateOf("") //remover o asterisco do import de remember
    }
    var isHintDisplayed by remember {
        mutableStateOf(hint != "")
    }

    Box(modifier = modifier) {
        BasicTextField(value = text,
            onValueChange = { //adicionada quando o valor de texto nesse campo muda
                text = it
                onSearch(it)

            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplayed = it != FocusState.Active && text.isEmpty()  //se tiver algo na search bar, nao mostrar o hint
                }
        )
        if (isHintDisplayed) {
            Text(text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

}

@Composable
fun PokemonList (
    navController: NavController,
    viewModel: PokemonListViewModel = hiltNavGraphViewModel()
) {
    //obter referencias aos estados definidos no viewModel
    val pokemonList by remember { viewModel.pokemonList }
    val endReached by remember {viewModel.endReached}
    val loadError by remember {viewModel.loadError}
    val isLoading by remember {viewModel.isLoading}
    val isSearching by remember { viewModel.isSearching }

    //carregar tardiamente essas entradas/ lazycolum eh equivalente ao recycleview no jetpack compose
    LazyColumn(contentPadding = PaddingValues(16.dp) ) {
        //se tivermos um numero par de pokemon na lista, divida essa lista por 2 - pq temos dois pokemon por entrada
        val itemCount = if (pokemonList.size % 2 == 0) {
            pokemonList.size / 2
        } else {
            pokemonList.size / 2 + 1
        }
        items(itemCount) {
            //rolar pro fundo - currentindex - item count
            if(it >= itemCount - 1 && !endReached && !isLoading && !isSearching) {
               LaunchedEffect(key1 = true ){
                   viewModel.loadPokemonPaginated() //se as duas condicoes forem verdadeiras,
                   // podemos carregar o pokemon paginado - que adicionara o novo pokemon a pokemonlist
               }

            }

            PokedexRow(rowIndex = it, entries = pokemonList, navController = navController)
        }
    }

    //box que exibira a retrysection se realmente houver um erro
    Box(
        contentAlignment = Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if(isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
        if (loadError.isNotEmpty()) {
            RetrySection(error = loadError) {
                viewModel.loadPokemonPaginated()
            }
        }
    }


}

@Composable
fun PokedexEntry(
    entry: PokeDexListEntry,
    navController: NavController,
    modifier: Modifier,
    viewModel: PokemonListViewModel = hiltNavGraphViewModel() //retorna o viewmodel com escopo para este navgraph

) {
    //definido se a cor dominante ainda nao tiver sido processada
    val defaultDominantColor = MaterialTheme.colors.surface

    //obtendo a cor dominante
    var dominanteColor by remember {
        mutableStateOf(defaultDominantColor)
    }

    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominanteColor,
                        defaultDominantColor
                    )
                )
            )
            .clickable {
                navController.navigate(
                    //passando a rota definida na mainact
                    "pokemon_detail_screen/${dominanteColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            CoilImage(
                data = ImageRequest.Builder(LocalContext.current) //solicitacao de imagem
                    .data(entry.imageUrl) //carregar a imagem do url
                    .target {
                        viewModel.calcDominantColor(it) { color ->
                            //atualizando o estado da cor dominante
                            dominanteColor = color
                        }

                    }
                    .build(),
                contentDescription = entry.pokemonName,
                fadeIn = true,
                modifier = Modifier
                    .size(120.dp)
                    .align(CenterHorizontally)
            ) {
                //escopo de box, uma funcao que podemos especificar composable eh mostrado quando a imagem eh carregada
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.scale(0.5f)
                )
            }
            Text(
                text = entry.pokemonName,
                fontFamily = RobotoCondensed,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()

            )
        }
    }
}

@Composable
//linha horizontal, 1, 2,3...
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokeDexListEntry>,
    navController: NavController
) {
    Column {
        Row {
            //multiplicar por 2 pois teremos dois pokemons por linha
            PokedexEntry(entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            ) //atribuir a mesma quantidade de largura para ambas entradas do pokedex
            
            Spacer(modifier = Modifier.width(16.dp))
            //verificando as entradas:
            if (entries.size >= rowIndex * 2 + 2)
                //se for esse o caso, saberemos que existem pelo menos mais duas entradas que podemos exibir em nossa lista,
                //entao, queremos exibir outra entrada de pokedex aqui
            {
                PokedexEntry(entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
//exibir o erro e a opcao de tentar novamente
fun RetrySection (
    error: String,
    onRetry: () -> Unit
) {
    Column{
        Text(error, color = Color.Red, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRetry()},
        modifier = Modifier.align(CenterHorizontally)
            )
        {
            Text(text = "Retry")
        }
    }

}