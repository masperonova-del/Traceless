# TraceLess Android 1.1

Application Android de diagnostic local de confidentialité et de sécurité, avec un module réseau en ligne déclenché à la demande.

## Principes de conception
- Les diagnostics locaux restent disponibles hors connexion.
- Le contrôle en ligne est séparé et ne démarre qu'après une action explicite.
- Aucun compte utilisateur ni envoi automatique de rapport.
- Les dates de diagnostic sont conservées localement.
- Chaque contrôle distingue un état positif, une alerte, une information et une donnée indisponible.
- Les limites des API Android et des fabricants sont indiquées au lieu d'inventer un résultat.

## Fonctions de cette version

### Diagnostic local
- État du verrouillage sécurisé de l'écran.
- Statut de chiffrement du stockage communiqué par Android.
- Date du correctif de sécurité Android.
- Lecture du mode DNS privé lorsque le système l'autorise.
- État du débogage USB et des options développeur.
- Type de connexion active, validation Internet Android et présence d'un transport VPN.

### Contrôle réseau en ligne, sur demande
- Requête HTTPS vers le point de diagnostic public de Cloudflare.
- Adresse IP publique visible par ce service.
- Point d'accès Cloudflare et version TLS communiquée.
- Résultat non sauvegardé dans l'historique local.

**Information importante :** le contrôle en ligne révèle l'adresse IP publique à Cloudflare. Il ne certifie pas l'anonymat, ne valide pas la qualité d'un VPN et ne détermine pas à lui seul le résolveur DNS utilisé par chaque application.

## Données et permissions
TraceLess demande la permission INTERNET pour le contrôle en ligne explicite et ACCESS_NETWORK_STATE pour lire l'état de la connexion Android. Les diagnostics locaux n'ont pas besoin d'Internet. Les rapports ne sont pas envoyés automatiquement et l'adresse IP du test n'est pas enregistrée par TraceLess.

Les préférences et la date du dernier diagnostic sont conservées dans le stockage privé de l'application.

## Limites assumées
Cette version n'est pas un antivirus et ne détecte pas tous les logiciels espions. Elle n'inspecte pas le contenu HTTPS des autres applications, ne bloque pas encore les domaines et ne lit pas librement toutes les permissions de toutes les applications. L'adresse MAC aléatoire est propre à chaque réseau Wi-Fi et n'est pas présentée comme automatiquement vérifiée.

Une protection anti-traçage locale basée sur VpnService nécessitera une phase indépendante, une autorisation Android explicite, des listes de filtrage vérifiables et des tests sur appareils réels.

## Configuration technique
- Kotlin 2.0.21
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Java 17
- Jetpack Compose
- compileSdk / targetSdk 35
- minSdk 26

## Compilation
Le workflow GitHub Actions android.yml compile l'APK debug à chaque push sur main ou manuellement depuis l'onglet Actions. L'artefact s'appelle TraceLess-debug-apk.

Une compilation réussie confirme que le projet compile ; elle ne remplace pas les tests fonctionnels sur différents téléphones et versions Android.
