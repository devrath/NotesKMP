// Shared page chrome: header navigation and the theme switcher.
(function () {
  const pages = [
    ['index.html', 'Overview'],
    ['design-system.html', 'Design system'],
    ['android.html', 'Android screens'],
    ['ios.html', 'iOS screens'],
  ];
  const here = location.pathname.split('/').pop() || 'index.html';

  function storedTheme() {
    try { return localStorage.getItem('marky-theme') || 'system'; } catch (e) { return 'system'; }
  }
  function applyTheme(t) {
    if (t === 'system') document.documentElement.removeAttribute('data-theme');
    else document.documentElement.setAttribute('data-theme', t);
    try { localStorage.setItem('marky-theme', t); } catch (e) { /* storage blocked */ }
    document.querySelectorAll('.theme-switch button').forEach((b) =>
      b.setAttribute('aria-pressed', String(b.dataset.theme === t)));
    document.dispatchEvent(new CustomEvent('marky-theme', { detail: t }));
  }
  applyTheme(storedTheme());

  document.addEventListener('DOMContentLoaded', () => {
    const header = document.createElement('header');
    header.className = 'site-header';
    header.innerHTML = `
      <a class="brand" href="index.html"><span class="wordmark">Marky</span><span class="brand-sub">Design</span></a>
      <nav aria-label="Pages">${pages.map(([href, label]) =>
        `<a href="${href}" ${href === here ? 'aria-current="page"' : ''}>${label}</a>`).join('')}</nav>
      <div class="theme-switch" role="group" aria-label="Theme">
        ${['system', 'light', 'dark'].map((t) =>
          `<button type="button" data-theme="${t}">${t[0].toUpperCase() + t.slice(1)}</button>`).join('')}
      </div>`;
    document.body.prepend(header);
    header.querySelectorAll('.theme-switch button').forEach((b) =>
      b.addEventListener('click', () => applyTheme(b.dataset.theme)));
    applyTheme(storedTheme());
  });
})();
