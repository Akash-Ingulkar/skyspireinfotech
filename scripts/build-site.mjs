// Builds the live skyspireinfotech.com site (static SassTech-themed pages in website/)
// into dist/, ready to upload. The old React app build is still available via `npm run build:react`.
import fs from 'fs';
import path from 'path';

const root = path.resolve(import.meta.dirname, '..');
const src = path.join(root, 'website');
const out = path.join(root, 'dist');

const files = [
  'index.html', 'about.html', 'services.html', 'contact.html',
  'favicon.svg', 'robots.txt', 'sitemap.xml',
  'assets',
  'images/background/11.jpg', // About page fallback image (admin panel can replace it)
];

fs.rmSync(out, { recursive: true, force: true });
for (const f of files) {
  fs.cpSync(path.join(src, f), path.join(out, f), { recursive: true });
}
console.log(`Built site into ${path.relative(root, out)}/`);
