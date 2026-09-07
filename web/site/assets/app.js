/** Point this at the deployed Worker. */
const API = 'https://l2c4-api.your-subdomain.workers.dev';

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

const MESSAGES = {
  invalid_login: 'The account name must be 4 to 14 characters, letters and numbers only.',
  invalid_email: 'That email address does not look right.',
  invalid_password: 'The password must be between 8 and 64 characters.',
  login_taken: 'That account name is already taken.',
  invalid_credentials: 'Wrong account name or password.',
  wrong_current_password: 'That is not your current password.',
  unauthenticated: 'Your session expired. Please log in again.',
  unknown_plan: 'That plan is no longer available.',
  internal_error: 'Something broke on our side. Try again in a moment.',
};

function say(element, text, kind = 'err') {
  element.textContent = MESSAGES[text] ?? text;
  element.className = `msg show ${kind}`;
}

function clear(element) {
  element.className = 'msg';
}

async function paintStatus(element) {
  if (!element) return;
  try {
    const status = await api('/api/status');
    const dot = element.querySelector('.dot');
    const label = element.querySelector('.label');
    if (status.online) {
      dot.className = 'dot on';
      label.textContent = `Online — ${status.players} ${status.players === 1 ? 'player' : 'players'}`;
    } else {
      dot.className = 'dot off';
      // A stale reading means the agent stopped reporting, which is not the same
      // as the server being down. Saying so beats showing a confident zero.
      label.textContent = status.stale ? 'Status unavailable' : 'Offline';
    }
  } catch {
    element.querySelector('.label').textContent = 'Status unavailable';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  paintStatus(document.querySelector('#status'));

  const path = location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('nav a').forEach((link) => {
    if (link.getAttribute('href') === path) link.classList.add('active');
  });
});

export { api, say, clear, paintStatus };
