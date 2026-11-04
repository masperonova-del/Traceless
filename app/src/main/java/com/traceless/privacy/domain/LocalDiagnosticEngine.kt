package com.traceless.privacy.domain

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

enum class DiagnosticStatus { GOOD, WARNING, INFO, UNAVAILABLE }

data class DiagnosticResult(
    val id: String,
    val title: String,
    val status: DiagnosticStatus,
    val value: String,
    val explanation: String,
    val recommendation: String
)

data class DeviceSnapshot(
    val deviceName: String,
    val androidVersion: String,
    val results: List<DiagnosticResult>,
    val scanTime: Long = System.currentTimeMillis()
) {
    val evaluatedCount: Int get() = results.count { it.status != DiagnosticStatus.UNAVAILABLE }
    val goodCount: Int get() = results.count { it.status == DiagnosticStatus.GOOD }
    val warningCount: Int get() = results.count { it.status == DiagnosticStatus.WARNING }
}

object LocalDiagnosticEngine {
    fun scan(context: Context): DeviceSnapshot {
        val results = listOf(
            checkScreenLock(context),
            checkEncryption(context),
            checkSecurityPatch(),
            checkPrivateDns(context),
            checkAdb(context),
            checkDeveloperOptions(context),
            checkNetwork(context)
        )
        return DeviceSnapshot(
            deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}".trim(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            results = results
        )
    }

    private fun checkScreenLock(context: Context): DiagnosticResult {
        val manager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val secure = runCatching { manager?.isDeviceSecure == true }.getOrDefault(false)
        return DiagnosticResult(
            "screen_lock", "Verrouillage de l’écran",
            if (secure) DiagnosticStatus.GOOD else DiagnosticStatus.WARNING,
            if (secure) "Verrouillage sécurisé détecté" else "Aucun verrouillage sécurisé détecté",
            if (secure) "Android confirme la présence d’un code, schéma ou mot de passe sécurisé."
            else "Android ne confirme pas la présence d’un verrouillage sécurisé.",
            "Utilisez un code difficile à deviner et un délai de verrouillage court."
        )
    }

    private fun checkEncryption(context: Context): DiagnosticResult {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val state = runCatching { manager?.storageEncryptionStatus }.getOrNull()
        return when (state) {
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER ->
                DiagnosticResult("encryption", "Chiffrement du stockage", DiagnosticStatus.GOOD,
                    "Chiffrement actif selon Android",
                    "Le système signale que le stockage de l’appareil est chiffré.",
                    "Gardez Android et le verrouillage de l’écran à jour.")
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE ->
                DiagnosticResult("encryption", "Chiffrement du stockage", DiagnosticStatus.WARNING,
                    "Chiffrement non actif selon Android",
                    "Le statut système ne confirme pas le chiffrement actif.",
                    "Vérifiez les paramètres de sécurité et la documentation du fabricant.")
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED ->
                DiagnosticResult("encryption", "Chiffrement du stockage", DiagnosticStatus.WARNING,
                    "Non pris en charge selon Android",
                    "Le système indique que le chiffrement du stockage n’est pas pris en charge.",
                    "Consultez le fabricant de l’appareil.")
            else ->
                DiagnosticResult("encryption", "Chiffrement du stockage", DiagnosticStatus.UNAVAILABLE,
                    "État indisponible",
                    "Android ou le fabricant ne fournit pas un état exploitable à cette application.",
                    "Vérifiez cette information dans les paramètres de sécurité.")
        }
    }

    private fun checkSecurityPatch(): DiagnosticResult {
        val patch = Build.VERSION.SECURITY_PATCH
        if (patch.isNullOrBlank()) return DiagnosticResult(
            "patch", "Correctif de sécurité", DiagnosticStatus.UNAVAILABLE,
            "Date non communiquée", "Le système ne communique pas de date de correctif.",
            "Vérifiez manuellement les mises à jour Android."
        )
        return try {
            val days = ChronoUnit.DAYS.between(LocalDate.parse(patch), LocalDate.now())
            val status = when {
                days < 0 -> DiagnosticStatus.INFO
                days <= 120 -> DiagnosticStatus.GOOD
                else -> DiagnosticStatus.WARNING
            }
            val value = when {
                days < 0 -> "Date inhabituelle : $patch"
                days <= 120 -> "Correctif du $patch"
                else -> "Correctif du $patch · environ $days jours"
            }
            DiagnosticResult("patch", "Correctif de sécurité", status, value,
                "Date annoncée par Android : $patch.",
                if (days > 120) "Recherchez une mise à jour système. Le calendrier dépend du fabricant."
                else "Installez régulièrement les mises à jour proposées.")
        } catch (_: DateTimeParseException) {
            DiagnosticResult("patch", "Correctif de sécurité", DiagnosticStatus.UNAVAILABLE,
                "Date illisible", "Le format fourni par Android n’a pas pu être interprété.",
                "Vérifiez la date dans les paramètres système.")
        }
    }

    private fun checkPrivateDns(context: Context): DiagnosticResult {
        val mode = runCatching {
            Settings.Global.getString(context.contentResolver, "private_dns_mode")
        }.getOrNull()
        val host = runCatching {
            Settings.Global.getString(context.contentResolver, "private_dns_specifier")
        }.getOrNull()
        return when (mode?.lowercase()) {
            "off" -> DiagnosticResult("private_dns", "DNS privé", DiagnosticStatus.WARNING,
                "Désactivé selon Android",
                "Le mode DNS privé du système est désactivé.",
                "Envisagez un fournisseur DNS privé fiable dans les paramètres Android.")
            "hostname" -> if (!host.isNullOrBlank()) DiagnosticResult(
                "private_dns", "DNS privé", DiagnosticStatus.GOOD,
                "Fournisseur personnalisé configuré",
                "Android signale un fournisseur DNS privé : $host.",
                "Vérifiez que vous faites confiance à ce fournisseur."
            ) else DiagnosticResult("private_dns", "DNS privé", DiagnosticStatus.INFO,
                "Mode personnalisé détecté", "Le fournisseur n’est pas lisible.",
                "Vérifiez le nom du fournisseur dans les paramètres DNS privés.")
            "opportunistic" -> DiagnosticResult("private_dns", "DNS privé", DiagnosticStatus.INFO,
                "Mode automatique",
                "Android essaie d’utiliser le DNS chiffré lorsque le serveur le permet, avec possibilité de repli.",
                "Vérifiez les options DNS privées pour choisir une configuration stricte.")
            null, "" -> DiagnosticResult("private_dns", "DNS privé", DiagnosticStatus.UNAVAILABLE,
                "Lecture indisponible",
                "Ce fabricant ou cette version Android ne permet pas de lire ce réglage.",
                "Vérifiez le DNS privé dans les paramètres réseau.")
            else -> DiagnosticResult("private_dns", "DNS privé", DiagnosticStatus.INFO,
                "Mode signalé : $mode", "Valeur système reçue, sans interprétation certaine.",
                "Vérifiez la configuration dans les paramètres Android.")
        }
    }

    private fun checkAdb(context: Context): DiagnosticResult {
        val enabled = runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        }.getOrDefault(false)
        return DiagnosticResult("adb", "Débogage USB",
            if (enabled) DiagnosticStatus.WARNING else DiagnosticStatus.GOOD,
            if (enabled) "Activé" else "Désactivé",
            if (enabled) "Le débogage USB est activé ; il peut autoriser des interactions avancées lorsqu’un ordinateur est approuvé."
            else "Android indique que le débogage USB est désactivé.",
            if (enabled) "Désactivez-le lorsque vous n’en avez pas besoin et retirez les autorisations USB inconnues."
            else "Laissez-le désactivé si vous ne développez pas d’applications.")
    }

    private fun checkDeveloperOptions(context: Context): DiagnosticResult {
        val enabled = runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        }.getOrDefault(false)
        return DiagnosticResult("developer_options", "Options développeur", DiagnosticStatus.INFO,
            if (enabled) "Activées" else "Désactivées",
            "État des options développeur communiqué par Android.",
            "Leur activation n’est pas une faille en soi ; désactivez les fonctions de débogage inutiles.")
    }

    private fun checkNetwork(context: Context): DiagnosticResult {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val caps = runCatching {
            manager?.activeNetwork?.let { manager.getNetworkCapabilities(it) }
        }.getOrNull()
        if (caps == null) return DiagnosticResult("connection", "Connexion active",
            DiagnosticStatus.INFO, "Aucun réseau actif détecté",
            "Android ne signale aucune connexion réseau active.",
            "Connectez-vous à un réseau pour lancer le contrôle en ligne.")
        val vpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        val type = when {
            vpn -> "VPN Android actif"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Réseau mobile"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Réseau détecté"
        }
        val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return DiagnosticResult("connection", "Connexion active", DiagnosticStatus.INFO,
            if (validated) "$type · Internet validé" else "$type · Internet non confirmé",
            "Type de connexion et validation fournis par Android. Cela ne certifie ni le réseau ni l’anonymat.",
            if (vpn) "Android signale un VPN, sans vérifier son fournisseur."
            else "Sur un réseau public, évitez les opérations sensibles si vous ne lui faites pas confiance.")
    }
}