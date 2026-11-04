package com.traceless.privacy.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.traceless.privacy.data.LocalPreferences
import com.traceless.privacy.domain.DiagnosticResult
import com.traceless.privacy.domain.DiagnosticStatus
import com.traceless.privacy.domain.DeviceSnapshot
import com.traceless.privacy.domain.LocalDiagnosticEngine
import com.traceless.privacy.domain.OnlineCheckResult
import com.traceless.privacy.domain.OnlineNetworkCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF07111F)
private val Panel = Color(0xFF101F32)
private val PanelLight = Color(0xFF172B43)
private val ElectricBlue = Color(0xFF5AA7FF)
private val Turquoise = Color(0xFF43E0CF)
private val Muted = Color(0xFFA8B8CC)

@Composable
fun TraceLessApp() {
    val context = LocalContext.current
    val store = remember { LocalPreferences(context.applicationContext) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    val snapshot = remember(refresh) { LocalDiagnosticEngine.scan(context.applicationContext) }

    LaunchedEffect(snapshot.scanTime) { store.saveLastScan(snapshot.scanTime) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Turquoise,
            secondary = ElectricBlue,
            background = Night,
            surface = Panel,
            surfaceVariant = PanelLight,
            onPrimary = Night,
            onBackground = Color(0xFFF3F7FC),
            onSurface = Color(0xFFF3F7FC),
            onSurfaceVariant = Muted
        )
    ) {
        Scaffold(
            containerColor = Night,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Color.White) {
                    val entries = listOf(
                        Triple("Accueil", Icons.Default.Dashboard, 0),
                        Triple("Diagnostic", Icons.Default.Shield, 1),
                        Triple("Réseau", Icons.Default.Wifi, 2),
                        Triple("Rapports", Icons.Default.Assessment, 3)
                    )
                    entries.forEach { entry ->
                        NavigationBarItem(
                            selected = selectedTab == entry.third,
                            onClick = { selectedTab = entry.third },
                            icon = { Icon(entry.second, contentDescription = entry.first) },
                            label = { Text(entry.first, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Turquoise,
                                selectedTextColor = Turquoise,
                                indicatorColor = PanelLight,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> OverviewScreen(
                    Modifier.padding(padding), snapshot,
                    onScan = { refresh++ },
                    onDiagnostics = { selectedTab = 1 },
                    onNetwork = { selectedTab = 2 }
                )
                1 -> DiagnosticScreen(Modifier.padding(padding), snapshot, onScan = { refresh++ })
                2 -> NetworkScreen(Modifier.padding(padding))
                else -> ReportsScreen(Modifier.padding(padding), store, snapshot)
            }
        }
    }
}

@Composable
private fun OverviewScreen(
    modifier: Modifier,
    snapshot: DeviceSnapshot,
    onScan: () -> Unit,
    onDiagnostics: () -> Unit,
    onNetwork: () -> Unit
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(15.dp), color = PanelLight) {
                    Icon(Icons.Default.Shield, null, tint = Turquoise, modifier = Modifier.padding(12.dp).size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("TRACELESS", color = Turquoise, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                    Text("Mobile Security Suite", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = Color(0xFF123A3B)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(Turquoise, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("LOCAL", color = Turquoise, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text("Votre appareil.\nVotre contrôle.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Diagnostic de confidentialité et de sécurité Android.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("ÉTAT DU DIAGNOSTIC", color = Turquoise, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(snapshot.deviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(snapshot.androidVersion, color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(shape = CircleShape, color = PanelLight) {
                            Icon(Icons.Default.Security, null, tint = ElectricBlue, modifier = Modifier.padding(14.dp).size(28.dp))
                        }
                    }
                    HorizontalDivider(color = PanelLight)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile("Conformes", snapshot.goodCount.toString(), Turquoise, Modifier.weight(1f))
                        MetricTile("À examiner", snapshot.warningCount.toString(), Color(0xFFFFC16B), Modifier.weight(1f))
                        MetricTile("Contrôlés", snapshot.evaluatedCount.toString(), ElectricBlue, Modifier.weight(1f))
                    }
                    Text("Ces indicateurs résument les contrôles accessibles à Android. Ils ne constituent pas une certification de sécurité.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onScan, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Relancer le diagnostic local")
                    }
                }
            }
        }
        item {
            Text("CENTRE DE CONTRÔLE", color = Muted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        item {
            ActionCard("Diagnostic de l’appareil", "Verrouillage, chiffrement, DNS, correctifs et débogage.", Icons.Default.Shield, onDiagnostics)
        }
        item {
            ActionCard("Analyse réseau en ligne", "Adresse IP publique et liaison TLS du service de contrôle.", Icons.Default.Language, onNetwork)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF14283A)), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lock, null, tint = Turquoise)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Confidentialité par conception", fontWeight = FontWeight.Bold)
                        Text("Le diagnostic local fonctionne sans Internet. Le contrôle en ligne est séparé et ne démarre qu’à votre demande.",
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = PanelLight) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Turquoise, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable
private fun DiagnosticScreen(modifier: Modifier, snapshot: DeviceSnapshot, onScan: () -> Unit) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Diagnostic local", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Ces contrôles interrogent les informations que le système Android rend accessibles à TraceLess.",
                color = Muted)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${snapshot.results.size} contrôles", color = Muted, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onScan) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Actualiser")
                }
            }
        }
        items(snapshot.results, key = { it.id }) { result -> DiagnosticCard(result) }
        item {
            Text("Limites : Android et certains fabricants masquent volontairement des réglages. « Indisponible » ne signifie pas « dangereux ».",
                color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DiagnosticCard(result: DiagnosticResult) {
    val color = when (result.status) {
        DiagnosticStatus.GOOD -> Turquoise
        DiagnosticStatus.WARNING -> Color(0xFFFFC16B)
        DiagnosticStatus.INFO -> ElectricBlue
        DiagnosticStatus.UNAVAILABLE -> Muted
    }
    val label = when (result.status) {
        DiagnosticStatus.GOOD -> "CONFORME"
        DiagnosticStatus.WARNING -> "À EXAMINER"
        DiagnosticStatus.INFO -> "INFORMATION"
        DiagnosticStatus.UNAVAILABLE -> "INDISPONIBLE"
    }
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(result.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(30.dp), color = color.copy(alpha = 0.14f)) {
                    Text(label, color = color, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Text(result.value, color = color, fontWeight = FontWeight.SemiBold)
            Text(result.explanation, color = Muted, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = PanelLight)
            Text("À faire", color = Turquoise, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(result.recommendation, style = MaterialTheme.typography.bodyMedium)
            val action = settingsActionFor(result.id)
            if (action != null) {
                OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ouvrir les paramètres Android")
                }
            }
        }
    }
}

@Composable
private fun settingsActionFor(id: String): (() -> Unit)? {
    val context = LocalContext.current
    val intent = when (id) {
        "screen_lock", "encryption" -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        "patch" -> Intent(Settings.ACTION_SETTINGS)
        "private_dns" -> Intent("android.settings.PRIVATE_DNS_SETTINGS")
        "adb", "developer_options" -> Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        else -> null
    } ?: return null
    return {
        val resolved = if (intent.resolveActivity(context.packageManager) != null) intent
            else Intent(Settings.ACTION_SETTINGS)
        runCatching { context.startActivity(resolved) }
    }
}

@Composable
private fun NetworkScreen(modifier: Modifier) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<OnlineCheckResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Analyse réseau", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Un contrôle à la demande, indépendant du diagnostic local.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Public, null, tint = ElectricBlue, modifier = Modifier.size(34.dp))
                    Text("Vérifier ma connexion publique", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("TraceLess contacte Cloudflare via HTTPS pour lire l’adresse IP publique visible par ce service, son point d’accès réseau et la version TLS annoncée.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    Text("Confidentialité : cette requête révèle votre adresse IP publique à Cloudflare. Elle n’est pas enregistrée dans l’historique local par TraceLess.",
                        color = Color(0xFFFFC16B), style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            loading = true
                            error = null
                            result = null
                            scope.launch {
                                try {
                                    result = withContext(Dispatchers.IO) { OnlineNetworkCheck.run() }
                                } catch (e: Exception) {
                                    error = e.message ?: "Connexion impossible. Vérifiez Internet puis réessayez."
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Vérification en cours…")
                        } else {
                            Icon(Icons.Default.WifiFind, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Lancer le contrôle en ligne")
                        }
                    }
                }
            }
        }
        if (error != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF38252A))) {
                    Text(error ?: "", modifier = Modifier.padding(16.dp), color = Color(0xFFFFB6B6))
                }
            }
        }
        if (result != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("RÉSULTAT DU CONTRÔLE", color = Turquoise, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        ResultLine("IP publique", result!!.publicIp)
                        ResultLine("Point d’accès Cloudflare", result!!.edge)
                        ResultLine("TLS observé", result!!.tls)
                        HorizontalDivider(color = PanelLight)
                        Text("Test effectué le " + SimpleDateFormat("dd/MM/yyyy à HH:mm:ss", Locale.getDefault()).format(Date(result!!.checkedAt)),
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Text("Ce test ne prouve pas l’anonymat, ne certifie pas un VPN et ne détermine pas à lui seul le résolveur DNS réellement utilisé par toutes les applications.",
                color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ResultLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReportsScreen(modifier: Modifier, store: LocalPreferences, snapshot: DeviceSnapshot) {
    val dateText = if (store.lastScan() == 0L) "Aucun diagnostic enregistré" else
        SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault()).format(Date(store.lastScan()))
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Rapports & confidentialité", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dernier diagnostic local", color = Turquoise, fontWeight = FontWeight.Bold)
                    Text(dateText, style = MaterialTheme.typography.titleMedium)
                    Text("${snapshot.goodCount} contrôles conformes · ${snapshot.warningCount} à examiner · ${snapshot.results.size - snapshot.evaluatedCount} indisponibles",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, null, tint = Turquoise)
                    Text("Données conservées sur l’appareil", fontWeight = FontWeight.Bold)
                    Text("Les dates de diagnostic sont stockées dans les préférences privées de TraceLess. Le contrôle réseau affiche son résultat pendant la session et ne sauvegarde pas l’adresse IP.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    Text("Aucun rapport n’est envoyé automatiquement.", color = Turquoise, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Text("La présence de la permission Internet sert uniquement à la fonction réseau déclenchée manuellement. Les diagnostics locaux restent utilisables hors connexion.",
                color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
