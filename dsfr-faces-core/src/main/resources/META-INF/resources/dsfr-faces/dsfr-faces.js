/*
 * dsfr-faces : API JavaScript des composants (chargée par dsfr:head).
 *
 *   DsfrFaces.component('form-popup').show()           // dsfr:popupPanel (modale)
 *   DsfrFaces.component('form-popup').hide()
 *   DsfrFaces.component('form-tabs').switchToItem('b')  // dsfr:tabPanel, dsfr:accordion
 *   DsfrFaces.component('form-panel').switchPanel()     // dsfr:collapsiblePanel
 *
 *   DsfrFaces.params(element, {nom: valeur})          // paramètres calculés dans le navigateur
 *                                                      // (a4j:param noEscape="true"), ajoutés au
 *                                                      // formulaire le temps d'une requête
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

  /**
   * Adds hidden fields to the form of the element for the request about to be sent (faces.ajax.request
   * serializes the form synchronously), then removes them.
   */
  function params(element, values) {
    var form = element && element.closest ? element.closest('form') : null;
    if (!form) {
      return;
    }
    var added = [];
    Object.keys(values).forEach(function (name) {
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = name;
      input.value = values[name] === null || values[name] === undefined ? '' : String(values[name]);
      form.appendChild(input);
      added.push(input);
    });
    setTimeout(function () {
      added.forEach(function (input) { input.remove(); });
    }, 0);
  }

  window.DsfrFaces = { component: component, params: params };

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
