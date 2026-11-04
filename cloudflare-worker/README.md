# TraceLess Cloudflare Worker

Prototype de passerelle sécurisée entre TraceLess et l'API OpenAI.

## Configuration Cloudflare

Depuis le dossier `cloudflare-worker` :

```bash
npx wrangler secret put OPENAI_API_KEY
npx wrangler secret put TRACELESS_ACCESS_TOKEN
npx wrangler deploy
```

Les deux valeurs sont des secrets Cloudflare distincts. Ne les place jamais dans Git, dans le code Android ou dans une capture d'écran.

## Routes

- `GET /health` : vérification de disponibilité.
- `POST /v1/analyze` : analyse d'un résumé minimal avec authentification Bearer.

## Corps accepté

```json
{
  "good": 3,
  "warning": 1,
  "unavailable": 2,
  "checks": ["Vérification locale terminée"]
}
```

## Avant toute connexion à l'application

Cette version est un prototype, pas une mise en production. Ajouter et tester une limitation de débit Cloudflare, des plafonds de dépenses/alertes côté fournisseur, et une authentification utilisateur adaptée. Un jeton intégré dans un APK peut être extrait : il ne constitue pas à lui seul une authentification durable. Ne pas publier l'URL comme service ouvert.

Le Worker n'enregistre pas volontairement le résumé. Seules les données minimales validées sont envoyées au fournisseur. L'analyse ne peut pas certifier l'absence de logiciel espion ou garantir la sécurité d'un appareil.
