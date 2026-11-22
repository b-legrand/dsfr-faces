/*
 * dsfr-faces-richfaces-compat : objet RichFaces minimal.
 *
 * - RichFaces.component(id) pour #{rich:component('id')}.show() & co. Délègue à l'API dsfr-faces
 *   (DsfrFaces.component, chargée par dsfr:head) : show() / hide() (popupPanel), switchToItem(nom)
 *   (tabPanel, accordion), switchPanel() (collapsiblePanel).
 * - RichFaces.ajax.send / poll : requêtes de a4j:jsFunction et a4j:poll (faces.ajax.request standard).
 * - a4j:status : bascule start / stop / error sur les événements faces.ajax.
 */
(function () {
  'use strict';
  var RichFaces = window.RichFaces = window.RichFaces || {};
  RichFaces.component = function (id) {
    return window.DsfrFaces ? window.DsfrFaces.component(id) : null;
  };
  RichFaces.$ = RichFaces.component;

  // ------------------------------------------------------------------ requests (a4j:jsFunction, a4j:poll)

  /** Ajax request with the element of the component as source (it must be inside a form). */
  function send(id, options) {
    var element = document.getElementById(id);
    if (element && window.faces) {
      window.faces.ajax.request(element, null, options);
    }
  }

  var polls = {}; // id -> {timer, generation}

  /**
   * (Re)starts the poll of the element id: next request in `interval` ms, then again once each request has
   * completed. Called by the inline script of the poll, so a re-rendered poll applies its new state.
   */
  function poll(id, interval, enabled, ontimer, options) {
    var state = polls[id] || (polls[id] = { generation: 0 });
    clearTimeout(state.timer);
    var generation = ++state.generation;
    if (!enabled) {
      return;
    }
    state.timer = setTimeout(function () {
      if (!document.getElementById(id) || state.generation !== generation) {
        return; // removed from the page, or re-rendered meanwhile
      }
      if (ontimer && ontimer() === false) {
        poll(id, interval, enabled, ontimer, options);
        return;
      }
      var request = {};
      Object.keys(options).forEach(function (key) { request[key] = options[key]; });
      function next(data) {
        // not re-rendered by the response: schedule the next request ourselves
        if (state.generation === generation) {
          poll(id, interval, enabled, ontimer, options);
        }
        return data;
      }
      request.onevent = function (data) {
        if (options.onevent) {
          options.onevent(data);
        }
        if (data.status === 'success') {
          next(data);
        }
      };
      request.onerror = function (data) {
        if (options.onerror) {
          options.onerror(data);
        }
        next(data);
      };
      send(id, request);
    }, interval);
  }

  RichFaces.ajax = { send: send, poll: poll };

  // ------------------------------------------------------------------ a4j:status

  function statuses(name) {
    var all = document.querySelectorAll('[data-a4j-status]');
    var result = [];
    for (var i = 0; i < all.length; i++) {
      if (all[i].getAttribute('data-a4j-status') === name) {
        result.push(all[i]);
      }
    }
    return result;
  }

  function run(element, callback, data) {
    var code = element.getAttribute('data-' + callback);
    if (code) {
      new Function('event', 'data', code).call(element, data, data);
    }
  }

  function show(element, state) {
    var parts = element.querySelectorAll('[data-a4j-status-state]');
    var target = state;
    if (state === 'error' && !element.querySelector('[data-a4j-status-state="error"]')) {
      target = 'stop';
    }
    for (var i = 0; i < parts.length; i++) {
      if (parts[i].closest('[data-a4j-status]') === element) {
        parts[i].hidden = parts[i].getAttribute('data-a4j-status-state') !== target;
      }
    }
  }

  /** Name of the status of a request: status="…" of its source (set by the request onevent), or "". */
  function statusName(data) {
    return (data.source && data.source.dsfrA4jStatus) || '';
  }

  function onEvent(data) {
    if (data.status === 'begin') {
      // the onevent of the request (which names its status) may run after the global listeners
      Promise.resolve().then(function () {
        statuses(statusName(data)).forEach(function (element) {
          show(element, 'start');
          run(element, 'onstart', data);
        });
      });
    } else if (data.status === 'success') {
      statuses(statusName(data)).forEach(function (element) {
        show(element, 'stop');
        run(element, 'onsuccess', data);
        run(element, 'onstop', data);
      });
    }
  }

  function onError(data) {
    statuses(statusName(data)).forEach(function (element) {
      show(element, 'error');
      run(element, 'onerror', data);
      run(element, 'onstop', data);
    });
  }

  var installed = false;

  function install() {
    if (installed || !window.faces || !window.faces.ajax) {
      return;
    }
    installed = true;
    window.faces.ajax.addOnEvent(onEvent);
    window.faces.ajax.addOnError(onError);
  }

  install();
  if (!installed) {
    document.addEventListener('DOMContentLoaded', install);
  }
})();
