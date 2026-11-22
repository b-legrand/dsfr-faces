import { test, expect, markWindow, isSameWindow } from './fixtures';

// composants.xhtml : composants dsfr: de la phase 2 (champs, tableau, pagination, modale, lien d'action,
// accordéons, onglets, panneau, fieldset).
test.describe('composants dsfr: (phase 2)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('composants.xhtml');
  });

  test.describe('champs de formulaire', () => {
    test('balisage DSFR des champs', async ({ page }) => {
      await expect(page.locator('#fields-comment')).toHaveClass('fr-input-group');
      await expect(page.locator('#fields-comment-input')).toHaveJSProperty('tagName', 'TEXTAREA');
      await expect(page.locator('#fields-password')).toHaveClass('fr-password');
      await expect(page.locator('#fields-password-show')).toHaveAttribute('type', 'checkbox');
      await expect(page.locator('#fields-birth-input')).toHaveAttribute('type', 'date');
      await expect(page.locator('#fields-department')).toHaveClass('fr-select-group');
      await expect(page.locator('#fields-department-input option').first()).toHaveText('Sélectionner un département');
      await expect(page.locator('#fields-contact')).toHaveClass('fr-fieldset');
      await expect(page.locator('#fields-contact .fr-radio-group')).toHaveCount(2);
      await expect(page.locator('#fields-topics .fr-checkbox-group')).toHaveCount(3);
      await expect(page.locator('#fields-cgu')).toHaveClass('fr-checkbox-group');
      await expect(page.locator('#fields-notifications')).toHaveClass('fr-toggle');
      await expect(page.locator('#fields-notifications-input')).toBeChecked();
      await expect(page.locator('#fields-address legend')).toContainText('Adresse');
      await expect(page.locator('#fields-address > .fr-fieldset__element')).toHaveCount(2);
    });

    test('envoi vide : erreurs, y compris le bouton radio obligatoire', async ({ page }) => {
      await page.locator('#fields-save').click();
      await expect(page.locator('#fields-comment')).toHaveClass(/fr-input-group--error/);
      await expect(page.locator('#fields-department')).toHaveClass(/fr-select-group--error/);
      await expect(page.locator('#fields-contact')).toHaveClass(/fr-fieldset--error/);
      await expect(page.locator('#fields-contact-messages .fr-message--error')).toContainText('Moyen de contact');
      await expect(page.locator('#fields-password-messages .fr-message--error')).toHaveCount(1);
    });

    test('envoi valide : conversions (date, liste, cases multiples, booléens)', async ({ page }) => {
      await page.locator('#fields-comment-input').fill('Bonjour');
      await page.locator('#fields-password-input').fill('secret');
      await page.locator('#fields-birth-input').fill('1990-05-14');
      await page.locator('#fields-department-input').selectOption('Tarn');
      await page.locator('label[for="fields-contact-1"]').click();
      await page.locator('label[for="fields-topics-0"]').click();
      await page.locator('label[for="fields-topics-2"]').click();
      await page.locator('label[for="fields-cgu-input"]').click();
      await page.locator('label[for="fields-notifications-input"]').click();
      await page.locator('#fields-city-input').fill('Albi');
      await page.locator('#fields-save').click();

      const alert = page.locator('#fields-msgs .fr-alert--info');
      await expect(alert).toContainText('Préférences enregistrées');
      await expect(alert).toContainText('Département : Tarn ; contact : phone ; thèmes : Culture, Sport ; '
        + 'naissance : 1990-05-14 ; conditions : oui ; notifications : non ; ville : Albi');
      // values redisplayed after the ajax update, except the password
      await expect(page.locator('#fields-birth-input')).toHaveValue('1990-05-14');
      await expect(page.locator('#fields-department-input')).toHaveValue('Tarn');
      await expect(page.locator('#fields-topics-2')).toBeChecked();
      await expect(page.locator('#fields-password-input')).toHaveValue('');
    });
  });

  test.describe('tableau, pagination, modale', () => {
    const firstColumn = (page) => page.locator('#list-communes tbody tr td:first-child');

    test('tableau DSFR paginé', async ({ page }) => {
      await expect(page.locator('#list-communes')).toHaveClass('fr-table fr-table--bordered');
      await expect(page.locator('#list-communes caption')).toHaveText('Communes');
      await expect(page.locator('#list-communes tbody tr')).toHaveCount(5);
      await expect(page.locator('#list-pages [aria-current="page"]')).toHaveText('1');
      await expect(page.locator('#list-pages-first')).toHaveAttribute('aria-disabled', 'true');
    });

    test('tri par colonne (ajax), puis ordre inverse', async ({ page }) => {
      await markWindow(page);
      await page.locator('#list-communes-sort-0').click();
      await expect(firstColumn(page).first()).toHaveText('Albi');
      await expect(page.locator('#list-communes th').first()).toHaveAttribute('aria-sort', 'ascending');
      await page.locator('#list-communes-sort-0').click();
      await expect(firstColumn(page).first()).toHaveText('Vitré');
      await expect(page.locator('#list-communes th').first()).toHaveAttribute('aria-sort', 'descending');
      expect(await isSameWindow(page)).toBe(true);
    });

    test('pagination (ajax), en gardant le tri', async ({ page }) => {
      await page.locator('#list-communes-sort-0').click();
      await expect(firstColumn(page).first()).toHaveText('Albi');
      await page.locator('#list-pages-2').click();
      await expect(page.locator('#list-pages [aria-current="page"]')).toHaveText('2');
      await expect(firstColumn(page)).toHaveText(['Colomiers', 'Écully', 'Fougères', 'Gaillac', 'Lyon']);
      await page.locator('#list-pages-last').click();
      await expect(page.locator('#list-pages [aria-current="page"]')).toHaveText('5');
      await expect(page.locator('#list-communes tbody tr')).toHaveCount(3);
    });

    test('lien d\'action ajax dans une ligne : la modale s\'ouvre (show)', async ({ page }) => {
      await page.locator('#list-communes-0-show').click();
      const dialog = page.locator('#list-detail');
      await expect(dialog).toHaveAttribute('open');
      await expect(page.locator('#detailText')).toContainText('Nantes (Loire-Atlantique)');
      await dialog.locator('.fr-btn--close').click();
      await expect(dialog).not.toHaveAttribute('open');
    });

    test('modale ouverte par l\'API JavaScript DsfrFaces', async ({ page }) => {
      await page.locator('#openHelp').click();
      await expect(page.locator('#list-help')).toHaveAttribute('open');
      await expect(page.locator('#list-help .fr-modal__title')).toHaveText('Aide');
    });

    test('lien d\'action sans ajax : envoi complet du formulaire', async ({ page }) => {
      await markWindow(page);
      await expect(page.locator('#list-count')).toHaveClass(/fr-link/);
      await page.locator('#list-count').click();
      await expect(page.locator('#clicks')).toHaveText('1');
      expect(await isSameWindow(page)).toBe(false);
    });
  });

  test.describe('accordéons, onglets, panneau', () => {
    test('accordéon : élément actif initial, état gardé après un rafraîchissement ajax', async ({ page }) => {
      const q1 = page.locator('#panels-q1 .fr-accordion__btn');
      const q2 = page.locator('#panels-q2 .fr-accordion__btn');
      await expect(q2).toHaveAttribute('aria-expanded', 'true');
      await q1.click();
      await expect(q1).toHaveAttribute('aria-expanded', 'true');
      await expect(q2).toHaveAttribute('aria-expanded', 'false');
      await page.locator('#panels-post').click(); // re-renders the form
      await expect(page.locator('#panels-q1 .fr-accordion__btn')).toHaveAttribute('aria-expanded', 'true');
      await expect(page.locator('#a1')).toBeVisible();
    });

    test('panneau repliable', async ({ page }) => {
      const button = page.locator('#panels-more .fr-accordion__btn');
      await expect(button).toHaveAttribute('aria-expanded', 'false');
      await button.click();
      await expect(page.locator('#moreText')).toBeVisible();
    });

    test('onglets ajax : seul l\'onglet actif est rendu, et traité', async ({ page }) => {
      await expect(page.locator('#t1Text')).toBeVisible();
      await expect(page.locator('#panels-nick-input')).toHaveCount(0);
      await page.locator('#panels-t2-tab').click();
      await expect(page.locator('#panels-t2-tab')).toHaveAttribute('aria-selected', 'true');
      await expect(page.locator('#panels-nick-input')).toBeVisible();
      await expect(page.locator('#t1Text')).toHaveCount(0);
      // the active tab is validated on submit...
      await page.locator('#panels-post').click();
      await expect(page.locator('#panels-nick-messages .fr-message--error')).toContainText('Pseudonyme');
      // ...but switching tab does not validate it
      await page.locator('#panels-t3-tab').click();
      await expect(page.locator('#panels-t3-tab')).toHaveAttribute('aria-selected', 'true');
      await expect(page.locator('#t3Text')).toHaveText('contact@example.fr');
    });

    test('onglets : API JavaScript switchToItem', async ({ page }) => {
      await page.evaluate(() => (window as any).DsfrFaces.component('panels-tabs').switchToItem('t3'));
      await expect(page.locator('#t3Text')).toBeVisible();
    });

    test('onglets client : tout est rendu, l\'onglet actif est gardé', async ({ page }) => {
      await expect(page.locator('#c2Text')).toBeAttached();
      await page.locator('#panels-c2-tab').click();
      await expect(page.locator('#panels-c2-tab')).toHaveAttribute('aria-selected', 'true');
      await page.locator('#panels-post').click();
      await expect(page.locator('#panels-c2-tab')).toHaveAttribute('aria-selected', 'true');
    });

    test('panneau : carte DSFR sans lien', async ({ page }) => {
      await expect(page.locator('#panels-info')).toHaveClass('fr-card fr-card--no-arrow');
      await expect(page.locator('#panels-info h3.fr-card__title')).toHaveText('Panneau');
    });
  });
});
