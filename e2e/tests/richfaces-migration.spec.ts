import { test, expect, markWindow, isSameWindow } from './fixtures';

// richfaces/inscription.xhtml : page RichFaces 4.5 d'origine (src/main/richfaces), migrée au build
// par scripts/migrate-richfaces.sh (le sed du README). rich: et a4j: sont servis par
// dsfr-faces-richfaces-compat.
test.describe('page RichFaces migrée par sed', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('richfaces/inscription.xhtml');
  });

  test('le DSFR est injecté sans dsfr:head', async ({ page }) => {
    await expect(page.locator('head link[rel="stylesheet"][href*="/dsfr/dsfr.min.css"]')).toHaveCount(1);
    await expect(page.locator('html')).toHaveAttribute('data-fr-js', 'true');
  });

  test('fonction EL rich:clientId', async ({ page }) => {
    await expect(page.locator('#signup-loginClientId')).toHaveText('signup-login');
  });

  test('les composants h: reçoivent les classes DSFR (dsfr-faces-html)', async ({ page }) => {
    await expect(page.locator('label[for="signup-login"]')).toHaveClass('fr-label');
    await expect(page.locator('#signup-login')).toHaveClass('fr-input');
    await expect(page.locator('#signup-profile')).toHaveClass('fr-select');
    // visible: the DSFR reset removes native borders, fr-input draws a background and a bottom border
    const style = await page.locator('#signup-login').evaluate((input) => {
      const css = getComputedStyle(input);
      return { background: css.backgroundColor, shadow: css.boxShadow };
    });
    expect(style.background).not.toBe('rgba(0, 0, 0, 0)');
    expect(style.shadow).not.toBe('none');
  });

  test('a4j:commandButton est un bouton DSFR', async ({ page }) => {
    await expect(page.locator('#signup-register')).toHaveClass(/\bfr-btn\b/);
  });

  test('a4j:ajax au blur : rich:message mis à jour sans render (ajaxRendered)', async ({ page }) => {
    const login = page.locator('#signup-login');
    await login.fill('ab');
    await login.blur();

    await expect(page.locator('#signup-loginMessage .fr-message--error')).toContainText('Identifiant');
    // execute="@this" by default: the email field was not validated
    await expect(page.locator('#signup-emailMessage .fr-message')).toHaveCount(0);
  });

  test('envoi vide : erreurs sous chaque champ, oncomplete exécuté', async ({ page }) => {
    await markWindow(page);
    await page.getByRole('button', { name: "S'inscrire" }).click();

    await expect(page.locator('#signup-loginMessage .fr-message--error')).toContainText('Identifiant');
    await expect(page.locator('#signup-emailMessage .fr-message--error')).toContainText('Adresse électronique');
    await expect(page.locator('#status')).toHaveText('Requête terminée');
    await expect(page.locator('#summary')).toHaveText('');
    // the form is re-rendered (render="signup, summary"): invalid h: fields are marked
    await expect(page.locator('#signup-login')).toHaveAttribute('aria-invalid', 'true');
    await expect(page.locator('#signup-login')).toHaveClass('fr-input');
    await expect(page.locator('#signup-profile')).not.toHaveAttribute('aria-invalid');
    expect(await isSameWindow(page)).toBe(true);
  });

  test('envoi valide : rich:messages, render hors du formulaire, oncomplete', async ({ page }) => {
    await markWindow(page);
    await page.locator('#signup-login').fill('marianne');
    await page.locator('#signup-email').fill('marianne@example.fr');
    await page.getByRole('button', { name: "S'inscrire" }).click();

    const alert = page.locator('#signup-globalMessages .fr-alert--info');
    await expect(alert).toContainText('Inscription enregistrée');
    await expect(alert).toHaveAttribute('role', 'alert');
    await expect(page.locator('#summary')).toHaveText('Compte créé pour marianne.');
    await expect(page.locator('#status')).toHaveText('Requête terminée');
    expect(await isSameWindow(page)).toBe(true);
  });
});
