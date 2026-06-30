package com.example.ui

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.PriorityContact
import com.example.permissions.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contact: PriorityContact?, // Null if adding new
    onBack: () -> Unit,
    onSave: (PriorityContact) -> Unit,
    onDelete: (PriorityContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var startTime by remember { mutableStateOf(contact?.startTime ?: "00:00") }
    var endTime by remember { mutableStateOf(contact?.endTime ?: "23:59") }
    
    // Parse days
    val initialDays = contact?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(initialDays) } }

    var alertType by remember { mutableStateOf(contact?.alertType ?: "SOUND_VIB") } // SOUND_VIB, SOUND_ONLY, VIBRATE_ONLY
    var intensity by remember { mutableStateOf(contact?.intensity ?: "HIGH") } // HIGH, MEDIUM, LOW

    // Launcher to pick a contact from the device's address book
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            try {
                context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        
                        if (nameIndex >= 0) {
                            name = cursor.getString(nameIndex)
                        }
                        if (numberIndex >= 0) {
                            phone = cursor.getString(numberIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar contato. Verifique as permissões de contatos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val daysMap = mapOf(
        1 to "S",
        2 to "T",
        3 to "Q",
        4 to "Q",
        5 to "S",
        6 to "S",
        7 to "D"
    )

    val daysFullMap = mapOf(
        1 to "Seg",
        2 to "Ter",
        3 to "Qua",
        4 to "Qui",
        5 to "Sex",
        6 to "Sáb",
        7 to "Dom"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (contact == null) "Novo Alerta Prioritário" else "Editar Alerta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (contact != null) {
                        IconButton(onClick = { onDelete(contact) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Regra",
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
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // CARD 1: Contact Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Contato Prioritário",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Contato") },
                        placeholder = { Text("Ex: João Silva") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("contact_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Número (Opcional)") },
                        placeholder = { Text("Ex: +55 11 99999-9999") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Button(
                        onClick = {
                            if (PermissionHelper.hasContactsPermission(context)) {
                                val intent = Intent(
                                    Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                )
                                contactPickerLauncher.launch(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Acesso à agenda não concedido. Conceda a permissão nas configurações.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar na Agenda Telefônica", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // CARD 2: Active Schedule Range
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Horário de Funcionamento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "O aplicativo irá alarmar apenas se a mensagem do contato chegar dentro do período configurado abaixo.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Início") },
                            placeholder = { Text("00:00") },
                            modifier = Modifier.weight(1f).testTag("start_time_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Fim") },
                            placeholder = { Text("23:59") },
                            modifier = Modifier.weight(1f).testTag("end_time_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            // CARD 3: Active Days
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dias Ativos na Semana",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 1..7) {
                            val isSelected = selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            if (selectedDays.size > 1) {
                                                selectedDays.remove(day)
                                            } else {
                                                Toast.makeText(context, "Selecione ao menos 1 dia.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            selectedDays.add(day)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysMap[day] ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Display friendly active days list
                    val sortedDays = selectedDays.sorted()
                    val daysText = if (sortedDays.size == 7) "Todos os dias"
                    else if (sortedDays.size == 5 && sortedDays.containsAll(listOf(1,2,3,4,5))) "Segunda a Sexta"
                    else sortedDays.joinToString(", ") { daysFullMap[it] ?: "" }

                    Text(
                        text = "Ativo em: $daysText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // CARD 4: Alarm Configurations
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Configurações de Feedback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Alert Type Selector
                    Text("Tipo de Alerta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            "SOUND_VIB" to "Som + Vibração",
                            "SOUND_ONLY" to "Apenas Som",
                            "VIBRATE_ONLY" to "Apenas Vibração"
                        )
                        types.forEach { (typeKey, typeLabel) ->
                            val isSelected = alertType == typeKey
                            Button(
                                onClick = { alertType = typeKey },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(typeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert Intensity Selector
                    Text("Intensidade do Alerta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intensities = listOf(
                            "HIGH" to "Alta",
                            "MEDIUM" to "Média",
                            "LOW" to "Baixa"
                        )
                        intensities.forEach { (intKey, intLabel) ->
                            val isSelected = intensity == intKey
                            Button(
                                onClick = { intensity = intKey },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(intLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 5: SAVE ACTION
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Por favor, defina o nome do contato.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!startTime.matches(Regex("\\d{2}:\\d{2}")) || !endTime.matches(Regex("\\d{2}:\\d{2}"))) {
                        Toast.makeText(context, "Insira os horários no formato correto: HH:MM (ex: 07:00).", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    
                    val sortedDays = selectedDays.sorted()
                    val daysCsv = sortedDays.joinToString(",")

                    val savedContact = PriorityContact(
                        id = contact?.id ?: 0,
                        name = name.trim(),
                        phone = if (phone.isBlank()) null else phone.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        daysOfWeek = daysCsv,
                        alertType = alertType,
                        intensity = intensity,
                        isActive = contact?.isActive ?: true
                    )
                    
                    onSave(savedContact)
                    Toast.makeText(context, "Regra de prioridade salva com sucesso!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_contact_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Alerta Prioritário", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
