package com.example.calculimc

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculIMCApp()
        }
    }
}

// Couleurs pour le thème clair (blanc)
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0D47A1),
    secondary = Color(0xFF2196F3),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Couleurs pour le thème sombre (noir)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF64B5F6),
    background = Color.Black,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalculIMCApp() {
    var isDarkTheme by remember { mutableStateOf(false) }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme
    ) {
        CalculIMCContent(isDarkTheme, onThemeChange = { isDarkTheme = it })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculIMCContent(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {

    // --- Variables d'état ---
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sexe by remember { mutableStateOf("Homme") }
    var poids by remember { mutableStateOf(70) }
    var taille by remember { mutableStateOf("") }
    var typeActivite by remember { mutableStateOf("Sédentaire") }
    var expanded by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var imcResult by remember { mutableStateOf(0f) }
    var categorieResult by remember { mutableStateOf("") }

    // --- Messages d'erreur ---
    var errorNom by remember { mutableStateOf("") }
    var errorPrenom by remember { mutableStateOf("") }
    var errorDate by remember { mutableStateOf("") }
    var errorTaille by remember { mutableStateOf("") }

    val types = listOf("Sédentaire", "Faible", "Actif", "Sportif", "Athlète")
    val context = LocalContext.current

    // DatePicker pour choisir la date de naissance
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            if (selectedDate.isAfter(LocalDate.now())) {
                errorDate = "Date impossible dans le futur"
            } else {
                dateNaissance = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                age = calculerAge(dateNaissance)
                errorDate = ""
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showResult) {
        // --- Page résultat avec message personnalisé ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Message personnalisé avec nom et prénom
            Text(
                text = "Bonjour ${prenom.trim()} ${nom.trim()} !",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Voici votre résultat IMC",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Carte de résultat
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Votre IMC",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "%.1f".format(imcResult),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Affichage de la catégorie avec couleur selon le résultat
                    val (categorieColor, message) = when {
                        imcResult < 18.5 -> Pair(Color(0xFF2196F3), "Maigreur")
                        imcResult < 25 -> Pair(Color(0xFF4CAF50), "Poids normal")
                        imcResult < 30 -> Pair(Color(0xFFFF9800), "Surpoids")
                        else -> Pair(Color(0xFFF44336), "Obésité")
                    }

                    Text(
                        text = message,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = categorieColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message d'interprétation
                    val interpretation = when {
                        imcResult < 18.5 -> "Vous êtes en dessous de votre poids idéal"
                        imcResult < 25 -> "Vous avez un poids santé"
                        imcResult < 30 -> "Vous êtes au-dessus de votre poids idéal"
                        else -> "Consultez un professionnel de santé"
                    }

                    Text(
                        text = interpretation,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { showResult = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Nouveau calcul",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp
                )
            }
        }
    } else {
        // --- Page formulaire ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Header avec switch thème ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculateur IMC",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDarkTheme) "🌙" else "☀️",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // --- Nom ---
            TextField(
                value = nom,
                onValueChange = {
                    nom = it
                    errorNom = when {
                        it.length < 3 -> "Minimum 3 caractères"
                        it.any { c -> c.isDigit() } -> "Pas de chiffres autorisés"
                        else -> ""
                    }
                },
                label = { Text("Nom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    unfocusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            if (errorNom.isNotEmpty()) Text(errorNom, color = Color.Red, fontSize = 12.sp)

            // --- Prénom ---
            TextField(
                value = prenom,
                onValueChange = {
                    prenom = it
                    errorPrenom = when {
                        it.length < 3 -> "Minimum 3 caractères"
                        it.any { c -> c.isDigit() } -> "Pas de chiffres autorisés"
                        else -> ""
                    }
                },
                label = { Text("Prénom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    unfocusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            if (errorPrenom.isNotEmpty()) Text(errorPrenom, color = Color.Red, fontSize = 12.sp)

            // --- Date de naissance ---
            Button(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
                )
            ) {
                Text(
                    if (dateNaissance.isEmpty()) "Choisir la date de naissance" else dateNaissance,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (errorDate.isNotEmpty()) Text(errorDate, color = Color.Red, fontSize = 12.sp)
            Text("Âge : $age ans", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

            // --- Sexe ---
            Text("Sexe :", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("Homme", "Femme").forEach { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = sexe == s,
                            onClick = { sexe = s },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = s,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 4.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // --- Poids ---
            Text("Poids : $poids kg", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { if (poids > 0) poids-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
                    )
                ) {
                    Text("-", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = { poids++ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
                    )
                ) {
                    Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // --- Taille (en cm) ---
            TextField(
                value = taille,
                onValueChange = {
                    // Filtrer pour n'autoriser que les chiffres
                    val filtered = it.filter { char -> char.isDigit() }
                    taille = filtered
                    errorTaille = if (filtered.isBlank() || filtered.toIntOrNull() == null || filtered.toInt() <= 0) {
                        "Taille invalide"
                    } else if (filtered.toInt() > 250) {
                        "Taille trop grande (max 250 cm)"
                    } else if (filtered.toInt() < 50) {
                        "Taille trop petite (min 50 cm)"
                    } else ""
                },
                label = { Text("Taille (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    unfocusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            if (errorTaille.isNotEmpty()) Text(errorTaille, color = Color.Red, fontSize = 12.sp)

            // --- Type activité ---
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = typeActivite,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type d'activité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                            unfocusedContainerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = MaterialTheme.colorScheme.onBackground) },
                                onClick = {
                                    typeActivite = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // --- Bouton Calculer ---
            Button(
                onClick = {
                    val t = taille.toIntOrNull()
                    val valide = nom.isNotBlank() && prenom.isNotBlank() && dateNaissance.isNotBlank() &&
                            t != null && t in 50..250 && errorNom.isEmpty() && errorPrenom.isEmpty() && errorDate.isEmpty() && errorTaille.isEmpty()

                    if (valide) {
                        // Convertir la taille de cm en mètres pour le calcul IMC
                        val tailleMetres = t.toFloat() / 100
                        val imc = poids / (tailleMetres * tailleMetres)
                        imcResult = imc
                        categorieResult = when {
                            imc < 18.5 -> "Maigreur"
                            imc in 18.5..24.9 -> "Normal"
                            imc in 25.0..29.9 -> "Surpoids"
                            else -> "Obésité"
                        }
                        showResult = true
                    } else {
                        if (nom.isBlank()) errorNom = "Nom obligatoire"
                        if (prenom.isBlank()) errorPrenom = "Prénom obligatoire"
                        if (dateNaissance.isBlank()) errorDate = "Date obligatoire"
                        if (t == null || t !in 50..250) errorTaille = "Taille invalide (50-250 cm)"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("Calculer IMC", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
            }
        }
    }
}

// --- Fonction calcul âge ---
@RequiresApi(Build.VERSION_CODES.O)
fun calculerAge(dateNaissance: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val birthDate = LocalDate.parse(dateNaissance, formatter)
        val now = LocalDate.now()
        ChronoUnit.YEARS.between(birthDate, now).toString()
    } catch (e: Exception) {
        ""
    }
}