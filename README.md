# TraceLess Android 1.0 — base fonctionnelle

Application Android locale d'aide à la confidentialité et à la sécurité.

## Principes
- Pas de compte utilisateur.
- Pas de permission INTERNET.
- Pas de collecte ni d'envoi de données.
- Préférences enregistrées localement.
- Les contrôles manuels sont clairement distingués des détections automatiques.

## Fonctionnalités incluses
- Tableau de bord.
- Progression du guide (ce n'est pas une note de sécurité).
- Checklist confidentialité : adresse MAC aléatoire, DNS privé, données du navigateur.
- Checklist sécurité : verrouillage, autorisations, mises à jour.
- Raccourcis vers les paramètres Android.
- Espace anti-traçage qui expose explicitement ce qui n'est pas encore implémenté.
- Enregistrement local de la date du bilan.
- Workflow GitHub Actions pour produire un APK debug.

## Limites connues
Cette version ne détecte pas automatiquement les trackers, n'inspecte pas le trafic réseau, ne bloque pas les domaines, ne lit pas les permissions de toutes les applications et ne garantit pas l'anonymat.
Le module anti-traçage avancé nécessitera une implémentation et des tests dédiés de `VpnService`, avec consentement explicite.

## Configuration
- Kotlin 2.0.21
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Java 17
- Compose BOM 2024.12.01
- compileSdk / targetSdk 35
- minSdk 26

## Compilation sur GitHub
Le workflow `.github/workflows/android.yml` compile l'APK à chaque push sur `main` ou manuellement depuis l'onglet Actions.
L'artefact est nommé `TraceLess-debug-apk`.

## Important
Le code source doit encore être compilé et testé sur un appareil réel. Le workflow GitHub sert à effectuer la première compilation reproductible.
