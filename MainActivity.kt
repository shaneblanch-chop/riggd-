package com.example.riggdup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RiggdUpApp() }
    }
}

private enum class Screen {
    Disclaimer, MainMenu, DistanceToLeadBlock, LeadLoad, TotalLoad,
    PurchaseHeadLoad, LeadBlockSlingLoad, Glossary
}

private val DarkBg = Color(0xFF111315)
private val Panel = Color(0xFF1B1F23)
private val ButtonDark = Color(0xFF2A3036)
private val Amber = Color(0xFFFFB000)
private val White = Color(0xFFF2F2F2)
private val Muted = Color(0xFFB8B8B8)

@Composable
fun RiggdUpApp() {
    var screen by remember { mutableStateOf(Screen.Disclaimer) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Amber,
            background = DarkBg,
            surface = Panel,
            onPrimary = Color.Black,
            onBackground = White,
            onSurface = White
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
            when (screen) {
                Screen.Disclaimer -> DisclaimerScreen(
                    onAccept = { screen = Screen.MainMenu },
                    onExit = { android.os.Process.killProcess(android.os.Process.myPid()) }
                )
                Screen.MainMenu -> MainMenuScreen(
                    onDistance = { screen = Screen.DistanceToLeadBlock },
                    onLeadLoad = { screen = Screen.LeadLoad },
                    onTotalLoad = { screen = Screen.TotalLoad },
                    onPurchaseHeadLoad = { screen = Screen.PurchaseHeadLoad },
                    onLeadBlockSlingLoad = { screen = Screen.LeadBlockSlingLoad },
                    onGlossary = { screen = Screen.Glossary }
                )
                Screen.DistanceToLeadBlock -> DistanceToLeadBlockScreen { screen = Screen.MainMenu }
                Screen.LeadLoad -> LeadLoadScreen { screen = Screen.MainMenu }
                Screen.TotalLoad -> TotalLoadScreen { screen = Screen.MainMenu }
                Screen.PurchaseHeadLoad -> PurchaseHeadLoadScreen { screen = Screen.MainMenu }
                Screen.LeadBlockSlingLoad -> LeadBlockSlingLoadScreen { screen = Screen.MainMenu }
                Screen.Glossary -> GlossaryScreen { screen = Screen.MainMenu }
            }
        }
    }
}

@Composable
fun DisclaimerScreen(onAccept: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("rigg’d up", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Amber)
        Text("Rigging Calculation Tool", fontSize = 18.sp, color = Muted)
        Spacer(Modifier.height(36.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Text(
                "This application is intended as a planning aid only.\n\n" +
                    "All lifting, rigging, and engineering calculations must be verified by a competent person prior to use.\n\n" +
                    "The user assumes full responsibility for all calculations and lifting operations.",
                modifier = Modifier.padding(20.dp), fontSize = 18.sp, color = White
            )
        }
        Spacer(Modifier.height(28.dp))
        IndustrialButton("ACCEPT", onAccept)
        Spacer(Modifier.height(12.dp))
        IndustrialButton("EXIT", onExit)
    }
}

@Composable
fun MainMenuScreen(
    onDistance: () -> Unit,
    onLeadLoad: () -> Unit,
    onTotalLoad: () -> Unit,
    onPurchaseHeadLoad: () -> Unit,
    onLeadBlockSlingLoad: () -> Unit,
    onGlossary: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("rigg’d up", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Amber)
        Spacer(Modifier.height(24.dp))
        IndustrialButton("Minimum Distance to Lead Block", onDistance)
        IndustrialButton("Lead Load", onLeadLoad)
        IndustrialButton("Total Load", onTotalLoad)
        IndustrialButton("Purchase Head Load", onPurchaseHeadLoad)
        IndustrialButton("Lead Block Sling Load", onLeadBlockSlingLoad)
        IndustrialButton("Glossary", onGlossary)
    }
}

@Composable
fun DistanceToLeadBlockScreen(onBack: () -> Unit) {
    var drumWidth by remember { mutableStateOf("") }
    var far by remember { mutableStateOf("12") }
    var result by remember { mutableStateOf<String?>(null) }
    CalculatorLayout("Minimum Distance to Lead Block", onBack) {
        NumberInput("Drum Width DW (metres)", drumWidth) { drumWidth = it }
        DropdownInput("Fleet Angle Ratio FAR", far, listOf("12", "19")) { far = it }
        IndustrialButton("CALCULATE") {
            val dw = drumWidth.toDoubleOrNull()
            val ratio = far.toDoubleOrNull()
            result = if (dw != null && ratio != null) formatValue(roundUp2((dw / 2.0) * ratio), "m") else "Invalid input"
        }
        ResultCard("Distance to Lead Block", result)
        WarningText("This is the minimum distance.")
    }
}

@Composable
fun LeadLoadScreen(onBack: () -> Unit) {
    var loadWeight by remember { mutableStateOf("") }
    var liftingGearWeight by remember { mutableStateOf("") }
    var pip by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    CalculatorLayout("Lead Load", onBack) {
        NumberInput("Load Weight (Ton)", loadWeight) { loadWeight = it }
        NumberInput("Lifting Gear Weight (Ton)", liftingGearWeight) { liftingGearWeight = it }
        NumberInput("Parts in Purchase PIP", pip) { pip = it }
        IndustrialButton("CALCULATE") {
            val lw = loadWeight.toDoubleOrNull()
            val gear = liftingGearWeight.toDoubleOrNull()
            val parts = pip.toDoubleOrNull()
            result = if (lw != null && gear != null && parts != null && parts > 0) {
                val tl = lw + gear
                val bl = tl / parts
                val nos = parts + 1
                val ll = bl + (bl * nos * 0.05)
                listOf(
                    "Total Load" to formatValue(roundUp2(tl), "Ton"),
                    "Becket Load" to formatValue(roundUp2(bl), "Ton"),
                    "Lead Load" to formatValue(roundUp2(ll), "Ton")
                )
            } else listOf("Error" to "Invalid input")
        }
        MultiResultCard(result)
        WarningText("Ensure all parts used are rated higher.")
    }
}

@Composable
fun TotalLoadScreen(onBack: () -> Unit) {
    var loadWeight by remember { mutableStateOf("") }
    var liftingGearWeight by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    CalculatorLayout("Total Load", onBack) {
        NumberInput("Load Weight (Ton)", loadWeight) { loadWeight = it }
        NumberInput("Lifting Gear Weight (Ton)", liftingGearWeight) { liftingGearWeight = it }
        IndustrialButton("CALCULATE") {
            val lw = loadWeight.toDoubleOrNull()
            val gear = liftingGearWeight.toDoubleOrNull()
            result = if (lw != null && gear != null) formatValue(roundUp2(lw + gear), "Ton") else "Invalid input"
        }
        ResultCard("Total Load", result)
    }
}

@Composable
fun PurchaseHeadLoadScreen(onBack: () -> Unit) {
    var totalLoad by remember { mutableStateOf("") }
    var leadLoad by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    CalculatorLayout("Purchase Head Load", onBack) {
        NumberInput("Total Load TL (Ton)", totalLoad) { totalLoad = it }
        NumberInput("Lead Load LL (Ton)", leadLoad) { leadLoad = it }
        IndustrialButton("CALCULATE") {
            val tl = totalLoad.toDoubleOrNull()
            val ll = leadLoad.toDoubleOrNull()
            result = if (tl != null && ll != null) formatValue(roundUp2(tl + ll), "Ton") else "Invalid input"
        }
        ResultCard("Purchase Head Load", result)
    }
}

@Composable
fun LeadBlockSlingLoadScreen(onBack: () -> Unit) {
    var leadLoad by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("60") }
    var result by remember { mutableStateOf<String?>(null) }
    CalculatorLayout("Lead Block Sling Load", onBack) {
        NumberInput("Lead Load LL (Ton)", leadLoad) { leadLoad = it }
        DropdownInput("Angle", angle, listOf("60", "90", "120")) { angle = it }
        IndustrialButton("CALCULATE") {
            val ll = leadLoad.toDoubleOrNull()
            val aa = when (angle) { "60" -> 1.73; "90" -> 1.41; "120" -> 1.0; else -> null }
            result = if (ll != null && aa != null) formatValue(roundUp2(ll * aa), "Ton") else "Invalid input"
        }
        ResultCard("Lead Block Support Sling Load", result)
        WarningText("Ensure all parts used are rated higher.")
    }
}

@Composable
fun GlossaryScreen(onBack: () -> Unit) {
    CalculatorLayout("Glossary", onBack) {
        listOf(
            "AA" to "Angle Allowance", "BL" to "Becket Load", "DLB" to "Distance to Lead Block",
            "DW" to "Drum Width", "FAR" to "Fleet Angle Ratio", "LBSS" to "Lead Block Support Sling Load",
            "LL" to "Lead Load", "NOS" to "Number of Sheaves", "PHL" to "Purchase Head Load",
            "PIP" to "Parts in Purchase", "TL" to "Total Load"
        ).forEach { (short, full) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Text("$short = $full", modifier = Modifier.padding(16.dp), fontSize = 18.sp, color = White)
            }
        }
    }
}

@Composable
fun CalculatorLayout(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Amber)
        Spacer(Modifier.height(16.dp))
        IndustrialButton("BACK", onBack)
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@Composable
fun NumberInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Amber, unfocusedBorderColor = Muted,
            focusedLabelColor = Amber, unfocusedLabelColor = Muted,
            focusedTextColor = White, unfocusedTextColor = White
        )
    )
}

@Composable
fun DropdownInput(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonDark),
            shape = RoundedCornerShape(10.dp)
        ) { Text("$label: $selected", fontSize = 18.sp, color = White) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
fun IndustrialButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(62.dp).padding(vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonDark),
        shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White) }
}

@Composable
fun ResultCard(label: String, result: String?) {
    if (result != null) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(label, fontSize = 18.sp, color = Muted)
                Text(result, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Amber)
            }
        }
    }
}

@Composable
fun MultiResultCard(results: List<Pair<String, String>>?) {
    if (results != null) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(modifier = Modifier.padding(20.dp)) {
                results.forEach { (label, value) ->
                    Text(label, fontSize = 18.sp, color = Muted)
                    Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Amber)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun WarningText(text: String) {
    Text(text = text, modifier = Modifier.padding(top = 16.dp), fontSize = 16.sp, color = Amber, fontWeight = FontWeight.Bold)
}

private fun roundUp2(value: Double): Double = ceil(value * 100.0) / 100.0
private fun formatValue(value: Double, unit: String): String = String.format("%.2f %s", value, unit)
