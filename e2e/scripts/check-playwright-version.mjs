// Vérifie que la version de l'image Docker Playwright (compose.yml) est celle de @playwright/test
// (package.json et version installée). Une différence donne des navigateurs incompatibles avec la
// bibliothèque ; ce contrôle échoue tôt avec un message clair.
// Usage : node scripts/check-playwright-version.mjs   (PLAYWRIGHT_COMPOSE_FILE pour un autre chemin)
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const root = new URL('..', import.meta.url);
const composePath = process.env.PLAYWRIGHT_COMPOSE_FILE ?? fileURLToPath(new URL('../compose.yml', root));
const fail = (message) => {
  console.error(`✗ Version Playwright : ${message}`);
  process.exit(1);
};

const pkg = JSON.parse(readFileSync(new URL('package.json', root), 'utf8'));
const pinned = pkg.devDependencies?.['@playwright/test'];
if (!/^\d+\.\d+\.\d+$/.test(pinned ?? '')) {
  fail(`@playwright/test doit être épinglé à une version exacte dans e2e/package.json (trouvé : ${pinned}).`);
}

if (!existsSync(composePath)) {
  fail(`${composePath} introuvable.`);
}
const image = readFileSync(composePath, 'utf8').match(/mcr\.microsoft\.com\/playwright:v(\d+\.\d+\.\d+)/);
if (!image) {
  fail(`aucune image mcr.microsoft.com/playwright:vX.Y.Z dans ${composePath}.`);
}
if (image[1] !== pinned) {
  fail(`compose.yml utilise l'image v${image[1]} mais e2e/package.json épingle @playwright/test ${pinned}.`
    + ' Aligner les deux versions (puis npm install pour mettre à jour package-lock.json).');
}

const installed = new URL('node_modules/@playwright/test/package.json', root);
if (existsSync(installed)) {
  const version = JSON.parse(readFileSync(installed, 'utf8')).version;
  if (version !== pinned) {
    fail(`@playwright/test ${version} installé, ${pinned} attendu : relancer npm ci.`);
  }
}

console.log(`✓ Version Playwright : ${pinned} (image et package.json alignés)`);
