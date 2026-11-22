import { test, expect } from './fixtures';

// richfaces/catalogue.xhtml : page RichFaces d'origine (src/main/richfaces), migrée par le sed obligatoire
// seul. rich:dataTable / column / dataScroller, rich:popupPanel + #{rich:component}, rich:panel,
// rich:collapsiblePanel, rich:accordion, rich:tabPanel (ajax), rich:calendar, rich:select, a4j:commandLink.
test.describe('page RichFaces « catalogue » migrée par sed (composants de la phase 2)', () => {
  const firstColumn = (page) => page.locator('#catalogue-communes tbody tr td:first-child');

  test.beforeEach(async ({ page }) => {
    await page.goto('richfaces/catalogue.xhtml');
  });

  test('rich:dataTable : tableau DSFR, titre depuis la facette header, rowClasses', async ({ page }) => {
    await expect(page.locator('#catalogue-communes')).toHaveClass('fr-table');
    await expect(page.locator('#catalogue-communes caption')).toHaveText('Communes');
    await expect(page.locator('#catalogue-communes tbody tr')).toHaveCount(5);
    await expect(page.locator('#catalogue-communes tbody tr').nth(0)).toHaveClass('odd');
    await expect(page.locator('#catalogue-communes tbody tr').nth(1)).toHaveClass('even');
  });

  test('rich:column sortBy et rich:dataScroller', async ({ page }) => {
    await page.locator('#catalogue-communes-sort-1').click(); // population
    await expect(firstColumn(page).first()).toHaveText('Morlaix');
    await page.locator('#catalogue-scroller-next').click();
    await expect(page.locator('#catalogue-scroller [aria-current="page"]')).toHaveText('2');
    await expect(firstColumn(page).first()).toHaveText('Saumur');
  });

  test('a4j:commandLink + render + #{rich:component(...)}.show() : modale DSFR', async ({ page }) => {
    await page.locator('#catalogue-communes-1-show').click();
    const dialog = page.locator('#catalogue-detail');
    await expect(dialog).toHaveAttribute('open');
    await expect(dialog.locator('.fr-modal__title')).toHaveText('Détail de la commune');
    await expect(page.locator('#catalogue-detailText')).toHaveText('Albi : 49236 habitants.');
    await dialog.locator('.fr-btn--close').click();
    await expect(dialog).not.toHaveAttribute('open');
  });

  test('rich:panel, rich:collapsiblePanel, rich:accordion', async ({ page }) => {
    await expect(page.locator('#catalogue-intro')).toHaveClass('fr-card fr-card--no-arrow');
    await expect(page.locator('#catalogue-intro .fr-card__title')).toHaveText('À propos');
    await page.locator('#catalogue-help .fr-accordion__btn').click();
    await expect(page.locator('#helpText')).toBeVisible();
    await page.locator('#catalogue-q2 .fr-accordion__btn').click();
    await expect(page.locator('#a2')).toBeVisible();
  });

  test('rich:tabPanel (ajax), rich:calendar et rich:select dans un onglet', async ({ page }) => {
    await expect(page.locator('#generalText')).toBeVisible();
    await expect(page.locator('#catalogue-since')).toHaveCount(0); // inactive tab not rendered
    await page.locator('#catalogue-search-tab').click();

    const since = page.locator('#catalogue-since');
    await expect(since).toHaveAttribute('type', 'date');
    await expect(since).toHaveClass('fr-input');
    await expect(page.locator('label[for="catalogue-since"]')).toHaveClass('fr-label'); // h:outputLabel still bound
    await expect(page.locator('#catalogue-dept')).toHaveClass('fr-select');
    await expect(page.locator('#catalogue-dept option').first()).toHaveText('Choisir un département');

    await since.fill('2024-03-15');
    await page.locator('#catalogue-dept').selectOption('Tarn');
    await page.locator('#catalogue-searchButton').click();
    await expect(page.locator('#catalogue-searchResult')).toHaveText('Recherche : Tarn depuis le 15/03/2024.');
  });
});
