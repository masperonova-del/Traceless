# État de développement

## Implémenté dans cette base
- Interface Compose avec cinq sections.
- Préférences locales via SharedPreferences.
- Checklist et progression explicite.
- Raccourcis vers les paramètres système.
- Aucun INTERNET permission.
- Workflow CI pour APK debug.

## À développer ensuite
1. Tests unitaires du calcul de progression et du catalogue de contrôles.
2. Contrôles automatiques compatibles avec les API Android publiques.
3. Inventaire des applications via les mécanismes de visibilité autorisés.
4. Module local `VpnService` facultatif pour observation limitée et blocage de listes locales.
5. Tests sur plusieurs versions et fabricants Android.
6. Audit de sécurité et accessibilité avant publication.

## Ce qui n'est pas prétendu
Aucune analyse antivirus, détection universelle de spyware, inspection HTTPS ou anonymisation complète n'est fournie par cette version.
