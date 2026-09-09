/**
 * Motion and formatted numbers.
 *
 * Everything here is progressive: the page is complete and readable before this runs,
 * and every effect checks `prefers-reduced-motion` before it moves anything. No
 * library -- the three things this page needs are an IntersectionObserver, a scroll
 * flag and a tween, and the smallest scroll-animation package is larger than all
 * three together on a connection that has to reach Argentina.
 *
 * Loaded as a classic script after i18n.js, so `window.I18N` is already there.
 */
(function () {
  const still = matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ------------------------------------------------------------------ header */

  const header = document.querySelector('header');
  if (header) {
    // The header is transparent over the hero and takes its ground once the page
    // moves, so the wordmark sits on the artwork rather than in a bar.
    let ticking = false;
    const sync = () => {
      header.classList.toggle('stuck', window.scrollY > 24);
      ticking = false;
    };
    addEventListener(
      'scroll',
      () => {
        if (!ticking) {
          ticking = true;
          requestAnimationFrame(sync);
        }
      },
      { passive: true }
    );
    sync();
  }

  /* ----------------------------------------------------------------- numbers */

  /**
   * A count and, optionally, what it is out of. Held as numbers rather than as text
   * so the thousands separator follows the language -- 1.448 in Spanish is 1,448 in
   * English, and the same string in both would misread in one of them.
   */
  const stats = [...document.querySelectorAll('[data-stat]')];

  function paintStat(cell, progress) {
    const locale = window.I18N?.lang === 'en' ? 'en-US' : 'es-AR';
    const total = Number(cell.dataset.stat);
    const of = cell.dataset.statOf;
    const join = 'statWord' in cell.dataset ? ` ${window.I18N?.t('stat.of') ?? 'de'} ` : ' / ';
    cell.textContent =
      Math.round(total * progress).toLocaleString(locale) +
      (of === undefined ? '' : join + Number(of).toLocaleString(locale));
  }

  const DURATION = 900;

  function countUp(cell) {
    if (still) return paintStat(cell, 1);
    const started = performance.now();
    const step = (now) => {
      const t = Math.min((now - started) / DURATION, 1);
      paintStat(cell, 1 - (1 - t) ** 3); // easeOutCubic: fast, then settling
      if (t < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
    // A frame callback that never arrives would leave the count reading zero, which
    // is not a slower animation but a wrong number. This lands after the tween has
    // finished, so when frames do arrive it repaints what is already there.
    setTimeout(() => paintStat(cell, 1), DURATION + 300);
  }

  document.addEventListener('l2c4:lang', () => stats.forEach((cell) => paintStat(cell, 1)));

  /* ----------------------------------------------------------------- reveals */

  const targets = [...document.querySelectorAll('[data-reveal]')];
  // A pane or an embedder can report a zero-height viewport; there nothing would
  // ever intersect, so treat it the same as no observer at all.
  const fold = window.innerHeight || document.documentElement.clientHeight || 0;
  const observable = 'IntersectionObserver' in window && fold > 0 && !still;

  /**
   * Only what starts below the fold is hidden. Hiding what is already on screen
   * would blank the page for a frame at load, and would leave it blank for good if
   * the observer never ran.
   */
  const waiting = observable ? targets.filter((el) => el.getBoundingClientRect().top > fold * 0.85) : [];
  waiting.forEach((el) => el.classList.add('reveal'));

  const pending = new Set(waiting);
  function reveal(element) {
    element.classList.add('in');
    pending.delete(element);
    element.querySelectorAll('[data-stat]').forEach(countUp);
  }

  // A count inside something still waiting starts at zero and is tweened when that
  // block arrives; every other count is final immediately.
  for (const cell of stats) paintStat(cell, cell.closest('.reveal') ? 0 : 1);

  if (!observable) {
    targets.forEach((el) => el.classList.add('in'));
  } else {
    let ran = false;
    const seen = new IntersectionObserver(
      (entries) => {
        ran = true;
        for (const entry of entries) {
          if (!entry.isIntersecting) continue;
          seen.unobserve(entry.target);
          reveal(entry.target);
        }
      },
      { rootMargin: '0px 0px -12% 0px', threshold: 0.08 }
    );
    waiting.forEach((el) => seen.observe(el));

    // The observer reports on every element it is given, intersecting or not, as
    // soon as it can. If that has not happened at all, it is not going to: show
    // everything rather than leave the page hiding its own content.
    setTimeout(() => {
      if (!ran) [...pending].forEach(reveal);
    }, 2000);
  }

  /* -------------------------------------------------------------- downloads */

  /**
   * A download is live only once someone pastes a URL into its `data-href`. Until
   * then the card carries no `href` at all: a disabled-looking button that still
   * navigates is worse than no button, and an empty href would reload the page.
   */
  function paintDownloads() {
    for (const card of document.querySelectorAll('.dl')) {
      const url = (card.dataset.href || '').trim();
      const state = card.querySelector('.dl-state');
      if (url) {
        card.href = url;
        card.removeAttribute('aria-disabled');
        if (state) state.textContent = window.I18N?.t('dl.get') ?? 'Descargar';
      } else {
        card.removeAttribute('href');
        card.setAttribute('aria-disabled', 'true');
        if (state) state.textContent = window.I18N?.t('dl.soon') ?? 'Proximamente';
      }
    }
  }

  paintDownloads();
  document.addEventListener('l2c4:lang', paintDownloads);

  /* ---------------------------------------------------------------- parallax */

  const crest = document.querySelector('.hero-crest');
  if (crest && !still) {
    let ticking = false;
    const drift = () => {
      // Stops once the hero is off screen; there is nothing to parallax after that.
      const y = Math.min(window.scrollY, 700);
      crest.style.transform = `translateX(-50%) translateY(${y * 0.18}px) scale(${1 + y * 0.00016})`;
      ticking = false;
    };
    addEventListener(
      'scroll',
      () => {
        if (!ticking) {
          ticking = true;
          requestAnimationFrame(drift);
        }
      },
      { passive: true }
    );
  }
})();
