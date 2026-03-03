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
        }

        // Initialise icon based on current theme
        const current = document.documentElement.getAttribute('data-bs-theme') || 'light';
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
            resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });
})();
