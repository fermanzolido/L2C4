/**
 * Two languages, one copy of the Spanish.
 *
 * The Spanish text lives in the HTML, which is what a visitor with no JavaScript
 * and what a crawler both read. This file carries only the English, keyed by
 * `data-i18n` (text) or `data-i18n-html` (text that contains markup). Switching to
 * Spanish restores what the HTML shipped with, so there is never a second copy of a
 * Spanish sentence to keep in sync.
 *
 * `STRINGS` is the exception: text built in JavaScript has no HTML to fall back to,
 * so both languages are written out.
 *
 * Loaded as a classic script at the end of <body> rather than as a module, so it
 * runs before the deferred module scripts and before the first paint.
 */
(function () {
  const STORAGE_KEY = 'l2c4.lang';

  /** English for every `data-i18n` / `data-i18n-html` node. Spanish is the HTML itself. */
  const EN = {
    'title.index': 'L2C4 — Lineage 2 Chronicle 4 x1 Server | Scions of Destiny',
    'title.register': 'Create an account — L2C4, a Lineage 2 Chronicle 4 server',
    'title.login': 'Log in — L2C4',
    'title.account': 'My account — L2C4',

    'nav.server': 'Server',
    'nav.register': 'Register',
    'nav.login': 'Log in',
    'nav.account': 'My account',

    'hero.eyebrow': 'Lineage 2 · Chronicle 4',
    'hero.lead':
      'C4 as it was, not as a feature list remembers it. Experience x1, no custom items, no ' +
      'rebalancing. The one hand we laid on the game is the dwarves, and it is spelled out below.',
    'hero.cta': 'Create an account',
    'hero.cta2': 'How to play',
    'status.checking': 'Checking…',

    'facts.chronicle': 'Chronicle 4',
    'facts.xp': 'Experience x1',
    'facts.java': 'Java 25',
    'facts.p2w': 'No pay-to-win',
    'facts.lang': 'Spanish client',

    'chip.essence': 'The essence',
    'chip.rates': 'Rates',
    'chip.dwarves': 'The dwarves',
    'chip.features': 'Features',
    'chip.howto': 'How to play',
    'chip.donate': 'Donation',
    'chip.code': 'The code',

    'essence.eyebrow': 'What this server is',
    'essence.title': 'C4, whole, with nothing bolted on',
    'essence.lead':
      'The idea is simple: a character here should be the same character it was in Chronicle 4. ' +
      'No skill changed a number, no drop table was touched, no NPC carries invented stats. The ' +
      'only things that moved are the rates — and they are one curve, not a pile of loose knobs — ' +
      'and the dwarven lines.',
    'essence.keep.title': 'WHAT IS HERE',
    'essence.keep.1': 'Olympiad and Heroes',
    'essence.keep.2': 'Seven Signs',
    'essence.keep.3': 'Castle sieges and clan halls',
    'essence.keep.4': 'Dimensional Rift',
    'essence.keep.5': 'Subclasses and noblesse by quest',
    'essence.keep.6': 'Fishing, manor, monster races',
    'essence.keep.7': 'Valakas, Antharas, Baium, Zaken',
    'essence.keep.8': 'Skill enchanting',
    'essence.drop.title': 'WHAT YOU WILL NOT FIND',
    'essence.drop.1': 'Custom items or a GM shop',
    'essence.drop.2': 'Stats, gear or levels for money',
    'essence.drop.3': 'A faction system',
    'essence.drop.4': 'Fake players padding the online count',
    'essence.drop.5': 'Auto-farm: autoplay is off',
    'essence.drop.6': 'An instant-noblesse NPC',
    'essence.drop.7': 'Transmog or player-to-player buff selling',
    'essence.drop.8': 'A custom starting point: you start where your race starts',

    'rates.eyebrow': 'One curve, not loose knobs',
    'rates.title': 'Rates',
    'rates.xp.head': 'EXPERIENCE',
    'rates.xp.body': 'Retail pace, including SP, party rates, quest experience and pets.',
    'rates.drop.head': 'DROP',
    'rates.drop.body': 'Chance x2, amount x1. Spoil matches it. Raid bosses x2 as well.',
    'rates.adena.head': 'ADENA',
    'rates.adena.body': 'Quest adena x3 too.',
    'rates.quest.head': 'QUEST REWARDS',
    'rates.quest.body': 'Potions, scrolls, recipes and materials included.',
    'rates.why':
      'Why x1 experience with x2 drops: at x1 a character sits on each level about five times ' +
      'longer, so x5 drops would mean five times the loot across five times the kills, and gear ' +
      'would run far ahead of level — ruining exactly the farming that x1 exists to preserve.',

    'config.title': 'The rest of the configuration',
    'config.autoloot': 'Autoloot',
    'config.autoloot.v': 'On for monsters, off for raids',
    'config.death': 'Delevel and death penalty',
    'config.death.v': 'Retail: you can lose a level, and the death penalty works as it always did',
    'config.champ': 'Champion monsters',
    'config.champ.v':
      '5% frequency between level 20 and 70, five times the health and five times the ' +
      'experience — roughly neutral per unit of time',
    'config.skills': 'Class skills',
    'config.skills.v': 'Delivered on their own as you level and when you log in',
    'config.chars': 'Characters per account',
    'config.chars.v': '7',
    'config.sub': 'Subclasses',
    'config.sub.v': '1 per character, up to level 80',
    'config.ip': 'Clients per IP',
    'config.ip.v': '1, or 2 with premium',
    'config.vit': 'Vitality',
    'config.vit.v': 'Off: it is a system that came after C4',
    'config.balance': 'Balance',
    'config.balance.v':
      'Untouched. No skill damage, drop table or NPC stat was rebalanced. C4 numbers.',

    'dwarf.eyebrow': 'The one change',
    'dwarf.title': 'The dwarves',
    'dwarf.lead':
      'In C4 a dwarf is a trade before it is a character: it spoils, it crafts, and to level it ' +
      'depends on someone carrying it. Here it is still the one who crafts and spoils, but it can ' +
      'also farm on its own. This is the server’s only balance change and it is deliberately ' +
      'narrow: each dwarven line follows one human line, tier for tier, and copies its skills ' +
      'verbatim — same numbers, same cost, not one invented skill.',
    'dwarf.th.class': 'Dwarven class',
    'dwarf.th.follows': 'Follows',
    'dwarf.th.entries': 'Entries in the tree',
    'dwarf.in.title': 'WHAT WAS COPIED',
    'dwarf.in.1': 'Masteries and passives',
    'dwarf.in.2': 'The damage: dagger blows, nukes, farming AoEs',
    'dwarf.in.3': 'Self-buffs',
    'dwarf.in.4': 'Sustain: recovery and drain',
    'dwarf.out.title': 'WHAT WAS LEFT OUT',
    'dwarf.out.1': 'Hard crowd control: Sleep, Sleeping Cloud, Slow, Curse Fear, Stunning Shot',
    'dwarf.out.2': 'Cancellation',
    'dwarf.out.3': 'The dash-and-escape kit: Dash, Ultimate Evasion',
    'dwarf.out.4': 'The curses aimed at players',
    'dwarf.out.5': 'The bow: the dwarf is a dagger user, not a second Rogue',
    'dwarf.out.6': 'The whole summoner branch',
    'dwarf.rules.title': 'The rules of the graft',
    'dwarf.rule.gate': 'You have to promote',
    'dwarf.rule.gate.v':
      'Nothing is grafted into Dwarf Fighter. None of these skills exists before the first class ' +
      '— the same gate the humans who own them go through. At 40, the second class continues the ' +
      'very line the first began.',
    'dwarf.rule.chain': 'Every chain starts at level 1',
    'dwarf.rule.chain.v':
      'The lower levels of Mortal Blow, Anti Magic, Ice Bolt, Vampiric Touch and Weapon Mastery ' +
      'belong to the Human Fighter and the Human Mystic, so those were copied too. No chain ' +
      'starts stranded at level 10.',
    'dwarf.rule.pve': 'The cut is a PvE cut',
    'dwarf.rule.pve.v':
      'What was left out is exactly what makes those classes strong in PvP. The point was a dwarf ' +
      'that can farm, not a second Rogue or a second Sorcerer.',
    'dwarf.rule.numbers': 'Not one number touched',
    'dwarf.rule.numbers.v':
      'Entries were copied verbatim from the human tree, children included: same damage, same SP ' +
      'price, same reuse delay.',

    'features.title': 'Features',
    'features.buffer.head': 'SCHEME BUFFER',
    'features.buffer.body':
      'One in each of the fifteen towns, beside the gatekeeper. 43 buffs, one hour long, up to 4 ' +
      'saved schemes per character. Free for everyone to level 30; above that, premium accounts only.',
    'features.buffer3.head': 'NO THIRD-CLASS BUFFS',
    'features.buffer3.body':
      'Elemental, Divine and Arcane Protection, Siren’s Dance and the Songs of Renewal, ' +
      'Meditation and Champion are not on the NPC. Those stay something only a Cardinal, an ' +
      'Eva’s Saint, a Shillien Saint, a Hierophant, a Sword Muse or a Spectral Dancer can give you.',
    'features.offline.head': 'OFFLINE SHOPS',
    'features.offline.body':
      'Offline trade and craft with <code>.offline</code>: close the client and your shop stays ' +
      'standing for up to 3 days. It survives server restarts.',
    'features.potions.head': 'AUTO POTIONS',
    'features.potions.body':
      'Automatic CP, HP and MP at 70%, with no plugins and no third-party programs. Autoplay, on ' +
      'the other hand, is off: the farming is yours to do.',
    'features.spanish.head': 'THE GAME IN SPANISH',
    'features.spanish.body':
      'With <code>.lang es</code> the client switches to Spanish: gatekeepers, merchants, guards, ' +
      'trainers, warehouses, village masters, the help books and Seven Signs. English and Greek ' +
      'are available too.',
    'features.exp.head': 'EXPERIENCE BRAKE',
    'features.exp.body':
      '<code>.expoff</code> and <code>.expon</code> to hold your level while you farm or set a ' +
      'character up.',
    'features.dualbox.head': 'ONE CLIENT PER IP',
    'features.dualbox.body':
      'Two with premium. It is a deliberate limit: on an x1 server the advantage of extra boxes ' +
      'compounds over months.',
    'features.pvp.head': 'PVP ANNOUNCEMENTS',
    'features.pvp.body':
      'Every PvP and every PK is announced server-wide. No reward for killing: the only ' +
      'consequence is the karma there has always been.',

    'howto.eyebrow': 'Four steps',
    'howto.title': 'How to play',
    'howto.1.head': 'Create your account here',
    'howto.1.body':
      'This is the only place accounts are made: the server does not create one for you at login. ' +
      'A name of 4 to 14 characters, letters and numbers; a password of 8 to 16 — the Interlude ' +
      'client’s own field will not take more than 16.',
    'howto.2.head': 'Get an Interlude client',
    'howto.2.body':
      'The server runs Chronicle 4 content (Scions of Destiny), but uses the Interlude client for ' +
      'performance. Accepted protocol revisions: 656, 660 and 746.',
    'howto.3.head': 'Point the client at the server',
    'howto.3.body': 'Open <code>system/l2.ini</code> and leave the line like this:',
    'howto.4.head': 'Log in',
    'howto.4.body':
      'With the account name and password from step 1. Your game account is ready a few seconds ' +
      'after you register; your account page confirms it.',
    'howto.dl.title': 'Downloads',
    'howto.dl.client': 'Interlude client',
    'howto.dl.client.note': 'The full client. Installed once.',
    'howto.dl.patch': 'L2C4 patch',
    'howto.dl.patch.note': 'The files that go over the client to play on this server.',
    'howto.cmd.title': 'In-game commands',
    'howto.cmd.premium': 'Shows the rates you are actually getting and how much premium is left',
    'howto.cmd.lang': 'Switches the client language (<code>es</code>, <code>en</code>, <code>el</code>)',
    'howto.cmd.offline': 'Leaves your shop or your crafting running and closes the client',
    'howto.cmd.exp': 'Stops and resumes experience gain',
    'howto.cmd.menu': 'Character menu',
    'howto.pass':
      'Passwords are changed only from <a href="/account">your account</a>. The ' +
      '<code>.changepassword</code> command is off on purpose: it reaches the game alone, and ' +
      'would leave you inside the server but locked out of the site.',

    'donate.eyebrow': 'Premium',
    'donate.title': 'Donation',
    'donate.lead':
      'Donations pay for the machine and the connection the server runs on. What they buy is ' +
      'pace, not power: not one item, not one point of a stat, not one level. A premium character ' +
      'and one that never donated fight with exactly the same numbers.',
    'donate.plans.fallback': 'Prices are shown once you are signed in to your account.',
    'donate.table.title': 'What premium changes',
    'donate.th.benefit': 'Benefit',
    'donate.th.normal': 'Normal',
    'donate.th.premium': 'Premium',
    'donate.row.xp': 'Experience',
    'donate.row.sp': 'SP',
    'donate.row.drop': 'Drop amount',
    'donate.row.spoil': 'Spoil amount',
    'donate.row.adena': 'Adena',
    'donate.row.ip': 'Clients per IP',
    'donate.row.buffer': 'Scheme buffer',
    'donate.row.buffer.n': 'to level 30',
    'donate.row.buffer.p': 'any level',
    'donate.how.head': 'HOW IT WORKS',
    'donate.how.1': 'Paid with Mercado Pago from your account page',
    'donate.how.2': 'Applies to every character on the account',
    'donate.how.3': 'Lands in under a minute, with no reconnect',
    'donate.how.4': 'Buying again before it expires adds to the time you have left',
    'donate.limits.head': 'WHAT IT DOES NOT REACH',
    'donate.limits.1': 'It does not affect the rest of your party',
    'donate.limits.2': 'It does not transfer to another account',
    'donate.limits.3': 'It does not change raid boss or herb drops',
    'donate.limits.4': 'It gives no items, no gear and no stats',
    'donate.note':
      'In game, <code>.premium</code> always shows the numbers you are really being given.',

    'code.eyebrow': 'Optimisation and code',
    'code.title': 'Why this does not fall over',
    'code.lead':
      'Almost every server tells you what it added. This one can tell you what it <em>verified</em>, ' +
      'and how many things it looked at to say so. Every number here is a count taken over the real ' +
      'data and the real source, not an estimate, and it is in the commit history for you to check.',
    'code.th.area': 'What was measured',
    'code.th.result': 'Result',
    'code.r1': 'XML data files validating against their schema',
    'code.r2': 'NPC ids defined more than once',
    'code.r3': 'Database connections without a guaranteed close',
    'code.r4': 'Paths from remote input to unparameterised SQL',
    'code.r4.v': 'none',
    'code.r5': 'Spawn points inside the world grid',
    'code.r6': 'Class-change dialogue pages present for C4 NPCs',
    'code.r7': 'Recurring tasks protected against a throwing body',
    'code.r8': 'Unit tests',
    'code.r9': 'Datapack scripts compiled on every change',
    'code.fixes.title': 'What that means for you, playing',
    'code.fix1.head': 'ITEMS DO NOT DUPLICATE',
    'code.fix1.body':
      'Adding items to a stack did not take the lock that destroying and transferring them both ' +
      'took. A test with 8 threads and 2,000 deposits each lost roughly half: 8,519 of an expected ' +
      '17,000. Fixed by taking both locks in object-id order.',
    'code.fix2.head': 'OBJECT IDS ARE NOT REISSUED',
    'code.fix2.body':
      'The five queries that collect the ids in use each swallowed their own failure, so one that ' +
      'did not finish silently removed a whole table’s ids and the server handed them out ' +
      'again. The manager now refuses to start rather than guess.',
    'code.fix3.head': 'CHARACTERS DO NOT LOSE DATA',
    'code.fix3.body':
      'Character variables discarded their changes when the write failed, so memory and the ' +
      'database drifted apart with nobody the wiser.',
    'code.fix4.head': 'FAILURES ARE VISIBLE',
    'code.fix4.body':
      'Task exceptions went to stderr and never to the log. Ten files and directories stayed open ' +
      'if anything went wrong, and six threads ignored their own interrupt — one of them could ' +
      'hold the JVM open.',
    'code.stack':
      'Runs on <a href="http://www.l2jmobius.org/" rel="noopener">L2J Mobius</a> compiled with ' +
      '<strong>Java 25</strong>, with a deadlock watcher every 20 seconds. The code is public: ' +
      '<a href="https://github.com/fermanzolido/L2C4" rel="noopener">github.com/fermanzolido/L2C4</a>.',

    'footer.text':
      'Built on <a href="http://www.l2jmobius.org/" rel="noopener">L2J Mobius</a>. ' +
      'Not affiliated with NCSOFT.',

    'register.title': 'Create an account',
    'register.lead': 'This is the account you will use in the game client. Accounts can only be created here.',
    'register.login': 'Account name',
    'register.login.hint': '4 to 14 characters, letters and numbers only. This is what you type in the client.',
    'register.email': 'Email',
    'register.email.hint': 'Only used to reach you about your account.',
    'register.password': 'Password',
    'register.password.hint':
      '8 to 16 characters. The game client’s login field stops at 16, so anything longer ' +
      'could be stored and then never typed there.',
    'register.have': 'Already have one?',
    'register.have.link': 'Log in',

    'login.title': 'Log in',
    'login.login': 'Account name',
    'login.password': 'Password',
    'login.none': 'No account yet?',
    'login.none.link': 'Create one',

    'account.loading': 'Loading…',
    'account.title': 'Account',
    'account.game.head': 'GAME LOGIN',
    'account.email.head': 'EMAIL',
    'account.email.note': 'Only used to reach you about your account.',
    'account.logout': 'Log out',
    'account.buy.title': 'Buy premium',
    'account.buy.lead':
      'Premium applies to every character on this account. Buying while you are online works: the ' +
      'server picks it up within a minute, no reconnect needed.',
    'account.orders.title': 'Orders',
    'account.orders.date': 'Date',
    'account.orders.plan': 'Plan',
    'account.orders.amount': 'Amount',
    'account.orders.status': 'Status',
    'account.password.title': 'Change password',
    'account.password.lead':
      'Changes it here and in the game at once. The in-game .changepassword only reaches the game, ' +
      'and would leave you unable to sign in here.',
    'account.password.current': 'Current password',
    'account.password.new': 'New password',
    'account.password.hint': '8 to 16 characters — the game client’s field stops at 16.',
  };

  /** Text built in JavaScript, which has no HTML original to fall back to. */
  const STRINGS = {
    'status.online': { es: 'En línea', en: 'Online' },
    'status.offline': { es: 'Fuera de línea', en: 'Offline' },
    'status.unknown': { es: 'Estado no disponible', en: 'Status unavailable' },
    'status.player': { es: 'jugador', en: 'player' },
    'status.players': { es: 'jugadores', en: 'players' },

    // Joins a count to what it is out of: "0 de 5.784".
    'stat.of': { es: 'de', en: 'of' },

    'dl.soon': { es: 'Próximamente', en: 'Coming soon' },
    'dl.get': { es: 'Descargar', en: 'Download' },

    'plan.month': { es: 'mes', en: 'month' },
    'plan.months': { es: 'meses', en: 'months' },
    'plan.buy': { es: 'Comprar', en: 'Buy' },
    'plan.opening': { es: 'Abriendo…', en: 'Opening…' },
    'plan.signin': { es: 'Entrar para comprar', en: 'Sign in to buy' },

    'order.none': { es: 'Todavía no hay órdenes.', en: 'No orders yet.' },
    'order.pending': { es: 'Esperando el pago', en: 'Awaiting payment' },
    'order.paid': { es: 'Acreditada', en: 'Granted' },
    'order.rejected': { es: 'Rechazada', en: 'Rejected' },
    'order.cancelled': { es: 'Cancelada', en: 'Cancelled' },
    'order.underpaid': { es: 'Pago incompleto — escribinos', en: 'Underpaid — contact us' },
    'order.in_process': { es: 'En proceso', en: 'In process' },
    'order.needs_attention': {
      es: 'Pagada — no aplicada, escribinos',
      en: 'Paid — not applied, contact us',
    },

    'game.ready': { es: 'Lista', en: 'Ready' },
    'game.ready.note': {
      es: 'Entrá al juego con este nombre de cuenta y esta contraseña.',
      en: 'Log in to the game with this account name and password.',
    },
    'game.failed': { es: 'Fallo', en: 'Failed' },
    'game.failed.note': {
      es: 'No pudimos crear tu cuenta de juego (%s). Escribinos y lo resolvemos.',
      en: 'Could not create your game login (%s). Contact us and we will sort it out.',
    },
    'game.creating': { es: 'Creándose', en: 'Being created' },
    'game.creating.note': {
      es: 'Suele tardar unos segundos. Recordá recargar la página en un momento.',
      en: 'This usually takes a few seconds. Reload the page in a moment.',
    },

    'btn.register': { es: 'Crear cuenta', en: 'Create account' },
    'btn.registering': { es: 'Creando…', en: 'Creating…' },
    'btn.login': { es: 'Ingresar', en: 'Log in' },
    'btn.password': { es: 'Cambiar contraseña', en: 'Change password' },

    'ok.registered': {
      es: 'Cuenta creada. Estamos preparando tu acceso al juego…',
      en: 'Account created. Setting up your game login…',
    },
    'ok.password': {
      es: 'Cambiada acá. La del juego sigue en unos segundos.',
      en: 'Changed here. The game password follows within a few seconds.',
    },
    'ok.paid': {
      es: 'Pago recibido. El premium se aplica dentro del minuto en que Mercado Pago lo confirma.',
      en: 'Payment received. Premium is applied within a minute of Mercado Pago confirming it.',
    },
    'ok.pending': {
      es: 'Tu pago sigue procesándose. El premium se acredita apenas se aprueba.',
      en: 'Your payment is still being processed. Premium is granted as soon as it clears.',
    },
    'err.failed_payment': {
      es: 'El pago no se completó. No se cobró nada.',
      en: 'The payment did not go through. Nothing was charged.',
    },

    // Keyed by the error codes the Worker returns, so an unknown code falls through
    // to itself rather than to a wrong message.
    invalid_login: {
      es: 'El nombre de cuenta tiene que tener de 4 a 14 caracteres, solo letras y números.',
      en: 'The account name must be 4 to 14 characters, letters and numbers only.',
    },
    invalid_email: {
      es: 'Esa dirección de correo no parece válida.',
      en: 'That email address does not look right.',
    },
    invalid_password: {
      es: 'La contraseña tiene que tener entre 8 y 16 caracteres.',
      en: 'The password must be between 8 and 16 characters.',
    },
    login_taken: { es: 'Ese nombre de cuenta ya está tomado.', en: 'That account name is already taken.' },
    invalid_credentials: {
      es: 'Nombre de cuenta o contraseña incorrectos.',
      en: 'Wrong account name or password.',
    },
    wrong_current_password: { es: 'Esa no es tu contraseña actual.', en: 'That is not your current password.' },
    unauthenticated: {
      es: 'Tu sesión venció. Volvé a ingresar.',
      en: 'Your session expired. Please log in again.',
    },
    unknown_plan: { es: 'Ese plan ya no está disponible.', en: 'That plan is no longer available.' },
    internal_error: {
      es: 'Algo se rompió de nuestro lado. Probá de nuevo en un momento.',
      en: 'Something broke on our side. Try again in a moment.',
    },
  };

  /**
   * The Spanish a node shipped with. Held per node so switching back restores the
   * markup the HTML author wrote, not a flattened copy of it.
   */
  const originals = new WeakMap();

  let lang = initialLanguage();

  function initialLanguage() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === 'es' || saved === 'en') return saved;
    } catch {
      // Private browsing can throw on read. The browser language is a fine answer.
    }
    return (navigator.language || 'es').toLowerCase().startsWith('es') ? 'es' : 'en';
  }

  /** Text built in JavaScript. An unknown key returns itself, so a raw error code still shows. */
  function t(key) {
    const entry = STRINGS[key];
    if (!entry) return key;
    return entry[lang] ?? entry.es;
  }

  function paint(node, property, key) {
    if (!originals.has(node)) originals.set(node, node[property]);
    const english = EN[key];
    node[property] = lang === 'en' && english != null ? english : originals.get(node);
  }

  function apply(root = document) {
    for (const node of root.querySelectorAll('[data-i18n]')) {
      paint(node, 'textContent', node.dataset.i18n);
    }
    for (const node of root.querySelectorAll('[data-i18n-html]')) {
      paint(node, 'innerHTML', node.dataset.i18nHtml);
    }

    document.documentElement.lang = lang;

    const titleKey = document.body?.dataset.titleKey;
    if (titleKey && EN[titleKey]) {
      if (!originals.has(document)) originals.set(document, document.title);
      document.title = lang === 'en' ? EN[titleKey] : originals.get(document);
    }

    for (const button of document.querySelectorAll('[data-lang-option]')) {
      const on = button.dataset.langOption === lang;
      button.classList.toggle('on', on);
      button.setAttribute('aria-pressed', String(on));
    }
  }

  function setLanguage(next) {
    if (next !== 'es' && next !== 'en') return;
    lang = next;
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Not being able to remember the choice is not a reason to refuse it.
    }
    apply();
    // Pages that render their own text listen for this and redraw.
    document.dispatchEvent(new CustomEvent('l2c4:lang', { detail: next }));
  }

  document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-lang-option]');
    if (button) setLanguage(button.dataset.langOption);
  });

  window.I18N = {
    t,
    apply,
    setLanguage,
    get lang() {
      return lang;
    },
  };

  // This script sits at the end of <body>, so everything it translates is parsed.
  apply();
})();
