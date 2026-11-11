// Notes icon set: 24 × 24 grid, 1.75 stroke, round caps and joins.
// Maps 1:1 to the app's icon object (NotesIcons.*). Android draws them at 24 dp,
// iOS at 22 pt (SF Symbols weight "regular" equivalent).
const ICONS = {
  search: '<circle cx="11" cy="11" r="7"/><path d="M16.5 16.5 21 21"/>',
  import: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5"/><path d="M12 11v6"/><path d="m9 14 3 3 3-3"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  compose: '<path d="M12 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-6"/><path d="M18.4 2.6a2 2 0 0 1 2.9 2.9L12 14.8l-3.9 1 1-3.9z"/>',
  back: '<path d="M19 12H5"/><path d="m12 19-7-7 7-7"/>',
  chevronLeft: '<path d="m15 18-6-6 6-6"/>',
  chevronRight: '<path d="m9 18 6-6-6-6"/>',
  preview: '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
  edit: '<path d="M17 3a2.83 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5z"/><path d="m15 5 4 4"/>',
  moreVert: '<circle cx="12" cy="5" r="1.3" fill="currentColor"/><circle cx="12" cy="12" r="1.3" fill="currentColor"/><circle cx="12" cy="19" r="1.3" fill="currentColor"/>',
  moreCircle: '<circle cx="12" cy="12" r="9.25"/><circle cx="7.6" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="16.4" cy="12" r="1.1" fill="currentColor" stroke="none"/>',
  pin: '<path d="M12 17v5"/><path d="M9 10.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H8a2 2 0 0 0 0 4 1 1 0 0 1 1 1z"/>',
  pinOff: '<path d="M12 17v5"/><path d="M15 9.34V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H7.89"/><path d="M9 9v1.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h11"/><path d="m2 2 20 20"/>',
  trash: '<path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="m19 6-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/>',
  export: '<path d="M12 3v13"/><path d="m7 8 5-5 5 5"/><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-7"/>',
  close: '<path d="M6 6l12 12M18 6 6 18"/>',
  clearFilled: '<circle cx="12" cy="12" r="9.5" fill="currentColor" stroke="none"/><path d="m8.8 8.8 6.4 6.4M15.2 8.8l-6.4 6.4" stroke="var(--surface)" stroke-width="1.9"/>',
  heading: '<path d="M6 4v16M18 4v16M6 12h12"/>',
  bold: '<path d="M7 4h6a4 4 0 0 1 0 8H7z" stroke-width="2.3"/><path d="M7 12h7a4 4 0 0 1 0 8H7z" stroke-width="2.3"/>',
  italic: '<path d="M19 4h-9M14 20H5M15 4 9 20"/>',
  listBullet: '<circle cx="4.5" cy="6" r="1.2" fill="currentColor"/><circle cx="4.5" cy="12" r="1.2" fill="currentColor"/><circle cx="4.5" cy="18" r="1.2" fill="currentColor"/><path d="M9 6h12M9 12h12M9 18h12"/>',
  listNumbered: '<path d="M10 6h11M10 12h11M10 18h11"/><path d="M4 4h1v4"/><path d="M4 8h2"/><path d="M3.6 14.6a1.4 1.4 0 0 1 2.8.2c0 1-2.8 2-2.8 3.2h2.9"/>',
  checklist: '<rect x="3" y="4" width="6" height="6" rx="1.2"/><path d="m3.8 16.4 1.9 1.9 3.4-3.8"/><path d="M13 7h8M13 17h8"/>',
  quote: '<path d="M17 6H3M21 12H8M21 18H8M3 12v6"/>',
  code: '<path d="m16 18 6-6-6-6M8 6l-6 6 6 6"/>',
  codeBlock: '<rect x="3" y="3" width="18" height="18" rx="2.5"/><path d="m10 9-3 3 3 3M14 15l3-3-3-3"/>',
  link: '<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>',
  check: '<path d="m5 12 5 5 9-10"/>',
  copy: '<rect x="8" y="8" width="13" height="13" rx="2"/><path d="M16 8V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h3"/>',
  openExternal: '<path d="M14 4h6v6M20 4l-9 9"/><path d="M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5"/>',
  note: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h4"/>',
  folder: '<path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>',
  backspace: '<path d="M21 5H8l-6 7 6 7h13a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1z"/><path d="m12 9 6 6M18 9l-6 6"/>',
  shift: '<path d="M12 4 20 12h-4v8H8v-8H4z"/>',
  warning: '<path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/>',
};

function icon(name, { size = 24, label = '', cls = '' } = {}) {
  const body = ICONS[name];
  if (!body) return '';
  const a11y = label ? `role="img" aria-label="${label}"` : 'aria-hidden="true"';
  return `<svg class="ic ${cls}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" ${a11y}>${body}</svg>`;
}
