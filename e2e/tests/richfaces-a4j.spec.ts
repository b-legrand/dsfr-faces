import { test, expect, markWindow, isSameWindow } from './fixtures';

// richfaces/a4j.xhtml : page RichFaces d'origine (src/main/richfaces), migrée par le sed obligatoire seul.
// a4j:outputPanel, a4j:status, a4j:region, a4j:param (+ f:param), a4j:repeat, a4j:actionListener,
// a4j:jsFunction, a4j:ajax englobant, a4j:poll.
test.describe('page RichFaces « a4j » migrée par sed (balises a4j: P2)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('richfaces/a4j.xhtml');
  });

  test('a4j:outputPanel : cible ajax toujours présente, et ajaxRendered', async ({ page }) => {
    await expect(page.locator('#ajax-details')).toHaveJSProperty('tagName', 'DIV'); // layout="block"
    await expect(page.locator('#ajax-last')).toHaveJSProperty('tagName', 'SPAN');
    await expect(page.locator('#ajax-last')).toHaveText('Dernière action : aucune');
    await expect(page.locator('#ajax-detailsText')).toHaveCount(0);

    await markWindow(page);
    await page.locator('#ajax-toggle').click();
    await expect(page.locator('#ajax-detailsText')).toHaveText('Détails affichés.');
    // not in render="details", updated because ajaxRendered="true"
    await expect(page.locator('#ajax-last')).toHaveText('Dernière action : détails');
    await page.locator('#ajax-toggle').click();
    await expect(page.locator('#ajax-detailsText')).toHaveCount(0);
    expect(await isSameWindow(page)).toBe(true);
  });

  test('a4j:status sans nom : suit les requêtes qui ne nomment pas d\'indicateur', async ({ page }) => {
    const start = page.locator('#globalStatus [data-a4j-status-state="start"]');
    const stop = page.locator('#globalStatus [data-a4j-status-state="stop"]');
    await expect(page.locator('#globalStatus')).toHaveAttribute('role', 'status');
    await expect(stop).toHaveText('Prêt');
    await expect(start).toBeHidden();

    await page.locator('#ajax-wait').click();
    await expect(start).toBeVisible();
    await expect(start).toHaveText('Chargement…');
    await expect(stop).toBeHidden();
    await expect(page.locator('#slowStatus [data-a4j-status-state="start"]')).toBeHidden();
    await expect(stop).toBeVisible();
    await expect(start).toBeHidden();
    await expect(page.locator('#ajax-last')).toHaveText('Dernière action : attente');
  });

  test('a4j:status nommé : seulement pour status="slow"', async ({ page }) => {
    const slow = page.locator('#slowStatus [data-a4j-status-state="start"]');
    await page.locator('#ajax-slow').click();
    await expect(slow).toBeVisible();
    await expect(slow).toHaveText('Traitement long…');
    await expect(page.locator('#globalStatus [data-a4j-status-state="start"]')).toBeHidden();
    await expect(slow).toBeHidden();
    await expect(page.locator('#ajax-last')).toHaveText('Dernière action : traitement long');
  });

  test('a4j:param : assignTo converti, noEscape évalué dans le navigateur, f:param en ajax', async ({ page }) => {
    await page.locator('#ajax-pick').click();
    await expect(page.locator('#ajax-picked'))
      .toHaveText('Albi (49236 hab.) ; réponse : 42 ; origine : bouton');
  });

  test('a4j:repeat (rows, first, iterationStatusVar) et a4j:param par ligne', async ({ page }) => {
    const items = page.locator('#communes li');
    await expect(items).toHaveCount(3);
    await expect(items).toHaveText([/^\s*3\.\s+Brest\s*$/, /^\s*4\.\s+Lyon\s*$/, /^\s*5\.\s+Quimper\s*$/]);
    await items.nth(1).locator('a').click();
    await expect(page.locator('#ajax-picked')).toHaveText(/^Lyon/);
  });

  test('a4j:actionListener : écouteur avant l\'action', async ({ page }) => {
    await page.locator('#ajax-listen').click();
    await expect(page.locator('#ajax-listened')).toHaveText('Écouteur : listen puis action');
  });

  test('a4j:jsFunction : fonction globale, argument dans a4j:param, oncomplete', async ({ page }) => {
    await expect(page.locator('#ajax-loaded')).toHaveCount(0);
    await page.locator('#callJs').click();
    await expect(page.locator('#ajax-loaded')).toHaveText('Chargée : Vitré');
    await expect(page.locator('#jsDone')).toHaveText('terminé');
    await expect(page.locator('#ajax-last')).toHaveText('Dernière action : jsFunction');
  });

  test('a4j:ajax englobant : chaque champ du corps envoie sa requête (événement par défaut)', async ({ page }) => {
    await page.locator('#ajax-dept').selectOption('Tarn');
    await expect(page.locator('#ajax-echo')).toHaveText('Tarn /');
    await page.locator('#ajax-size').fill('M');
    await page.locator('#ajax-size').press('Tab');
    await expect(page.locator('#ajax-echo')).toHaveText('Tarn / M');
  });

  test('a4j:region : seule la région est traitée, pas le reste du formulaire', async ({ page }) => {
    await page.locator('#regionForm-city').fill('Albi');
    await page.locator('#regionForm-checkCity').click();
    await expect(page.locator('#regionForm-cityResult')).toHaveText('Commune vérifiée : Albi');
    await expect(page.locator('#regionForm-otherMessage .fr-message')).toHaveCount(0);

    // outside the region, the whole form is executed: the other required field fails
    await page.locator('#regionForm-checkAll').click();
    await expect(page.locator('#regionForm-otherMessage .fr-message--error')).toHaveCount(1);
  });

  test('a4j:poll : requêtes périodiques, arrêtées quand enabled passe à false', async ({ page }) => {
    await expect(page.locator('#pollForm-ticks')).toHaveText('0');
    await page.locator('#pollForm-start').click();
    await expect(page.locator('#pollForm-ticks')).toHaveText('3'); // 3 requests, 400 ms apart
    await page.waitForTimeout(1500); // the bean disables the poll at 3
    await expect(page.locator('#pollForm-ticks')).toHaveText('3');
  });
});
