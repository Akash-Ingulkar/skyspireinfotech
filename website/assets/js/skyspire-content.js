/*
 * Skyspire InfoTech: fills the static pages with content managed in the admin panel
 * (skyspireinfotech.com/admin). One request to /admin/api/site per page; if the admin
 * panel can't be reached, the text already written in the HTML stays as it is.
 *
 * Hooks in the HTML:
 *   [data-contact="email|phone|address"]   text of a contact detail
 *   [data-contact-href="email|phone"]       link that becomes mailto: / tel:
 *   [data-footer-services]                  footer <ul> of service links
 *   [data-service-slot="0..3"]              home page service cards (with [data-service-title], [data-service-label])
 *   [data-services-grid]                    services page card grid (cards are re-used as templates)
 *   [data-home="subheading|heading|body"]   home hero text; .hero-video is the hero media card
 *   [data-about="subheading|heading|heading-plain|body"], [data-stat], [data-about-feature-title|text="0..2"],
 *   [data-about-image] / [data-about-bg]     About content (home About section + about.html)
 */
(function () {
    var apiBase = (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
        ? 'http://localhost:8081/admin'
        : '/admin';
    var assetOrigin = apiBase.indexOf('http') === 0 ? apiBase.replace(/\/admin$/, '') : '';

    // Small words stay light in the hero heading, everything else is bold (matches the design).
    var LIGHT_WORDS = ['a', 'an', 'the', 'and', 'or', 'of', 'to', 'for', 'in', 'on', 'at', 'by', 'with',
        'from', 'behind', 'we', 'our', 'your', 'that', 'is', 'are', 'it', 'its', 'into'];

    fetch(apiBase + '/api/site')
        .then(function (res) {
            if (!res.ok) throw new Error('Admin content unavailable');
            return res.json();
        })
        .then(function (data) {
            if (data.contact) applyContact(data.contact);
            if (data.services && data.services.length) {
                applyFooterServices(data.services);
                applyHomeServices(data.services);
                applyServicesGrid(data.services);
            }
            if (data.home) applyHome(data.home);
            if (data.about) applyAbout(data.about);
        })
        .catch(function () {
            // Keep the built-in content.
        });

    function each(selector, fn) {
        Array.prototype.forEach.call(document.querySelectorAll(selector), fn);
    }

    function applyContact(c) {
        ['email', 'phone', 'address'].forEach(function (key) {
            if (!c[key]) return;
            each('[data-contact="' + key + '"]', function (el) { el.textContent = c[key]; });
        });
        if (c.email) each('[data-contact-href="email"]', function (a) { a.href = 'mailto:' + c.email.trim(); });
        if (c.phone) each('[data-contact-href="phone"]', function (a) { a.href = 'tel:' + c.phone.replace(/[^\d+]/g, ''); });
        if (c.address) {
            var q = encodeURIComponent(c.address.trim());
            each('[data-contact-href="map"]', function (a) {
                a.href = 'https://www.google.com/maps/search/?api=1&query=' + q;
            });
            each('iframe[data-contact-map]', function (frame) {
                var src = 'https://www.google.com/maps?q=' + q + '&output=embed';
                if (frame.src !== src) frame.src = src;
            });
        }
    }

    function applyFooterServices(services) {
        each('[data-footer-services]', function (ul) {
            var template = ul.querySelector('li');
            if (!template) return;
            ul.innerHTML = '';
            services.slice(0, 6).forEach(function (s, i, list) {
                var li = template.cloneNode(true);
                li.className = i === list.length - 1 ? 'mb-0' : '';
                var a = li.querySelector('a');
                a.textContent = s.title;
                a.href = 'services.html';
                ul.appendChild(li);
            });
        });
    }

    function applyHomeServices(services) {
        each('[data-service-slot]', function (slot) {
            var i = parseInt(slot.getAttribute('data-service-slot'), 10);
            var s = services[i];
            if (!s) {
                slot.style.display = 'none';
                return;
            }
            var title = slot.querySelector('[data-service-title]');
            if (title) title.textContent = s.title;
            var label = slot.querySelector('[data-service-label]');
            if (label) label.textContent = 'Service ' + (i < 9 ? '0' : '') + (i + 1);
            var link = slot.querySelector('a[aria-label]');
            if (link) link.setAttribute('aria-label', s.title);
        });
    }

    function applyServicesGrid(services) {
        var grid = document.querySelector('[data-services-grid]');
        if (!grid) return;
        var templates = Array.prototype.slice.call(grid.children);
        if (!templates.length) return;
        grid.innerHTML = '';
        services.forEach(function (s, i) {
            var card = templates[i % templates.length].cloneNode(true);
            // Fresh nodes aren't registered with the scroll-reveal library; show them directly.
            card.removeAttribute('data-aos');
            var title = card.querySelector('[data-service-title]');
            if (title) title.textContent = s.title;
            var desc = card.querySelector('[data-service-desc]');
            if (desc) desc.textContent = s.description || '';
            var label = card.querySelector('[data-service-label]');
            if (label) label.textContent = 'Service ' + (i < 9 ? '0' : '') + (i + 1);
            var link = card.querySelector('a[aria-label]');
            if (link) link.setAttribute('aria-label', 'Enquire about ' + s.title);
            grid.appendChild(card);
        });
    }

    function applyHome(h) {
        var eyebrow = document.querySelector('[data-home="subheading"]');
        if (eyebrow && h.subheading) {
            eyebrow.textContent = h.subheading;
            eyebrow.hidden = false;
        }
        var heading = document.querySelector('[data-home="heading"]');
        if (heading && h.heading) renderHeading(heading, h.heading);
        var body = document.querySelector('[data-home="body"]');
        if (body && h.bodyText) body.textContent = h.bodyText;
        if (h.mediaUrl) applyHeroMedia(h.mediaType, assetOrigin + h.mediaUrl);
    }

    /** Heading in the theme's mixed style: small words light, the rest bold. */
    function renderHeading(el, text) {
        el.innerHTML = '';
        el.classList.add('heading--dynamic');
        text.trim().split(/\s+/).forEach(function (word, i) {
            // Real spaces between words (for screen readers and copy/paste).
            if (i > 0) el.appendChild(document.createTextNode(' '));
            var span = document.createElement('span');
            span.className = 'd-inline-block' + (LIGHT_WORDS.indexOf(word.toLowerCase().replace(/[^a-z-]/g, '')) === -1 ? ' fw-semibold' : '');
            span.textContent = word;
            el.appendChild(span);
        });
    }

    function applyAbout(a) {
        if (a.subheading) each('[data-about="subheading"]', function (el) { el.textContent = a.subheading; });
        if (a.heading) {
            each('[data-about="heading"]', function (el) { renderHeading(el, a.heading); });
            each('[data-about="heading-plain"]', function (el) { el.textContent = a.heading; });
        }
        if (a.bodyText) each('[data-about="body"]', function (el) { el.textContent = a.bodyText; });

        var stats = a.stats || {};
        ['projects', 'clients', 'years'].forEach(function (key) {
            if (!stats[key]) return;
            each('[data-stat="' + key + '"]', function (el) {
                // Stop a count-up animation that may be running so it can't restore the old number.
                if (window.counterUp && window.counterUp.default) window.counterUp.default(el, { action: 'stop' });
                el.textContent = stats[key];
            });
        });

        (a.features || []).forEach(function (f, i) {
            if (!f || !f.title) return;
            each('[data-about-feature-title="' + i + '"]', function (el) { el.textContent = f.title; });
            each('[data-about-feature-text="' + i + '"]', function (el) { el.textContent = f.text || ''; });
        });

        if (a.imageUrl) {
            var src = /^https?:/.test(a.imageUrl) ? a.imageUrl : assetOrigin + a.imageUrl;
            each('[data-about-image]', function (img) {
                img.src = src;
                img.alt = 'About Skyspire InfoTech';
                img.classList.add('about-photo--cover');
            });
            each('[data-about-bg]', function (el) {
                el.style.backgroundImage = 'url("' + src + '")';
                // Landscape photos fill the card; square or tall images (logos) are shown whole.
                var probe = new Image();
                probe.onload = function () {
                    var ratio = probe.naturalWidth / (probe.naturalHeight || 1);
                    el.classList.toggle('about-image-box--fit', ratio < 1.15 || ratio > 2.4);
                };
                probe.src = src;
            });
        }
    }

    function applyHeroMedia(type, src) {
        var card = document.querySelector('.hero-video');
        if (!card) return;
        var fallback = card.querySelector('img');
        if (type === 'IMAGE') {
            if (fallback) fallback.src = src;
            return;
        }
        if (type !== 'VIDEO') return;
        var reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        var video = document.createElement('video');
        video.className = 'hero-video__media';
        video.src = src;
        video.muted = true;
        video.loop = true;
        video.playsInline = true;
        video.setAttribute('playsinline', '');
        video.setAttribute('aria-hidden', 'true');
        video.preload = 'auto';
        if (!reduceMotion) video.autoplay = true;
        if (fallback) video.poster = fallback.src;
        // If the video can't load, put the photo back.
        video.addEventListener('error', function () {
            if (fallback && video.parentNode) card.replaceChild(fallback, video);
        }, { once: true });
        if (fallback) card.replaceChild(video, fallback); else card.appendChild(video);
        if (!reduceMotion) video.play().catch(function () {});
    }
})();
