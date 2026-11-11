// Marky screen mockups. Every screen is built from the same pieces for both
// platforms; `p` is 'android' or 'ios' and decides the platform-specific chrome.

/* ---------- Sample content ---------- */

const NOTES = [
  { title: 'Welcome to Marky', pinned: true, time: 'Mar 4',
    preview: 'Write in Markdown, read it formatted, and find any note in seconds. Everything stays on this device.' },
  { title: 'Weekly plan', pinned: true, time: 'Yesterday',
    preview: 'This week: focus on search and import errors. Ship the notes list, review import error copy, finish the search screen.' },
  { title: 'Café ideas', time: 'Just now',
    preview: 'Oat flat white with cardamom. Ask about the long table by the window for Saturday mornings.' },
  { title: 'Meeting notes: Q4 roadmap', time: '5 min ago',
    preview: 'We agreed the plan keeps export per note. Search has to stay under 100 ms with ten thousand notes.' },
  { title: '', fallback: 'Groceries for the weekend', time: '2 h ago',
    preview: 'Lemons, oat milk, sourdough, basil, the good olive oil.' },
  { title: 'Markdown cheat sheet', time: 'Mar 4',
    preview: 'Headings, bold, italic, lists, checklists, quotes, code and links: everything Marky understands.' },
  { title: 'Tips', time: 'Mar 4',
    preview: 'Long-press a note to pin it. Search finds words in titles and text. Export any note as a .md file.' },
  { title: 'Trip to Lisbon', time: 'Dec 12, 2025',
    preview: 'Plan the tram 28 ride early, before the queues. Pastéis in Belém on the second day.' },
];

const IMPORTED = { title: 'Ideas', time: 'Just now', fresh: true,
  preview: 'A notes app that keeps only the core: write, read, find. Maybe a highlighter as the logo?' };

/* ---------- Building blocks ---------- */

function statusBar(p) {
  return `<div class="sb" aria-hidden="true"><span>9:41</span><span class="sb-icons">
    <i class="sb-sig"></i><i class="sb-wifi"></i><i class="sb-bat"></i></span></div>`;
}

function frame(p, inner, { theme, ts } = {}) {
  const t = theme ? ` data-theme="${theme}"` : '';
  const big = ts ? ` big-text" style="--ts:${ts}` : '';
  const bottom = p === 'android' ? '<div class="gesture"></div>' : '<div class="home-ind" style="position:relative;z-index:6"></div>';
  return `<div class="device ${p}"${t}><div class="screen${big}">${statusBar(p)}${inner}${bottom}</div></div>`;
}

function iconBtn(p, name, label, extra = '') {
  return `<button class="icon-btn ${extra}" aria-label="${label}" title="${label}">${icon(name, { size: p === 'ios' ? 22 : 24 })}</button>`;
}

function noteTitle(n) {
  return n.title || n.fallback || 'Untitled';
}

function card(p, n, { selected } = {}) {
  const cls = p === 'android' ? `a-card${selected ? ' selected' : ''}` : 'i-row';
  const style = n.fresh ? ' style="box-shadow:0 0 0 2px var(--primary) inset"' : '';
  return `<div class="${cls}"${style} role="button">
    <div class="card-head"><div class="card-title${n.title ? '' : ' fallback'}">${noteTitle(n)}</div>
      ${n.pinned ? `<span class="card-pin">${icon('pin', { size: 16, label: 'Pinned' })}</span>` : ''}</div>
    <div class="card-preview">${n.preview}</div>
    <div class="card-time">${n.time}</div></div>`;
}

function noteGroups(p, notes, opts = {}) {
  const pinned = notes.filter((n) => n.pinned);
  const rest = notes.filter((n) => !n.pinned);
  const c = (n) => card(p, n, { selected: opts.selected === n.title });
  if (p === 'android') {
    return `<div class="a-list">
      ${pinned.length ? `<div class="group-label">Pinned</div>${pinned.map(c).join('')}` : ''}
      <div class="group-label">Notes</div>${rest.map(c).join('')}</div>`;
  }
  return `<div class="i-list">
    ${pinned.length ? `<div class="i-section">Pinned</div><div class="i-group">${pinned.map(c).join('')}</div>` : ''}
    <div class="i-section">Notes</div><div class="i-group">${rest.map(c).join('')}</div></div>`;
}

function keyboard(p) {
  const row = (keys) => keys.map((k) => `<span class="k">${k}</span>`).join('');
  return `<div class="kbd" aria-hidden="true">
    <div class="row">${row('qwertyuiop'.split(''))}</div>
    <div class="row pad">${row('asdfghjkl'.split(''))}</div>
    <div class="row"><span class="k alt">${icon('shift', { size: 18 })}</span>${row('zxcvbnm'.split(''))}<span class="k alt">${icon('backspace', { size: 20 })}</span></div>
    <div class="row"><span class="k alt">123</span>${p === 'android' ? '<span class="k alt">,</span>' : ''}<span class="k space">${p === 'ios' ? 'space' : ''}</span>${p === 'android' ? '<span class="k alt">.</span>' : ''}<span class="k ret alt">${p === 'ios' ? 'return' : '↵'}</span></div>
  </div>`;
}

function formatBar(p) {
  const b = (n, l) => iconBtn(p, n, l);
  return `<div class="fmt-bar" role="toolbar" aria-label="Formatting">
    ${b('heading', 'Heading')}${b('bold', 'Bold')}${b('italic', 'Italic')}<span class="sep"></span>
    ${b('listBullet', 'Bulleted list')}${b('listNumbered', 'Numbered list')}${b('checklist', 'Checklist')}${b('quote', 'Quote')}<span class="sep"></span>
    ${b('code', 'Inline code')}${b('codeBlock', 'Code block')}${b('link', 'Link')}</div>`;
}

function emptyState(p, glyph, title, text, actions = '') {
  return `<div class="empty"><div class="glyph">${icon(glyph, { size: 32 })}</div>
    <h2>${title}</h2><p>${text}</p>${actions ? `<div class="actions">${actions}</div>` : ''}</div>`;
}

/* ---------- Notes list ---------- */

function listChrome(p) {
  if (p === 'android') {
    return `<div class="a-topbar"><span class="wordmark">Marky</span><span class="spacer"></span>
      ${iconBtn(p, 'search', 'Search notes')}${iconBtn(p, 'import', 'Import Markdown file')}</div>`;
  }
  return `<div class="i-nav"><span class="spacer"></span>${iconBtn(p, 'import', 'Import Markdown file')}</div>
    <div class="i-large"><span class="wordmark">Marky</span></div>
    <div class="i-search" role="search">${icon('search', { size: 18 })}Search</div>`;
}

function createButton(p, count) {
  if (p === 'android') return `<button class="a-fab">${icon('plus')}New note</button>`;
  return `<div class="i-toolbar"><span class="count">${count ? `${count} notes` : ''}</span>
    <button class="i-compose" aria-label="New note">${icon('compose', { size: 24 })}</button></div>`;
}

function snackbar(p, msg, action, { onNote } = {}) {
  if (p === 'android') {
    const style = onNote ? ' style="bottom:16px"' : '';
    return `<div class="a-snackbar" role="status"${style}><span class="msg">${msg}</span>${action ? `<button class="action">${action}</button>` : ''}</div>`;
  }
  const style = onNote ? ' style="bottom:16px"' : '';
  return `<div class="i-toast" role="status"${style}><span class="msg">${msg}</span>${action ? `<button class="action">${action}</button>` : ''}</div>`;
}

function notesList(p, o = {}) {
  let notes = NOTES;
  if (o.imported) notes = [NOTES[0], NOTES[1], IMPORTED, ...NOTES.slice(2)];
  let body;
  if (o.empty) {
    body = emptyState(p, 'note', 'No notes yet', 'Write your first note, or import a Markdown file.',
      `<button class="btn accent">${icon('plus', { size: 20 })}New note</button>
       <button class="btn outline">${icon('import', { size: 20 })}Import file</button>`);
  } else {
    body = noteGroups(p, notes, { selected: o.menu ? 'Weekly plan' : null });
  }
  let overlay = '';
  if (o.snackbar) overlay += snackbar(p, o.snackbar, o.action);
  if (o.menu) overlay += p === 'android' ? androidListMenu() : iosContextMenu();
  const create = o.empty && p === 'android' ? '' : createButton(p, o.empty ? 0 : notes.length);
  return frame(p, `${listChrome(p)}<div class="body"><div class="scroll">${body}</div>${create}${overlay}</div>`, o);
}

function androidListMenu() {
  return `<div class="a-menu" role="menu" style="right:24px;top:296px">
    <div class="item" role="menuitem">${icon('pinOff')}Unpin</div>
    <div class="item" role="menuitem">${icon('export')}Export as Markdown</div>
    <hr><div class="item danger" role="menuitem">${icon('trash')}Delete</div></div>`;
}

function iosContextMenu() {
  return `<div class="i-blur"></div>
    <div class="i-lifted" style="top:150px">${card('ios', NOTES[1])}</div>
    <div class="i-menu" role="menu" style="left:16px;top:282px">
      <div class="item" role="menuitem">Unpin${icon('pinOff', { size: 20 })}</div>
      <div class="item" role="menuitem">Export as Markdown${icon('export', { size: 20 })}</div>
      <div class="item danger gap" role="menuitem">Delete${icon('trash', { size: 20 })}</div></div>`;
}

/* ---------- Note ---------- */

const PREVIEW_HTML = `
  <h2>This week</h2>
  <p>Focus on <strong>search</strong> and <em>import errors</em>. Everything else waits.</p>
  <ul class="tasks">
    <li class="task done" role="checkbox" aria-checked="true"><span class="box">${icon('check', { size: 16 })}</span><span class="txt">Ship the notes list</span></li>
    <li class="task done" role="checkbox" aria-checked="true"><span class="box">${icon('check', { size: 16 })}</span><span class="txt">Review import error copy</span></li>
    <li class="task" role="checkbox" aria-checked="false"><span class="box"></span><span class="txt">Finish the search screen</span></li>
    <li class="task" role="checkbox" aria-checked="false"><span class="box"></span><span class="txt">Book the design review for Thursday</span></li>
  </ul>
  <blockquote>Writing comes first. Nothing asks you to organize before you write.</blockquote>
  <h3>Search call</h3>
  <pre><code>val results = notes.search(query, limit = 50, rank = TitleFirst)</code></pre>
  <p>Details are in the <a href="#">CommonMark spec</a>. Dropped for now: <del>tags and folders</del>.</p>
  <ol><li>Draft the screens</li><li>Review with the team</li><li>Ship</li></ol>`;

function editorText(withCaret = true) {
  const m = (s) => `<span class="mk">${s}</span>`;
  return [
    `<span class="h l2">${m('## ')}This week</span>`,
    `Focus on ${m('**')}<span class="b">search</span>${m('**')} and ${m('*')}<span class="i">import errors</span>${m('*')}. Everything else waits.`,
    '',
    `${m('- [x]')} Ship the notes list`,
    `${m('- [x]')} Review import error copy`,
    `${m('- [ ]')} Finish the search screen`,
    `${m('- [ ]')} Book the design review for Thursday`,
    `${m('- [ ]')} ${withCaret ? '<span class="caret"></span>' : ''}`,
    '',
    `${m('&gt;')} Writing comes first.`,
    '',
    `Details: ${m('[')}<span class="lt">CommonMark spec</span>${m('](')}<span class="url">https://spec.commonmark.org</span>${m(')')}`,
  ].join('\n');
}

function noteTop(p, mode, { saved } = {}) {
  const toggle = mode === 'preview'
    ? iconBtn(p, 'edit', 'Edit')
    : iconBtn(p, 'preview', 'Preview');
  const savedTxt = saved ? '<span class="saved" role="status">Saved</span>' : '';
  if (p === 'android') {
    return `<div class="a-topbar note">${iconBtn(p, 'back', 'Back')}<span class="spacer"></span>${savedTxt}${toggle}${iconBtn(p, 'moreVert', 'More options')}</div>`;
  }
  return `<div class="i-nav"><button class="back">${icon('chevronLeft', { size: 28 })}Notes</button><span class="spacer"></span>${savedTxt}${toggle}${iconBtn(p, 'moreCircle', 'More options')}</div>`;
}

function note(p, o = {}) {
  const mode = o.mode || 'preview';
  let content;
  if (mode === 'new') {
    content = `<div class="note-title placeholder">Title</div>
      <div class="editor"><span class="caret"></span><span class="placeholder">Start writing. Markdown works here.</span></div>`;
  } else if (mode === 'edit') {
    content = `<div class="note-title">Weekly plan</div><div class="editor">${editorText()}</div>`;
  } else {
    content = `<div class="note-title">Weekly plan</div><div class="md">${o.linkPressed ? PREVIEW_HTML.replace('<a href="#">', '<a href="#" class="pressed">') : PREVIEW_HTML}</div>`;
  }
  const editing = mode !== 'preview';
  let overlay = '';
  if (o.menu) overlay += p === 'android' ? androidNoteMenu() : iosNoteMenu();
  if (o.dialog) overlay += deleteDialog(p);
  if (o.linkMenu) overlay += linkMenu(p);
  if (o.share) overlay += p === 'ios' ? shareSheet() : '';
  if (o.snackbar) overlay += snackbar(p, o.snackbar, null, { onNote: true });
  const bottom = editing ? `${formatBar(p)}${keyboard(p)}` : '';
  return frame(p, `${noteTop(p, editing ? 'edit' : 'preview', { saved: mode === 'edit' })}
    <div class="body"><div class="scroll">${content}</div>${overlay}</div>${bottom}`, o);
}

function androidNoteMenu() {
  return `<div class="a-menu" role="menu" style="right:8px;top:4px">
    <div class="item" role="menuitem">${icon('pinOff')}Unpin</div>
    <div class="item" role="menuitem">${icon('export')}Export as Markdown</div>
    <hr><div class="item danger" role="menuitem">${icon('trash')}Delete</div></div>`;
}

function iosNoteMenu() {
  return `<div class="scrim" style="background:transparent"></div><div class="i-menu top-anchored" role="menu" style="top:6px">
    <div class="item" role="menuitem">Unpin${icon('pinOff', { size: 20 })}</div>
    <div class="item" role="menuitem">Export as Markdown${icon('export', { size: 20 })}</div>
    <div class="item danger gap" role="menuitem">Delete${icon('trash', { size: 20 })}</div></div>`;
}

function deleteDialog(p) {
  const title = 'Delete “Weekly plan”?';
  const body = 'This note will be deleted permanently. This can’t be undone.';
  if (p === 'android') {
    return `<div class="scrim"></div><div class="a-dialog" role="alertdialog" aria-label="${title}">
      <h3>${title}</h3><p>${body}</p>
      <div class="actions"><button class="text-btn focused">Cancel</button><button class="text-btn danger">Delete</button></div></div>`;
  }
  return `<div class="scrim"></div><div class="i-alert" role="alertdialog" aria-label="${title}">
    <div class="txt"><h3>${title}</h3><p>${body}</p></div>
    <div class="actions"><button class="default">Cancel</button><button class="danger">Delete</button></div></div>`;
}

function linkMenu(p) {
  const url = 'https://spec.commonmark.org';
  if (p === 'android') {
    return `<div class="scrim" style="background:transparent"></div><div class="a-menu" role="menu" style="left:24px;top:520px;min-width:240px">
      <div class="item muted" style="height:40px;font-size:13px">${url}</div>
      <div class="item" role="menuitem">${icon('copy')}Copy link</div>
      <div class="item" role="menuitem">${icon('openExternal')}Open</div></div>`;
  }
  return `<div class="i-blur"></div>
    <div class="i-lifted" style="top:430px;left:40px;right:40px;padding:14px 16px;font-size:15px">
      <div style="font-weight:600">CommonMark spec</div><div class="muted" style="font-size:13px;margin-top:2px">${url}</div></div>
    <div class="i-menu" role="menu" style="left:40px;top:512px">
      <div class="item" role="menuitem">Copy link${icon('copy', { size: 20 })}</div>
      <div class="item" role="menuitem">Open${icon('openExternal', { size: 20 })}</div></div>`;
}

function shareSheet() {
  return `<div class="scrim"></div><div class="i-sheet" role="dialog" aria-label="Share">
    <div class="grab"></div>
    <div class="file"><div class="doc">.md</div><div><b>Weekly plan.md</b><span>Markdown document, 1 KB</span></div></div>
    <div class="apps"><div class="app"><i></i>AirDrop</div><div class="app"><i></i>Messages</div><div class="app"><i></i>Mail</div><div class="app"><i></i>More</div></div>
    <div class="acts"><div>Copy${icon('copy', { size: 20 })}</div><div>Save to Files${icon('folder', { size: 20 })}</div></div></div>`;
}

/* ---------- Search ---------- */

const RESULTS = [
  { title: 'Weekly <mark class="hl">plan</mark>', snip: 'This week: focus on search and import errors. Ship the notes list, review import error copy.', time: 'Yesterday', pinned: true },
  { title: 'Meeting notes: Q4 roadmap', snip: '…We agreed the <mark class="hl">plan</mark> keeps export per note. Search has to stay under 100 ms…', time: '5 min ago' },
  { title: 'Trip to Lisbon', snip: '<mark class="hl">Plan</mark> the tram 28 ride early, before the queues. Pastéis in Belém on the second day.', time: 'Dec 12, 2025' },
];

function searchField(p, query, focused) {
  const caret = focused ? '<span class="caret"></span>' : '';
  const text = query
    ? `<span>${query}</span>${caret}`
    : `${caret}<span class="muted">Search notes</span>`;
  if (p === 'android') {
    return `<div class="a-searchfield" role="search">${iconBtn(p, 'back', 'Back')}
      <div class="input">${text}</div>${query ? iconBtn(p, 'close', 'Clear search', 'muted') : ''}</div>`;
  }
  return `<div class="i-search-row" role="search"><div class="i-search active">${icon('search', { size: 18 })}
      <span style="flex:1;display:flex;align-items:center">${text}</span>
      ${query ? `<span style="color:var(--on-muted);display:grid">${icon('clearFilled', { size: 18, label: 'Clear search' })}</span>` : ''}</div>
    <button class="cancel">Cancel</button></div>`;
}

function search(p, o = {}) {
  let body;
  if (o.state === 'results') {
    body = `<div class="result-count">3 notes</div>${RESULTS.map((r) => `<div class="result" role="button">
      <div class="r-title">${r.title}</div><div class="r-snip">${r.snip}</div>
      <div class="r-meta">${r.time}${r.pinned ? icon('pin', { size: 14, label: 'Pinned' }) : ''}</div></div>`).join('')}`;
  } else if (o.state === 'none') {
    body = emptyState(p, 'search', 'No results for “quarterly budget”', 'Check the spelling or try fewer words.');
  } else {
    body = emptyState(p, 'search', 'Search your notes', 'Find any word in titles and text.');
  }
  const query = o.state === 'results' ? 'plan' : o.state === 'none' ? 'quarterly budget' : '';
  const kb = o.state === 'results' ? '' : keyboard(p);
  return frame(p, `${searchField(p, query, o.state !== 'results')}<div class="body"><div class="scroll">${body}</div></div>${kb}`, o);
}

/* ---------- Catalogue ---------- */

const SCREENS = [
  { group: 'Notes list', items: [
    { id: 'list', name: 'Notes list', build: (p, t) => notesList(p, t),
      notes: ['Pinned notes first under a quiet “Pinned” label, then the rest by last edited.',
        'Each entry: one-line title, two-line preview with Markdown stripped, edit time, pin icon.',
        '“Groceries for the weekend” has no title, so it uses the first line of its body.',
        { android: 'The marker-yellow extended FAB creates a note.', ios: 'The marker-yellow compose button in the bottom toolbar creates a note, within thumb reach.' }] },
    { id: 'list-empty', name: 'No notes', build: (p, t) => notesList(p, { ...t, empty: true }),
      notes: ['Headline, one sentence of guidance, and both ways to resolve it: create or import.',
        { android: 'The FAB is hidden while the empty state shows its own create button.', ios: 'The toolbar stays; its compose button also creates a note.' }] },
    { id: 'list-menu', name: 'Long-press actions', build: (p, t) => notesList(p, { ...t, menu: true }),
      notes: ['Long-press opens Unpin (or Pin), Export as Markdown, and Delete.',
        { android: 'Material menu anchored to the pressed card, which shows a selected state.', ios: 'Native context menu: the note lifts above a blurred list.' },
        'Delete is set apart and shown in the destructive color with an icon, never by color alone.'] },
    { id: 'list-imported', name: 'Imported a file', build: (p, t) => notesList(p, { ...t, imported: true, snackbar: 'Imported “Ideas”', action: 'Open' }),
      notes: ['The imported note lands at the top of Notes and the list scrolls to it; the outline marks it briefly.',
        'Confirmation includes an Open action. Stays at least 4 s and is announced by screen readers.',
        { android: 'Snackbar sits above the FAB so it never covers it.', ios: 'Toast floats above the toolbar.' }] },
    { id: 'list-import-error', name: 'Import failed', build: (p, t) => notesList(p, { ...t, snackbar: 'This file is too large to import (max 5 MB)' }),
      notes: ['Plain-language reason, no note created. Other messages: “This file isn’t a text file”, “Couldn’t read this file”.',
        'Cancelling the file picker shows nothing at all.'] },
  ] },
  { group: 'Note', items: [
    { id: 'note-preview', name: 'Reading (preview)', build: (p, t) => note(p, { ...t, mode: 'preview' }),
      notes: ['Existing notes open in preview. The top-bar toggle shows the pencil, the mode it switches to, labelled “Edit”.',
        'Checklist items toggle on tap and stay in place; done items are muted with a check.',
        'Code blocks scroll sideways instead of wrapping. Links underline; only http, https and mailto open.',
        'Double-tap anywhere to edit with the cursor at that spot.'] },
    { id: 'note-edit', name: 'Writing (edit)', build: (p, t) => note(p, { ...t, mode: 'edit' }),
      notes: ['Raw Markdown with light highlighting: muted markers, bold headings, monospace code.',
        'Formatting toolbar rides on the keyboard: Heading, Bold, Italic, lists, Checklist, Quote, Code, Code block, Link. It scrolls; nothing hides in a menu.',
        '“Saved” appears quietly in the top bar and never shifts content. There is no save button.',
        'Enter on a list line continues it; Enter on an empty item ends the list.'] },
    { id: 'note-new', name: 'New note', build: (p, t) => note(p, { ...t, mode: 'new' }),
      notes: ['One tap from the list: edit mode, body focused, keyboard up.',
        'The title is optional and single-line. Leaving with nothing typed discards the note silently.'] },
    { id: 'note-menu', name: 'Note actions', build: (p, t) => note(p, { ...t, mode: 'preview', menu: true }),
      notes: ['Overflow menu: Pin or Unpin, Export as Markdown, Delete.',
        { android: 'Material dropdown from the more button.', ios: 'Pull-down menu from the ellipsis button.' }] },
    { id: 'note-link', name: 'Link actions', build: (p, t) => note(p, { ...t, mode: 'preview', linkPressed: true, linkMenu: true }),
      notes: ['Tap opens the link in the system browser. Long-press offers Copy link and Open, with the address shown first.'] },
    { id: 'note-export', name: 'Export', build: (p, t) => p === 'ios'
        ? note(p, { ...t, mode: 'preview', share: true })
        : note(p, { ...t, mode: 'preview', snackbar: 'Exported “Weekly plan.md”' }),
      notes: [{ android: 'The system document creator saves the file (not drawn). Marky confirms afterwards because Android doesn’t.',
        ios: 'The native share sheet offers Save to Files. iOS confirms the save itself, so Marky adds nothing.' },
        'File name is the title with illegal characters replaced by “-”, trimmed to 100 characters, plus .md.'] },
  ] },
  { group: 'Dialog', items: [
    { id: 'delete', name: 'Delete confirmation', build: (p, t) => note(p, { ...t, mode: 'preview', dialog: true }),
      notes: ['Title names the note. Cancel has default focus; Delete is styled as destructive.',
        'Tapping outside or pressing back counts as Cancel.',
        { android: 'Material 3 basic dialog.', ios: 'Standard iOS alert, Cancel in bold as the default action.' }] },
  ] },
  { group: 'Search', items: [
    { id: 'search-start', name: 'Start typing', build: (p, t) => search(p, { ...t, state: 'start' }),
      notes: ['Field focused and keyboard up on open. Results update as you type, no submit step.',
        { android: 'Back arrow closes search.', ios: 'Cancel closes search; swipe-back works too.' }] },
    { id: 'search-results', name: 'Results', build: (p, t) => search(p, { ...t, state: 'results' }),
      notes: ['Title matches rank first, then newer notes. Every matched word is highlighted with background and bold.',
        'Snippet centres on the first body match; a title-only match shows the start of the body.',
        'Scrolling the results hides the keyboard. Opening one lands in preview at the first match.'] },
    { id: 'search-none', name: 'No results', build: (p, t) => search(p, { ...t, state: 'none' }),
      notes: ['Repeats the query so the person can spot a typo, and suggests what to try next.'] },
  ] },
  { group: 'Accessibility check', items: [
    { id: 'list-200', name: 'Notes list at 200% text', build: (p, t) => notesList(p, { ...t, ts: 2 }),
      notes: ['Text scales with system size up to 200%. Titles wrap instead of truncating; previews keep their 2-line limit.',
        'Touch targets stay at least 48 dp / 44 pt. Pin state is an icon with the label “Pinned”, not color.'] },
  ] },
];

function noteText(n, p) {
  return typeof n === 'string' ? n : n[p];
}

function renderGallery(p, root) {
  const both = root.dataset.both === 'true';
  const themes = both ? ['light', 'dark'] : [undefined];
  root.innerHTML = SCREENS.map((g) => `
    <section class="group" aria-labelledby="g-${g.items[0].id}">
      <h2 id="g-${g.items[0].id}">${g.group}</h2>
      <div class="shots">${g.items.map((s) => `
        <figure class="shot" id="${s.id}">
          <div class="frames">${themes.map((t) => s.build(p, { theme: t })).join('')}</div>
          <figcaption><h3>${s.name}</h3><ul>${s.notes.map((n) => `<li>${noteText(n, p)}</li>`).join('')}</ul></figcaption>
        </figure>`).join('')}</div>
    </section>`).join('');
}

function renderJump(root) {
  root.innerHTML = SCREENS.map((g) => `<li><span>${g.group}</span>${g.items.map((s) => `<a href="#${s.id}">${s.name}</a>`).join('')}</li>`).join('');
}
