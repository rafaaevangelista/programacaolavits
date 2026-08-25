package org.lavits.programacao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dados locais: carregados de assets uma única vez, sem rede.
        val program = ProgramLoader.load(applicationContext)
        val favorites = FavoritesStore(applicationContext)

        setContent {
            LavitsTheme {
                ProgramApp(program = program, favorites = favorites)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramApp(program: Program, favorites: FavoritesStore) {

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var hiddenTypes by remember { mutableStateOf(emptySet<SessionType>()) }
    var expandedIds by remember { mutableStateOf(emptySet<String>()) }

    val agendaTab = program.days.size
    val searching = query.isNotBlank()

    val toggleExpand: (String) -> Unit = { id ->
        expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Column {
                        Text(
                            text = "VII Simpósio LAVITS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "26–28 de agosto de 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {
                program.days.forEachIndexed { index, day ->
                    DayTabItem(
                        selected = selectedTab == index,
                        label = "${day.key} de agosto · ${day.label}",
                        onClick = { query = ""; selectedTab = index }
                    )
                }
                DayTabItem(
                    selected = selectedTab == agendaTab,
                    label = if (favorites.count > 0) {
                        "Minha agenda (${favorites.count})"
                    } else {
                        "Minha agenda"
                    },
                    onClick = { query = ""; selectedTab = agendaTab }
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                placeholder = { Text("Buscar autor, instituição, título…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            if (searching || selectedTab < program.days.size) {
                TypeFilterRow(
                    hiddenTypes = hiddenTypes,
                    onToggle = { type ->
                        hiddenTypes =
                            if (type in hiddenTypes) hiddenTypes - type else hiddenTypes + type
                    }
                )
            }

            when {
                searching -> SearchResults(
                    program = program,
                    query = query,
                    hiddenTypes = hiddenTypes,
                    favorites = favorites,
                    expandedIds = expandedIds,
                    onToggleExpand = toggleExpand
                )

                selectedTab == agendaTab -> AgendaScreen(
                    program = program,
                    favorites = favorites,
                    expandedIds = expandedIds,
                    onToggleExpand = toggleExpand
                )

                else -> DayScreen(
                    program = program,
                    day = program.days[selectedTab],
                    hiddenTypes = hiddenTypes,
                    favorites = favorites,
                    expandedIds = expandedIds,
                    onToggleExpand = toggleExpand
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeFilterRow(
    hiddenTypes: Set<SessionType>,
    onToggle: (SessionType) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(SessionType.filterable) { type ->
            FilterChip(
                selected = type !in hiddenTypes,
                onClick = { onToggle(type) },
                label = {
                    Text(type.plural, style = MaterialTheme.typography.labelMedium)
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(colorOf(type), CircleShape)
                    )
                }
            )
        }
    }
}

@Composable
private fun DayTabItem(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
    }
}

@Composable
private fun DayScreen(
    program: Program,
    day: Day,
    hiddenTypes: Set<SessionType>,
    favorites: FavoritesStore,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit
) {
    val slots = remember(day.key, hiddenTypes) {
        program.sessionsOf(day.key)
            .filter { it.type !in hiddenTypes }
            .groupBy { it.time }
            .toList()
            .sortedBy { (_, sessions) -> sessions.first().startMinute }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "head-${day.key}") {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (slots.isEmpty()) {
            item(key = "empty-${day.key}") {
                EmptyMessage("Nenhuma atividade com os filtros atuais.")
            }
        }

        slots.forEach { (time, sessions) ->
            item(key = "slot-${day.key}-$time") { TimeHeader(time) }
            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    isFavorite = favorites.contains(session.id),
                    onToggleFavorite = { favorites.toggle(session.id) },
                    expanded = session.id in expandedIds,
                    onToggleExpand = { onToggleExpand(session.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    program: Program,
    query: String,
    hiddenTypes: Set<SessionType>,
    favorites: FavoritesStore,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit
) {
    val needle = query.normalizeForSearch().trim()

    // A busca varre os três dias de uma vez: procurar um autor não deve
    // depender de adivinhar em que dia ele apresenta.
    val hits = remember(needle, hiddenTypes) {
        program.sessions
            .filter { it.type !in hiddenTypes }
            .filter { it.searchIndex.contains(needle) }
            .sortedWith(compareBy({ it.day }, { it.startMinute }))
    }

    val daysByKey = remember { program.days.associateBy { it.key } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "search-count") {
            Text(
                text = if (hits.isEmpty()) {
                    "Nenhum resultado"
                } else if (hits.size == 1) {
                    "1 atividade encontrada"
                } else {
                    "${hits.size} atividades encontradas"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (hits.isEmpty()) {
            item(key = "search-empty") {
                EmptyMessage("Tente outro termo — a busca cobre títulos, coordenação, ministrantes, autores e instituições dos 144 trabalhos.")
            }
        }

        items(hits, key = { it.id }) { session ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${daysByKey[session.day]?.label ?: session.day} · ${session.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SessionCard(
                    session = session,
                    isFavorite = favorites.contains(session.id),
                    onToggleFavorite = { favorites.toggle(session.id) },
                    expanded = session.id in expandedIds,
                    onToggleExpand = { onToggleExpand(session.id) }
                )
            }
        }
    }
}

@Composable
private fun AgendaScreen(
    program: Program,
    favorites: FavoritesStore,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit
) {
    val slots = remember(favorites.ids) { buildAgenda(program, favorites.ids) }
    val conflicts = slots.count { it.hasConflict }

    if (slots.isEmpty()) {
        EmptyMessage(
            "Sua agenda está vazia.\n\nToque na estrela de qualquer atividade para adicioná-la aqui. " +
                "As escolhas ficam salvas no aparelho."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "agenda-head") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Minha agenda",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (conflicts == 0) {
                            "${favorites.count} atividades · sem conflitos"
                        } else if (conflicts == 1) {
                            "${favorites.count} atividades · 1 conflito de horário"
                        } else {
                            "${favorites.count} atividades · $conflicts conflitos de horário"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conflicts > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                TextButton(onClick = { favorites.clear() }) { Text("Limpar") }
            }
        }

        var lastDay: String? = null
        slots.forEach { slot ->
            if (slot.day.key != lastDay) {
                lastDay = slot.day.key
                item(key = "agenda-day-${slot.day.key}") {
                    Text(
                        text = "${slot.day.label} · ${slot.day.date}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            item(key = "agenda-slot-${slot.day.key}-${slot.time}") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TimeHeader(slot.time)
                    if (slot.hasConflict) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${slot.sessions.size} atividades simultâneas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            items(slot.sessions, key = { "agenda-${it.id}" }) { session ->
                SessionCard(
                    session = session,
                    isFavorite = true,
                    onToggleFavorite = { favorites.toggle(session.id) },
                    expanded = session.id in expandedIds,
                    onToggleExpand = { onToggleExpand(session.id) }
                )
            }
        }
    }
}

@Composable
private fun TimeHeader(time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun SessionCard(
    session: Session,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val accent = colorOf(session.type)
    val hasWorks = session.works.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // Faixa lateral colorida identificando o tipo de atividade.
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.badge,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accent
                        )
                        if (session.cancelled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Text(
                                    text = "CANCELADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = if (isFavorite) {
                                "Remover da minha agenda"
                            } else {
                                "Adicionar à minha agenda"
                            },
                            tint = if (isFavorite) {
                                accent
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                    }
                }

                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (session.cancelled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (session.cancelled) TextDecoration.LineThrough else null
                )

                if (session.people.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    session.people.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasWorks) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleExpand)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${session.works.size} trabalhos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )
                    }

                    if (expanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        session.works.forEachIndexed { index, work ->
                            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = work.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = work.authors,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
