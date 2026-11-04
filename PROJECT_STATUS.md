# État de développement — TraceLess 1.1

## Implémenté
- Interface Jetpack Compose remaniée : Accueil, Diagnostic, Réseau et Rapports.
- Identité visuelle sombre, bleu et turquoise.
- Moteur de diagnostic local utilisant les API Android accessibles.
- Contrôles : verrouillage de l'écran, chiffrement signalé par Android, correctif de sécurité, DNS privé, débogage USB, options développeur et état de connexion.
- États distincts : conforme, à examiner, information et indisponible.
- Accès contextuels aux paramètres Android pour les contrôles qui demandent une intervention.
- Contrôle HTTPS en ligne lancé manuellement vers Cloudflare : IP publique visible par ce service, point d'accès et version TLS retournée.
- Résultat IP gardé en mémoire de session seulement ; aucune sauvegarde dans le rapport.
- Préférences locales et date du dernier diagnostic.
- Workflow GitHub Actions pour produire l'APK debug.

## Limites actuelles
- L'état du DNS privé dépend des permissions de lecture et du fabricant.
- L'état de chiffrement correspond à la valeur publiée par Android, pas à un audit cryptographique indépendant.
- Le contrôle Cloudflare ne certifie ni l'anonymat, ni la fiabilité d'un VPN, ni le résolveur DNS de toutes les applications.
- Pas encore de détection universelle de spyware, d'inventaire complet des applications, de blocage de trackers ni d'inspection du trafic HTTPS.
- L'adresse MAC aléatoire n'est pas prétendue comme vérifiée automatiquement.

## Prochaines étapes prioritaires
1. Tests sur téléphones réels de plusieurs fabricants et versions Android.
2. Amélioration des diagnostics d'applications dans les limites de visibilité Android.
3. Historique local détaillé et export PDF/JSON sur action de l'utilisateur.
4. Phase séparée d'un filtre local VPN facultatif, avec consentement explicite, règles compréhensibles et arrêt d'urgence.
5. Tests automatisés, accessibilité, audit de confidentialité et de sécurité avant publication.

## Engagement de transparence
TraceLess ne doit jamais transformer une donnée inaccessible en résultat positif, ni présenter un score de sécurité comme une certification.
