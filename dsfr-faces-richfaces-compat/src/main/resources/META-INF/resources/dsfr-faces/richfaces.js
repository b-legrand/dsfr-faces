/*
 * dsfr-faces-richfaces-compat : objet RichFaces minimal pour #{rich:component('id')}.show() & co.
 * Délègue à l'API dsfr-faces (DsfrFaces.component, chargée par dsfr:head) :
 * show() / hide() (popupPanel), switchToItem(nom) (tabPanel, accordion), switchPanel() (collapsiblePanel).
 */
(function () {
  'use strict';
  var RichFaces = window.RichFaces = window.RichFaces || {};
  RichFaces.component = function (id) {
    return window.DsfrFaces ? window.DsfrFaces.component(id) : null;
  };
  RichFaces.$ = RichFaces.component;
})();
