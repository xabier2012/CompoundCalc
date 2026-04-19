/* Theme toggle and smooth scroll to results. */
(function () {
    'use strict';

    const THEME_KEY = 'cc-theme';

    // Apply stored theme immediately to avoid flash of wrong theme
    const stored = localStorage.getItem(THEME_KEY);
    if (stored) {
        document.documentElement.setAttribute('data-bs-theme', stored);
    }

    document.addEventListener('DOMContentLoaded', function () {

        // ---- Theme toggle ----
        const toggleBtn = document.getElementById('themeToggle');
        const themeIcon = document.getElementById('themeIcon');

        function applyTheme(theme) {
            document.documentElement.setAttribute('data-bs-theme', theme);
            localStorage.setItem(THEME_KEY, theme);
            if (themeIcon) {
                themeIcon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
            }
            if (toggleBtn) {
                toggleBtn.setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
            }
        }

        // Initialise icon based on current theme (respect OS preference on first visit)
        let current = document.documentElement.getAttribute('data-bs-theme');
        if (!stored) {
            const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
            current = prefersDark ? 'dark' : 'light';
        }
        current = current || 'light';
        applyTheme(current);

        if (toggleBtn) {
            toggleBtn.addEventListener('click', function () {
                const now = document.documentElement.getAttribute('data-bs-theme');
                applyTheme(now === 'dark' ? 'light' : 'dark');
            });
        }

        // ---- Smooth scroll to results after form submission ----
        const resultsSection = document.getElementById('results');
        if (resultsSection) {
            const prefersReducedMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            resultsSection.scrollIntoView({
                behavior: prefersReducedMotion ? 'auto' : 'smooth',
                block: 'start'
            });
        }
    });
})();
