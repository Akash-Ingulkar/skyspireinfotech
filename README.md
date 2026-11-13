# Skyspire InfoTech

Website and admin panel for [skyspireinfotech.com](https://skyspireinfotech.com).

| Folder | What it is |
|---|---|
| `website/` | The public site: static HTML pages (Home, About, Services, Contact) on the SassTech template, plus our own styles and scripts in `website/assets/` (`skyspire.css`, `skyspire-theme.css`, `js/skyspire-content.js`). |
| `admin-panel/` | Spring Boot admin panel (Java 17, MySQL), served at `/admin`. It manages the hero text and media, About content, services, contact details, and contact-form messages. The website reads this content from `/admin/api/site`. |
| `branding/` | Logo files (SVG/PNG) and hero images. |
| `scripts/build-site.mjs` | Copies `website/` into `dist/` for upload. |

## Build

```bash
# Website -> dist/
npm run build
tar -czf dist.tar.gz -C dist .

# Admin panel -> admin-panel/target/admin-panel.jar
cd admin-panel
mvn -DskipTests clean package
```

## Run the admin panel locally

Needs MySQL on `localhost:3306`. With no environment variables set, it uses database `skyspire_admin`, user `root` and an empty password, and creates the login `admin` / `ChangeMe123!` on first run (local development only).

```bash
java -jar admin-panel/target/admin-panel.jar
# http://localhost:8081/admin
```

Opening `website/index.html` through a local server on `localhost` makes the pages read content from `http://localhost:8081/admin`.

## Deploy (VPS)

Upload the files you changed to `/tmp` on the server, then run the update script:

```bash
scp dist.tar.gz admin-panel/target/admin-panel.jar admin-panel/deploy/update-vps.sh root@<server>:/tmp/
ssh root@<server> "bash /tmp/update-vps.sh"
```

`update-vps.sh` backs up the live site, installs whichever of `dist.tar.gz` and `admin-panel.jar` it finds in `/tmp`, restarts the `skyspire-admin` service (and rolls back to the previous jar if the new one doesn't start), then checks the live URLs. `setup-vps.sh` is the one-time first install (database, systemd service, nginx `/admin` route).

The server keeps its database credentials and initial admin password in `/etc/skyspire-admin.env` (root-only). They are never stored in this repository.

## Notes

- The website design is based on the SassTech template (Envato) and the admin panel on the Rocker admin template (Codervent). Those template files remain under their authors' licenses and are not covered by this repository.
- `website/assets/images/hero-workspace.jpg` is an Unsplash photo by Jakub Żerdzicki (Unsplash License).
