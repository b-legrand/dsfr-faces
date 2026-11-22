/*
 * dsfr-faces : API JavaScript des composants (chargée par dsfr:head).
 *
 *   DsfrFaces.component('form-popup').show()           // dsfr:popupPanel (modale)
 *   DsfrFaces.component('form-popup').hide()
 *   DsfrFaces.component('form-tabs').switchToItem('b')  // dsfr:tabPanel, dsfr:accordion
 *   DsfrFaces.component('form-panel').switchPanel()     // dsfr:collapsiblePanel
 *
 * S'appuie sur l'API JS du DSFR (dsfr(element).modal...) et sur les boutons rendus par les composants,
 * qui gardent ainsi leur état (champ caché, requête ajax des onglets).
 */
(function () {
  'use strict';

  /** Runs fn(instance) once the DSFR instance of the element exists (DSFR starts asynchronously). */
  function whenDsfr(element, key, fn, tries) {
    var api = window.dsfr && window.dsfr(element);
    var instance = api && api[key];
    if (instance) {
      fn(instance);
    } else if ((tries === undefined ? 50 : tries) > 0) {
      setTimeout(function () { whenDsfr(element, key, fn, (tries === undefined ? 50 : tries) - 1); }, 100);
    }
  }

  function clickItem(element, name) {
    var buttons = element.querySelectorAll('[data-dsfr-faces-item]');
    for (var i = 0; i < buttons.length; i++) {
      if (buttons[i].getAttribute('data-dsfr-faces-item') === name) {
        buttons[i].click();
        return;
      }
    }
  }

  function component(id) {
    var element = document.getElementById(id);
    if (!element) {
      return null;
    }
    var api = { element: element };
    if (element.classList.contains('fr-modal')) {
      api.show = function () { whenDsfr(element, 'modal', function (m) { m.disclose(); }); };
      api.hide = function () { whenDsfr(element, 'modal', function (m) { m.conceal(); }); };
    } else if (element.classList.contains('fr-tabs') || element.classList.contains('fr-accordions-group')) {
      api.switchToItem = function (name) { clickItem(element, name); };
    } else if (element.classList.contains('fr-accordion')) {
      api.switchPanel = function () {
        var button = element.querySelector('.fr-accordion__btn');
        if (button) {
          button.click();
        }
      };
    }
    return api;
  }

  /** dsfr:popupPanel show="true": open once the page or the ajax update is in place. */
  function openShown(root) {
    var dialogs = (root || document).querySelectorAll('dialog.fr-modal[data-dsfr-faces-show="true"]');
    for (var i = 0; i < dialogs.length; i++) {
      (function (dialog) {
        whenDsfr(dialog, 'modal', function (m) { m.disclose(); });
      })(dialogs[i]);
    }
  }

  // An ajax update replacing an open modal would close it: reopen the modals open before the request.
  var openBeforeAjax = [];

  function onAjaxEvent(data) {
    if (data.status === 'begin') {
      openBeforeAjax = [];
      var open = document.querySelectorAll('dialog.fr-modal[open], dialog.fr-modal.fr-modal--opened');
      for (var i = 0; i < open.length; i++) {
        if (open[i].id) {
          openBeforeAjax.push(open[i].id);
        }
      }
    } else if (data.status === 'success') {
      openBeforeAjax.forEach(function (id) {
        var dialog = document.getElementById(id);
        if (dialog && !dialog.hasAttribute('open')) {
          whenDsfr(dialog, 'modal', function (m) { m.disclose(); });
        }
      });
      openBeforeAjax = [];
      openShown();
    }
  }

  window.DsfrFaces = { component: component };

  function start() {
    openShown();
    if (window.faces && window.faces.ajax) {
      window.faces.ajax.addOnEvent(onAjaxEvent);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }
})();
