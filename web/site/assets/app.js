/**
 * The Worker, which must be a subdomain of this site's own domain rather than a
 * workers.dev address. The session cookie is SameSite=Lax and a browser will not
 * send it on a cross-site fetch, so an API on workers.dev would leave every visitor
 * permanently logged out.
 */
const API = 'https://api.l2jsaked.com.ar';

/** Credentials are included so the session cookie travels; the Worker allows this origin only. */
async function api(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(body.error ?? `http_${response.status}`);
    error.code = body.error;
    error.status = response.status;
    throw error;
  }
  return body;
}

/**
 * `key` is looked up in the string table, which returns the key itself when it does
 * not know it -- so an error code the Worker grows tomorrow still reaches the reader
 * instead of turning into a blank box.
 */
function say(element, key, kind = 'err') {
  element.textContent = window.I18N.t(key);
  element.dataset.key = key;
  element.className = `msg show ${kind}`;
}

function clear(element) {
  element.className = 'msg';
  delete element.dataset.key;
}

/** Redraws a visible message in the language just chosen. */
function repaintMessages() {
  for (const element of document.querySelectorAll('.msg.show[data-key]')) {
    element.textContent = window.I18N.t(element.dataset.key);
  }
}

async function paintStatus(element) {
  if (!element) return;
  const t = (key) => window.I18N.t(key);
  const dot = element.querySelector('.dot');
  const label = element.querySelector('.label');

  const draw = (state, players) => {
    // Remembered on the node so a language switch can redraw without asking the API again.
    element.dataset.state = state;
    element.dataset.players = players ?? '';
    if (state === 'online') {
      dot.className = 'dot on';
      const word = players === 1 ? t('status.player') : t('status.players');
      label.textContent = `${t('status.online')} — ${players} ${word}`;
    } else if (state === 'offline') {
      dot.className = 'dot off';
      label.textContent = t('status.offline');
    } else {
      dot.className = 'dot';
      label.textContent = t('status.unknown');
    }
  };

  document.addEventListener('l2c4:lang', () => {
    if (element.dataset.state) draw(element.dataset.state, Number(element.dataset.players));
  });

  try {
    const status = await api('/api/status');
    // A stale reading means the agent stopped reporting, which is not the same as
    // the server being down. Saying so beats showing a confident zero.
    if (status.online) draw('online', status.players);
    else draw(status.stale ? 'unknown' : 'offline', 0);
  } catch {
    draw('unknown', 0);
  }
}

/**
 * The plans on the home page are read-only: buying needs a session, so the button is
 * a link to the account page rather than a checkout the visitor cannot complete.
 */
async function paintPlans(container, fallback) {
  if (!container) return;

  let data;
  try {
    data = await api('/api/plans');
  } catch {
    // Prices are not worth an error box on a marketing page; the account page has them.
    if (fallback) fallback.hidden = false;
    return;
  }

  const draw = () => {
    const t = (key) => window.I18N.t(key);
    container.innerHTML = data.plans
      .map(
        (plan) => `<div class="plan">
          <div class="days">${plan.months}<span>${plan.months === 1 ? t('plan.month') : t('plan.months')}</span></div>
          <div class="price">${data.currency} ${plan.price.toLocaleString()}</div>
          <a class="btn ghost" href="/account" style="display:block;margin-top:16px">${t('plan.signin')}</a>
        </div>`
      )
      .join('');
  };

  draw();
  document.addEventListener('l2c4:lang', draw);
}

document.addEventListener('DOMContentLoaded', () => {
  document.addEventListener('l2c4:lang', repaintMessages);

  // Hosting has cleanUrls on, so the browser is at /register rather than at
  // /register.html. Comparing resolved paths works either way; comparing the href
  // to a file name silently stops matching anything.
  document.querySelectorAll('nav a').forEach((link) => {
    if (new URL(link.href).pathname === location.pathname) link.classList.add('active');
  });
});

export { api, say, clear, paintStatus, paintPlans };
