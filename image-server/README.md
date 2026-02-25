# image-server

Service Quarkus pour:
- uploader des images,
- servir l'original,
- générer et mettre en cache des variantes redimensionnées,
- supprimer une image et ses variantes de cache,
- nettoyer automatiquement le cache expiré.

## Prérequis

- Java 21+
- Maven Wrapper (`./mvnw`)

## Lancer le service

Depuis la racine du monorepo:

```bash
./mvnw -pl image-server quarkus:dev
```

Le service expose les endpoints sous `/images`.
Le préfixe `/images` est configurable via `image.route-prefix`.

## Configuration

Fichier: `image-server/src/main/resources/application.properties`

Propriétés principales:

- `image.upload-base-dir` (défaut: `uploads`)
- `image.cache-dir` (défaut: `.cache`)
- `image.allowed-extensions` (défaut: `jpg,jpeg,png,gif,webp,bmp`)
- `image.max-file-size` (défaut: `20971520` = 20 MB)
- `image.max-dimension` (défaut: `5000`)
- `image.cache-days` (défaut: `7`)
- `image.route-prefix` (défaut: `images`)
- `image.cleanup-cron` (défaut du projet: `0 0 * * * ?`)

Variables d'environnement supportées:

- `IMAGE_UPLOAD_BASE_DIR`
- `IMAGE_CACHE_DIR`

## API

### 1) Upload

- `POST /images/upload`
- `Content-Type: multipart/form-data`
- Champs:
- `file` (requis)
- `folder` (optionnel, ex: `avatars`, `products/shoes`)

Exemple:

```bash
curl -X POST "http://localhost:8080/images/upload" \
  -F "file=@/tmp/photo.jpg" \
  -F "folder=avatars"
```

Réponse succès (`201`):

```json
{
  "url": "avatars/<uuid>.jpg",
  "error": null
}
```

### 2) Lire une image / générer une variante

- `GET /images/{path}`

Exemples:

- Original: `GET /images/avatars/alice.jpg`
- Variante: `GET /images/avatars/alice.jpg?w=300&h=300&crop=true&fmt=webp&q=85&upscale=false`

Query params:

- `w` largeur cible (optionnel)
- `h` hauteur cible (optionnel)
- `crop` recadrage centré (`false` par défaut)
- `q` qualité de sortie 1-100 (`85` par défaut)
- `fmt` format de sortie `jpg|png|webp` (appliqué uniquement si resize)
- `upscale` autoriser l'agrandissement (`false` par défaut)

Comportement:

- Sans `w`/`h`: renvoie le fichier original.
- Avec `w`/`h`: utilise le cache disque (`image.cache-dir`) puis génère si absent.
- Avec `upscale=false`: la clé de cache est basée sur les dimensions effectives (clampées à la taille source).

### 3) Supprimer

- `DELETE /images/{path}`

Exemple:

```bash
curl -X DELETE "http://localhost:8080/images/avatars/alice.jpg"
```

Effet:

- supprime l'original,
- supprime toutes les variantes de cache associées à ce fichier.

## Nettoyage automatique du cache

Le scheduler appelle `CacheCleanupService` selon `image.cleanup-cron`.

Par défaut dans ce projet:
- prod: toutes les heures (`0 0 * * * ?`)
- dev: toutes les 30 minutes (`%dev.image.cleanup-cron=0 */30 * * * ?`)

Les fichiers plus anciens que `image.cache-days` sont supprimés.

## Tests

Classe de tests: `image-server/src/test/java/image/server/service/ImageServerServiceTest.java`

Couvre actuellement:

- suppression des variantes de cache par préfixe,
- stabilité de l'ETag pour un même contenu binaire,
- cohérence de clé de cache quand `upscale=false`.

### Exécuter les tests du module

Commande standard:

```bash
./mvnw -pl image-server test
```

Note actuelle:
Sur cet environnement, la configuration surefire du parent injecte `@{argLine}` en fork et peut faire échouer `test`.
Contournement:

```bash
./mvnw -pl image-server -DforkCount=0 -Dtest=ImageServerServiceTest test
```

Ou pour tout le module sans fork:

```bash
./mvnw -pl image-server -DskipTests=false -DforkCount=0 test
```
