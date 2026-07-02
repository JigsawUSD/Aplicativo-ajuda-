package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AlertHistory
import com.example.models.PriorityContact
import com.example.permissions.PermissionHelper
import com.example.services.UrgencySettings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contacts: List<PriorityContact>,
    history: List<AlertHistory>,
    onAddContact: () -> Unit,
    onEditContact: (PriorityContact) -> Unit,
    onToggleContactActive: (PriorityContact, Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListenerEnabled by remember { mutableStateOf(PermissionHelper.isNotificationListenerEnabled(context)) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Resumo, 1: Contatos, 2: Histórico, 3: Urgência

    // Periodically update the service enabled state in the UI
    LaunchedEffect(Unit) {
        while (true) {
            isListenerEnabled = PermissionHelper.isNotificationListenerEnabled(context)
            kotlinx.coroutines.delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (selectedTab) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Person
                                    2 -> Icons.Default.List
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = when (selectedTab) {
                                0 -> "WakeAlert • Resumo"
                                1 -> "Contatos Prioritários"
                                2 -> "Histórico de Alertas"
                                else -> "Alerta de Urgência"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (selectedTab == 2 && history.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpar Histórico",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Resumo") },
                    label = { Text("Resumo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Contatos") },
                    label = { Text("Contatos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Histórico") },
                    label = { Text("Histórico", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurar Urgência") },
                    label = { Text("Urgência", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onAddContact,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_contact_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Contato",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> SummaryTab(
                    isEnabled = isListenerEnabled,
                    contacts = contacts,
                    onEnableClick = { PermissionHelper.openNotificationListenerSettings(context) },
                    onNavigateToContacts = { selectedTab = 1 },
                    onNavigateToUrgency = { selectedTab = 3 }
                )
                1 -> ContactsTab(
                    contacts = contacts,
                    onEditContact = onEditContact,
                    onToggleContactActive = onToggleContactActive,
                    onAddContact = onAddContact
                )
                2 -> HistoryTab(
                    history = history,
                    contacts = contacts
                )
                3 -> UrgencyTab(
                    onResetOnboarding = onResetOnboarding
                )
            }
        }
    }
}

// ==================== TAB 0: RESUMO (DASHBOARD) ====================
@Composable
fun SummaryTab(
    isEnabled: Boolean,
    contacts: List<PriorityContact>,
    onEnableClick: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToUrgency: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Pulse animation for the glowing state ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing status widget
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(160.dp * scale)
                .clip(CircleShape)
                .background(
                    if (isEnabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                )
                .border(
                    width = 2.dp,
                    color = if (isEnabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEnabled) "Monitor Ativo" else "Monitor Inativo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnabled) "Sistema operacional protegido" else "Toque para ativar",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        if (!isEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                border = CardHelper.borderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Acesso às Notificações Bloqueado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "O aplicativo necessita de acesso às notificações do sistema para conseguir capturar mensagens de urgência.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onEnableClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ativar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Monitoring Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = onNavigateToContacts,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Contatos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${contacts.filter { it.isActive }.size} ativos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "De ${contacts.size} cadastrados",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            val isUrgencyOn = UrgencySettings.isEnabled(context)
            val keywordsCount = UrgencySettings.getKeywords(context).size
            Card(
                onClick = onNavigateToUrgency,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isUrgencyOn) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Urgência", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isUrgencyOn) "Ativado" else "Desativado",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isUrgencyOn) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Text(
                        text = "$keywordsCount palavras monitoradas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Active Now Live Status Screen
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🟢 Monitoramento em Tempo Real",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val activeContactsNow = contacts.filter { isContactActiveNow(it) }
                if (activeContactsNow.isEmpty()) {
                    Text(
                        text = "Não há nenhum contato prioritário com horário ativo no momento. O sistema tocará apenas para Alertas de Urgência de Palavras-Chave (caso configurado).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "Monitorando ativamente notificações recebidas de:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    activeContactsNow.forEach { contact ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "• ${getFriendlyHoursRange(contact)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==================== TAB 1: CONTATOS ====================
@Composable
fun ContactsTab(
    contacts: List<PriorityContact>,
    onEditContact: (PriorityContact) -> Unit,
    onToggleContactActive: (PriorityContact, Boolean) -> Unit,
    onAddContact: () -> Unit
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyContactsState(onAddContactClick = onAddContact)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Monitore pessoas importantes e defina regras de horários específicas.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(contacts, key = { it.id }) { contact ->
                ContactCard(
                    contact = contact,
                    onCardClick = { onEditContact(contact) },
                    onToggleActive = { isActive -> onToggleContactActive(contact, isActive) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Avoid covering by FAB
            }
        }
    }
}

// ==================== TAB 2: HISTÓRICO DE ALERTAS ====================
@Composable
fun HistoryTab(
    history: List<AlertHistory>,
    contacts: List<PriorityContact>
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Nenhum Alerta Disparado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "O histórico registrará os alertas de alta prioridade recebidos e disparados.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Toque em um item do histórico para abrir o aplicativo correspondente e responder à mensagem.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(history) { historyEntry ->
                HistoryCard(
                    historyEntry = historyEntry,
                    modifier = Modifier.clickable {
                        launchAppForContact(context, historyEntry, contacts)
                    }
                )
            }
        }
    }
}

// ==================== TAB 3: CONFIGURAÇÕES DE URGÊNCIA ====================
@Composable
fun UrgencyTab(
    onResetOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isEnabled by remember { mutableStateOf(UrgencySettings.isEnabled(context)) }
    var selectedIntensity by remember { mutableStateOf(UrgencySettings.getIntensity(context)) }
    var whoCanTrigger by remember { mutableStateOf(UrgencySettings.getWhoCanTrigger(context)) }
    
    // Manage keywords
    val keywordsList = remember { mutableStateListOf<String>().apply { addAll(UrgencySettings.getKeywords(context)) } }
    var newKeywordText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core urgence toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alerta de Urgência de Palavras",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dispara alertas mesmo se o celular estiver em Não Perturbe se alguma notificação contiver palavras de socorro.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        UrgencySettings.setEnabled(context, checked)
                        Toast.makeText(context, if (checked) "Urgência Ativada" else "Urgência Desativada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("urgency_global_switch")
                )
            }
        }

        if (isEnabled) {
            // Keywords addition
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Palavras-Chave Monitoradas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quando estas palavras forem detectadas em mensagens, o alarme tocará.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newKeywordText,
                            onValueChange = { newKeywordText = it },
                            placeholder = { Text("Ex: socorro, ajuda, emergência") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("keyword_input_field"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newKeywordText.isNotBlank()) {
                                    val newWord = newKeywordText.trim().lowercase()
                                    if (!keywordsList.contains(newWord)) {
                                        keywordsList.add(newWord)
                                        UrgencySettings.setKeywords(context, keywordsList.toSet())
                                    }
                                    newKeywordText = ""
                                }
                            })
                        )
                        IconButton(
                            onClick = {
                                if (newKeywordText.isNotBlank()) {
                                    val newWord = newKeywordText.trim().lowercase()
                                    if (!keywordsList.contains(newWord)) {
                                        keywordsList.add(newWord)
                                        UrgencySettings.setKeywords(context, keywordsList.toSet())
                                    }
                                    newKeywordText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar palavra")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Keywords list layout (Scrollable row)
                    if (keywordsList.isEmpty()) {
                        Text(
                            text = "Nenhuma palavra-chave cadastrada.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            keywordsList.forEach { keyword ->
                                KeywordChip(
                                    text = keyword,
                                    onDelete = {
                                        keywordsList.remove(keyword)
                                        UrgencySettings.setKeywords(context, keywordsList.toSet())
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Who can trigger
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quem pode ativar a Urgência?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Defina se qualquer pessoa que enviar a palavra-chave dispara o alarme ou apenas os contatos da lista de prioridade.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                whoCanTrigger = "ANYONE"
                                UrgencySettings.setWhoCanTrigger(context, "ANYONE")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (whoCanTrigger == "ANYONE") MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (whoCanTrigger == "ANYONE") Color.White 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Qualquer Pessoa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                whoCanTrigger = "ONLY_PRIORITY_CONTACTS"
                                UrgencySettings.setWhoCanTrigger(context, "ONLY_PRIORITY_CONTACTS")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (whoCanTrigger == "ONLY_PRIORITY_CONTACTS") MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (whoCanTrigger == "ONLY_PRIORITY_CONTACTS") Color.White 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apenas Contatos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Intensity level selection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intensidade do Alerta de Urgência",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Controla o padrão de vibração e som para alertas críticos.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("LOW" to "Fraca", "MEDIUM" to "Média", "HIGH" to "Forte").forEach { (level, label) ->
                            Button(
                                onClick = {
                                    selectedIntensity = level
                                    UrgencySettings.setIntensity(context, level)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedIntensity == level) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedIntensity == level) Color.White 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Maintenance section (Reset onboarding)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ajustes Gerais",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onResetOnboarding,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rever Tela de Boas-Vindas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==================== KEYWORD CHIP ====================
@Composable
fun KeywordChip(
    text: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = CardHelper.borderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Excluir palavra-chave",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

// ==================== COMPATIBILITY HELPERS & SUB-COMPONENTS ====================

@Composable
fun ServiceStatusBanner(
    isEnabled: Boolean,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isEnabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)

    val contentColor = if (isEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        border = CardHelper.borderStroke(1.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnabled) "Monitor Ativo" else "Monitor Inativo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isEnabled) "O app está monitorando notificações recebidas."
                    else "Toque para ativar o acesso às notificações do sistema.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            if (!isEnabled) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Configurar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: PriorityContact,
    onCardClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = contact.name.firstOrNull()?.uppercase() ?: "?"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initial Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (contact.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (contact.isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (contact.isActive) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Hour range and days
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Das ${contact.startTime} às ${contact.endTime}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Alert type friendly display
                val alertLabel = when (contact.alertType) {
                    "SOUND_VIB" -> "Som + Vibração"
                    "SOUND_ONLY" -> "Apenas Som"
                    else -> "Apenas Vibração"
                }

                Text(
                    text = "$alertLabel • Intensidade ${contact.intensity.lowercase()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = contact.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("contact_switch_${contact.id}")
            )
        }
    }
}

@Composable
fun HistoryCard(
    historyEntry: AlertHistory,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("dd/MM - HH:mm", Locale.getDefault()) }
    val formattedTime = remember(historyEntry.timestamp) { formatter.format(Date(historyEntry.timestamp)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardHelper.borderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = historyEntry.contactName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Capturado em: ${historyEntry.appName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${historyEntry.title}: ${historyEntry.content}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Toque para responder",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyContactsState(
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardHelper.borderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Nenhum Contato Prioritário",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Cadastre contatos de familiares e emergência para tocar alarme mesmo se seu aparelho estiver no modo silencioso.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = onAddContactClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cadastrar Contato", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyHistoryState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum alerta recente recebido.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// Friendly helpers for summary tab
private fun getFriendlyHoursRange(contact: PriorityContact): String {
    val startDesc = when (contact.startTime) {
        "00:00" -> "Meia-noite (00:00)"
        "12:00" -> "Meio-dia (12:00)"
        else -> contact.startTime
    }
    val endDesc = when (contact.endTime) {
        "00:00" -> "Meia-noite (00:00)"
        "12:00" -> "Meio-dia (12:00)"
        "23:59" -> "Fim do dia (23:59)"
        else -> contact.endTime
    }
    return "$startDesc às $endDesc"
}

private fun isContactActiveNow(contact: PriorityContact): Boolean {
    if (!contact.isActive) return false
    val calendar = Calendar.getInstance()
    val dayOfWeekCalendar = calendar.get(Calendar.DAY_OF_WEEK)
    val dayOfWeekIndex = when (dayOfWeekCalendar) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }
    val daysList = contact.daysOfWeek.split(",").map { it.trim() }
    if (!daysList.contains(dayOfWeekIndex.toString())) return false

    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val nowStr = sdf.format(calendar.time)
    
    val partsNow = nowStr.split(":")
    val partsStart = contact.startTime.split(":")
    val partsEnd = contact.endTime.split(":")
    
    if (partsNow.size != 2 || partsStart.size != 2 || partsEnd.size != 2) return false
    
    val nowMins = (partsNow[0].toIntOrNull() ?: 0) * 60 + (partsNow[1].toIntOrNull() ?: 0)
    val startMins = (partsStart[0].toIntOrNull() ?: 0) * 60 + (partsStart[1].toIntOrNull() ?: 0)
    val endMins = (partsEnd[0].toIntOrNull() ?: 0) * 60 + (partsEnd[1].toIntOrNull() ?: 0)
    
    return if (startMins <= endMins) {
        nowMins in startMins..endMins
    } else {
        nowMins >= startMins || nowMins <= endMins
    }
}

// Redirect and direct chat helper function
private fun launchAppForContact(context: Context, historyEntry: AlertHistory, contacts: List<PriorityContact>) {
    val contact = contacts.firstOrNull { 
        it.name.contains(historyEntry.contactName, ignoreCase = true) || 
        historyEntry.contactName.contains(it.name, ignoreCase = true) 
    }
    val phone = contact?.phone?.filter { it.isDigit() }
    val appNameLower = historyEntry.appName.lowercase()
    
    try {
        if (appNameLower.contains("whatsapp") && !phone.isNullOrBlank()) {
            val formattedPhone = if (phone.length <= 11) "55$phone" else phone
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else if (appNameLower.contains("whatsapp")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "WhatsApp não instalado.", Toast.LENGTH_SHORT).show()
            }
        } else if (appNameLower.contains("telegram")) {
            val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Telegram não instalado.", Toast.LENGTH_SHORT).show()
            }
        } else if (appNameLower.contains("instagram")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Instagram não instalado.", Toast.LENGTH_SHORT).show()
            }
        } else {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage("com.whatsapp") 
                ?: pm.getLaunchIntentForPackage("org.telegram.messenger")
                ?: pm.getLaunchIntentForPackage("com.instagram.android")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Alerta do aplicativo: ${historyEntry.appName}", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao abrir conversa: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

// Border Stroke Helper for Compatibility and Precision
object CardHelper {
    @Composable
    fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
        androidx.compose.foundation.BorderStroke(width, color)
}
