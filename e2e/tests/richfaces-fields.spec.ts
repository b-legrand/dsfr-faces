import { test, expect, markWindow, isSameWindow } from './fixtures';

// Migration FACULTATIVE, testée indépendamment de la migration obligatoire :
// richfaces-fields/contact.xhtml est une page RichFaces d'origine (src/main/richfaces-fields), migrée au
// build par le sed obligatoire PUIS par l'outil dsfr-faces-migrate, qui enveloppe chaque
// libellé + champ + message dans dsfr:field (groupe DSFR fr-input-group / fr-select-group).
const ERROR_COLOR = 'rgb(206, 5, 0)'; // DSFR --text-default-error (light theme)

test.describe('page RichFaces migrée par sed + dsfr:field (migration facultative)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('richfaces-fields/contact.xhtml');
  });

  test('les champs sont dans des groupes DSFR, sans changement d\'identifiant', async ({ page }) => {
    const nameField = page.locator('#request-nameField');
    await expect(nameField).toHaveClass('fr-input-group');
    // ids of the original components are unchanged
    await expect(nameField.locator('> label.fr-label[for="request-name"]')).toBeVisible();
    await expect(nameField.locator('> input#request-name.fr-input')).toBeVisible();
    await expect(nameField.locator('> #request-nameMessage.fr-messages-group')).toHaveCount(1);

    await expect(page.locator('#request-topicField')).toHaveClass('fr-select-group');
    await expect(page.locator('#request-topicField select#request-topic.fr-select')).toBeVisible();
    await expect(page.locator('#request-bodyField')).toHaveClass('fr-input-group');
    await expect(page.locator('#request-bodyField textarea#request-body.fr-input')).toBeVisible();
  });

  test('accessibilité du groupe : mention obligatoire, aria-required, aria-describedby', async ({ page }) => {
    await expect(page.locator('label[for="request-name"]')).toHaveText('Nom *');
    await expect(page.locator('label[for="request-topic"]')).toHaveText('Sujet');
    const name = page.locator('#request-name');
    await expect(name).toHaveAttribute('aria-required', 'true');
    await expect(name).toHaveAttribute('aria-describedby', 'request-nameMessage');
    await expect(page.locator('#request-topic')).not.toHaveAttribute('aria-required');
  });

  test('le champ dans un h:panelGrid est laissé tel quel', async ({ page }) => {
    const phone = page.locator('#request-phone');
    await expect(phone).toHaveClass('fr-input'); // still styled by dsfr-faces-html
    expect(await phone.evaluate((input) => input.closest('.fr-input-group') === null)).toBe(true);
  });

  test('blur invalide : le groupe passe en erreur (rafraîchi car exécuté)', async ({ page }) => {
    const email = page.locator('#request-email');
    await email.fill('pas-une-adresse');
    await email.blur();

    const emailField = page.locator('#request-emailField');
    await expect(emailField).toHaveClass('fr-input-group fr-input-group--error');
    await expect(emailField.locator('.fr-message--error'))
      .toHaveText("Le format de l'adresse électronique est invalide.");
    await expect(emailField.locator('label')).toHaveCSS('color', ERROR_COLOR);
    await expect(page.locator('#request-email')).toHaveValue('pas-une-adresse');
    await expect(page.locator('#request-email')).toHaveAttribute('aria-invalid', 'true');
    // execute="@this": the other groups are untouched
    await expect(page.locator('#request-nameField')).toHaveClass('fr-input-group');
  });

  test('envoi vide sans render : groupes en erreur, liste sans erreur', async ({ page }) => {
    await markWindow(page);
    await page.getByRole('button', { name: 'Envoyer' }).click();

    for (const id of ['name', 'email', 'body']) {
      await expect(page.locator(`#request-${id}Field`)).toHaveClass(/fr-input-group--error/);
      await expect(page.locator(`#request-${id}`)).toHaveAttribute('aria-invalid', 'true');
    }
    await expect(page.locator('#request-topicField')).toHaveClass('fr-select-group');
    expect(await isSameWindow(page)).toBe(true);
  });

  test('saisie avec a4j:ajax keyup : le champ n\'est pas remplacé pendant la frappe', async ({ page }) => {
    const name = page.locator('#request-name');
    await name.click();
    const typed = page.waitForResponse((r) => r.request().method() === 'POST');
    await name.pressSequentially('Dupont', { delay: 30 });
    await typed;
    await page.waitForLoadState('networkidle');

    await expect(name).toHaveValue('Dupont');
    await expect(name).toBeFocused();
  });

  test('envoi valide : groupes sans erreur et message de confirmation', async ({ page }) => {
    await page.locator('#request-name').fill('Dupont');
    await page.locator('#request-email').fill('dupont@example.fr');
    const blur = page.waitForResponse((r) => r.request().method() === 'POST');
    await page.locator('#request-email').blur();
    await blur;
    await page.locator('#request-body').fill('Bonjour');
    await page.getByRole('button', { name: 'Envoyer' }).click();

    await expect(page.locator('#request-globalMessages .fr-alert--info')).toContainText('Demande envoyée');
    for (const id of ['name', 'email', 'body']) {
      await expect(page.locator(`#request-${id}Field`)).toHaveClass('fr-input-group');
      await expect(page.locator(`#request-${id}`)).not.toHaveAttribute('aria-invalid');
    }
  });
});
