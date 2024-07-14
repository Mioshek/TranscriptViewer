package com.mioshek.podcastreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mioshek.podcastreader.ui.theme.PodcastReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun MainView(
    modifier: Modifier = Modifier,
    lyricsViewModel: LyricsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val lyricsUiState by lyricsViewModel.lyrics.collectAsState()
    var showCreateView by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .blur(if (showCreateView) 8.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var selectedIndex by remember { mutableStateOf(-1) }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.1f)
        ) {
            LargeDropdownMenu(
                label = if (selectedIndex != -1) lyricsViewModel.lyricsList[selectedIndex].name else "Item Not Selected",
                items = lyricsViewModel.lyricsList,
                selectedIndex = selectedIndex,
                onItemSelected = {
                    lyricsViewModel.changeLyrics(it)
                    selectedIndex = it
                },
            )
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.8f)
                .padding(start = 10.dp, end = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = lyricsUiState.displayedLyrics)
            }
            Box(modifier = modifier.fillMaxSize()){
                Row(modifier = modifier.fillMaxSize()) {
                    Box(modifier = modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                        .clickable {
                            lyricsViewModel.showPreviousPage()
                        }
                    )

                    Box(modifier = modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                        .clickable {
                            lyricsViewModel.showNextPage()
                        }
                    )
                }

            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp)
                .weight(0.1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { showCreateView = true }) {
                Text(text = "Add")
            }
        }
    }

    if (showCreateView) {
        CreatorWindow(lyricsViewModel = lyricsViewModel, { showCreateView = false })
    }
}

@Composable
fun CreatorWindow(
    lyricsViewModel: LyricsViewModel,
    exitWindow: () -> Unit,
    modifier: Modifier = Modifier
){
    var name by remember{ mutableStateOf("")}
    var content by remember{ mutableStateOf("")}
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        TextField(value = name, onValueChange = {name = it}, maxLines = 1)
        TextField(value = content, onValueChange = {content = it}, minLines = 10, maxLines = 20)

        Row {
            Button(onClick = {
                exitWindow()
            }) {
                Text(text = "Cancel")
            }

            Button(onClick = {
                lyricsViewModel.create(Lyrics(name = name, content = content, lastSelectedPage = 0))
                exitWindow()
            }) {
                Text(text = "Add")
            }
        }
    }
}

@Composable
fun LargeDropdownMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    notSetLabel: String? = null,
    items: List<LyricsUiState>,
    selectedIndex: Int = -1,
    onItemSelected: (index: Int) -> Unit,
    drawItem: @Composable (LyricsUiState, Boolean, Boolean, () -> Unit) -> Unit = { item, selected, itemEnabled, onClick ->
        LargeDropdownMenuItem(
            text = item.name,
            selected = selected,
            enabled = itemEnabled,
            onClick = onClick,
        )
    },
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        OutlinedTextField(
            label = { Text(label) },
            value = items.getOrNull(selectedIndex)?.name ?: "",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                Icon(icon, "")
            },
            onValueChange = { },
            readOnly = true,
        )

        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled) { expanded = true },
            color = Color.Transparent,
        ) {}
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
        ) {
            PodcastReaderTheme {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                ) {
                    val listState = rememberLazyListState()
                    if (selectedIndex > -1) {
                        LaunchedEffect("ScrollToSelected") {
                            listState.scrollToItem(index = selectedIndex)
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
                        if (notSetLabel != null) {
                            item {
                                LargeDropdownMenuItem(
                                    text = notSetLabel,
                                    selected = false,
                                    enabled = false,
                                    onClick = { },
                                )
                            }
                        }
                        itemsIndexed(items) { index, item ->
                            val selectedItem = index == selectedIndex
                            drawItem(
                                item,
                                selectedItem,
                                true
                            ) {
                                onItemSelected(index)
                                expanded = false
                            }

                            if (index < items.lastIndex) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargeDropdownMenuItem(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 1f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 1f)
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(modifier = Modifier
            .clickable(enabled) { onClick() }
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    PodcastReaderTheme {
        MainView()
    }
}

data class LyricsUiState(
    val id: Int = 0,
    val name: String = "",
    val loadedLyrics: Array<String> = arrayOf(""),
    val displayedLyrics: String = "",
    val selectedPage: Int = 0
)

class LyricsViewModel(private val repository: LyricsRepository): ViewModel(){
    private val _lyrics = MutableStateFlow(LyricsUiState())
    val lyrics: StateFlow<LyricsUiState> = _lyrics.asStateFlow()
    private val _lyricsList = mutableStateListOf<LyricsUiState>()
    val lyricsList: List<LyricsUiState> = _lyricsList

    init {
        getAllLyrics()
    }

    fun showNextPage(){
        if (_lyrics.value.selectedPage < _lyrics.value.loadedLyrics.size -1){
            _lyrics.update {currentState ->
                currentState.copy(
                    displayedLyrics = currentState.loadedLyrics[currentState.selectedPage + 1],
                    selectedPage = currentState.selectedPage + 1
                )
            }
            val updatedPageLyrics = Lyrics(_lyrics.value.id, _lyrics.value.name,mergeChunks(_lyrics.value.loadedLyrics), _lyrics.value.selectedPage)
            viewModelScope.launch {
                repository.upsert(updatedPageLyrics)
            }
        }
    }

    fun clear() = _lyrics.update { LyricsUiState()}

    fun showPreviousPage(){
        if (_lyrics.value.selectedPage > 0){
            _lyrics.update {currentState ->
                currentState.copy(
                    displayedLyrics = currentState.loadedLyrics[currentState.selectedPage - 1],
                    selectedPage = currentState.selectedPage - 1
                )
            }
            val updatedPageLyrics = Lyrics(_lyrics.value.id, _lyrics.value.name,mergeChunks(_lyrics.value.loadedLyrics), _lyrics.value.selectedPage)
            viewModelScope.launch {
                repository.upsert(updatedPageLyrics)
            }
        }
    }

    fun create(lyrics: Lyrics) {
        viewModelScope.launch {
            // Perform the insert operation
            repository.upsert(lyrics)

            // Fetch the last inserted item
            val inserted = repository.getLast()

            // Update the UI state
            _lyricsList.add(
                LyricsUiState(
                    id = inserted.id,
                    name = inserted.name,
                    loadedLyrics = splitStringIntoChunks(inserted.content),
                    selectedPage = inserted.lastSelectedPage
                )
            )
        }
    }

    fun delete(id: Int, uiId: Int){
        _lyricsList.remove(_lyricsList[uiId])
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun changeLyrics(id: Int){
        _lyrics.update {currentState ->
            val newLyrics = _lyricsList[id]
            currentState.copy(
                newLyrics.id,
                newLyrics.name,
                newLyrics.loadedLyrics,
                newLyrics.loadedLyrics[newLyrics.selectedPage],
                newLyrics.selectedPage
            )
        }
    }

    private fun getAllLyrics(){
        viewModelScope.launch {
            val importedLyrics = repository.getAll().first()
            importedLyrics.forEach{
                _lyricsList.add(
                    LyricsUiState(
                        id = it.id,
                        name = it.name,
                        loadedLyrics = splitStringIntoChunks(it.content),
                        selectedPage = it.lastSelectedPage
                    )
                )
            }
        }
    }

    private fun splitStringIntoChunks(input: String, chunkSize: Int = 10): Array<String> {
        val lines = input.split("\n")
        val chunks = mutableListOf<String>()
        val chunk = StringBuilder()

        for (i in lines.indices) {
            chunk.append(lines[i])
            if ((i + 1) % chunkSize == 0 || i == lines.size - 1) {
                chunks.add(chunk.toString())
                chunk.clear()
            } else {
                chunk.append("\n")
            }
        }

        return chunks.toTypedArray()
    }

    private fun mergeChunks(chunks: Array<String>): String {
        return chunks.joinToString(separator = "\n")
    }

}
