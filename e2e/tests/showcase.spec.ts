import { test, expect, markWindow, isSameWindow } from './fixtures';

// index.xhtml : composants dsfr: (dsfr:head, dsfr:inputText, dsfr:button, dsfr:messages)
test.describe('showcase dsfr:', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('./');
  });

  test('charge les CSS, polices et JS du DSFR', async ({ page }) => {
    await expect(page.locator('html')).toHaveAttribute('data-fr-js', 'true');
    const font = await page.locator('h1').evaluate((h1) => getComputedStyle(h1).fontFamily);
    expect(font).toContain('Marianne');
  });

  test('envoi ajax vide : erreurs des champs obligatoires', async ({ page }) => {
    await markWindow(page);
    await page.getByRole('button', { name: 'Envoyer (ajax)' }).click();

    await expect(page.locator('#form-name')).toHaveClass(/fr-input-group--error/);
    await expect(page.locator('#form-name-messages .fr-message--error'))
      .toContainText('Vous devez indiquer une valeur');
    await expect(page.locator('#form-name-input')).toHaveAttribute('aria-invalid', 'true');
    await expect(page.locator('#form-email-messages .fr-message--error')).toHaveCount(1);
    expect(await isSameWindow(page)).toBe(true);
  });

  test('validation ajax au blur avec message personnalisé', async ({ page }) => {
    const email = page.locator('#form-email-input');
    await email.fill('pas-une-adresse');
    await email.blur();

    await expect(page.locator('#form-email-messages .fr-message--error'))
      .toHaveText("Le format de l'adresse électronique est invalide.");
    // only the email field was processed
    await expect(page.locator('#form-name-messages .fr-message')).toHaveCount(0);
  });

  test('envoi ajax valide : alerte de confirmation et conversion en Integer', async ({ page }) => {
    await page.locator('#form-name-input').fill('Dupont');
    await page.locator('#form-email-input').fill('dupont@example.fr');
    await page.locator('#form-age-input').fill('42');
    await page.getByRole('button', { name: 'Envoyer (ajax)' }).click();

    const alert = page.locator('#form-msgs .fr-alert--info');
    await expect(alert).toHaveAttribute('role', 'alert');
    await expect(alert.locator('h2.fr-alert__title')).toHaveText('Formulaire envoyé');
    await expect(alert).toContainText('Merci Dupont');
    await expect(page.locator('#form-age-input')).toHaveValue('42');
  });

  test('envoi complet (sans ajax)', async ({ page }) => {
    await markWindow(page);
    await page.locator('#form-name-input').fill('Dupont');
    await page.locator('#form-email-input').fill('dupont@example.fr');
    // let the email field's ajax blur validation finish: navigating during an XHR aborts it
    // (Firefox logs "XMLHttpRequest has an invalid context")
    const blurValidation = page.waitForResponse((r) => r.request().method() === 'POST');
    await page.locator('#form-email-input').blur();
    await blurValidation;
    await page.getByRole('button', { name: 'Envoyer (sans ajax)' }).click();

    const alert = page.locator('#form-msgs .fr-alert--info');
    await expect(alert).toContainText('Merci Dupont');
    await expect(alert).not.toHaveAttribute('role', 'alert'); // not announced on page load
    expect(await isSameWindow(page)).toBe(false);
  });
});
