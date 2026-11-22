import { test as base, expect } from '@playwright/test';

/** Every test fails if the page logs a console error or throws. */
export const test = base.extend({
  page: async ({ page }, use) => {
    const errors: string[] = [];
    page.on('console', (message) => {
      if (message.type() === 'error') {
        errors.push(message.text());
      }
    });
    page.on('pageerror', (error) => errors.push(error.message));
    await use(page);
    expect(errors, 'erreurs JavaScript dans la page').toEqual([]);
  },
});

export { expect };

/** Marks the window, to check later that a request was ajax (no page reload). */
export async function markWindow(page: import('@playwright/test').Page) {
  await page.evaluate(() => ((window as any).__dsfrFacesMarker = true));
}

export async function isSameWindow(page: import('@playwright/test').Page) {
  return page.evaluate(() => (window as any).__dsfrFacesMarker === true);
}
