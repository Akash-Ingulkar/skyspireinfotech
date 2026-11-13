#!/usr/bin/env bash
# One-time (and re-runnable) setup of the Skyspire admin panel on the VPS,
# served at https://skyspireinfotech.com/admin.
#
# Usage (as root, after uploading admin-panel.jar and this script to /tmp):
#   bash /tmp/setup-vps.sh
#
# Touches only: /opt/skyspire-admin, /etc/skyspire-admin.env, the skyspire-admin
# systemd service, the skyspire_admin MySQL database/user, and the
# skyspireinfotech.com nginx site (backed up first, restored if nginx -t fails).
set -euo pipefail

APP_DIR=/opt/skyspire-admin
ENV_FILE=/etc/skyspire-admin.env
SERVICE=skyspire-admin
NGINX_SITE=/etc/nginx/sites-available/skyspireinfotech.com
JAR_SRC=/tmp/admin-panel.jar

[ -f "$JAR_SRC" ] || { echo "Missing $JAR_SRC - upload admin-panel.jar to /tmp first"; exit 1; }

echo "==> 1/5 App folder and system user"
id -u "$SERVICE" >/dev/null 2>&1 || useradd --system --home "$APP_DIR" --shell /usr/sbin/nologin "$SERVICE"
mkdir -p "$APP_DIR/uploads"
[ -f "$APP_DIR/admin-panel.jar" ] && cp "$APP_DIR/admin-panel.jar" "$APP_DIR/admin-panel.jar.prev"
cp "$JAR_SRC" "$APP_DIR/admin-panel.jar"
chown -R "$SERVICE:$SERVICE" "$APP_DIR"

echo "==> 2/5 Database (created only on first run)"
if [ ! -f "$ENV_FILE" ]; then
    DB_PASS=$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)
    SEED_PASS=$(openssl rand -base64 18 | tr -d '/+=' | cut -c1-16)
    mysql <<SQL
CREATE DATABASE IF NOT EXISTS skyspire_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'skyspire_admin'@'localhost' IDENTIFIED BY '${DB_PASS}';
ALTER USER 'skyspire_admin'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON skyspire_admin.* TO 'skyspire_admin'@'localhost';
FLUSH PRIVILEGES;
SQL
    umask 077
    cat > "$ENV_FILE" <<ENV
DB_URL=jdbc:mysql://localhost:3306/skyspire_admin?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=skyspire_admin
DB_PASSWORD=${DB_PASS}
ADMIN_SEED_PASSWORD=${SEED_PASS}
ENV
    chmod 600 "$ENV_FILE"
    FIRST_RUN=1
else
    echo "    $ENV_FILE already exists - keeping existing database credentials"
    FIRST_RUN=0
fi

echo "==> 3/5 systemd service"
cat > /etc/systemd/system/$SERVICE.service <<UNIT
[Unit]
Description=Skyspire InfoTech admin panel (Spring Boot, /admin)
After=network.target mysql.service
Requires=mysql.service

[Service]
User=$SERVICE
WorkingDirectory=$APP_DIR
EnvironmentFile=$ENV_FILE
ExecStart=/usr/bin/java -Xms128m -Xmx384m -jar $APP_DIR/admin-panel.jar --server.port=8081 --server.address=127.0.0.1 --spring.thymeleaf.cache=true
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable "$SERVICE" >/dev/null
systemctl restart "$SERVICE"

echo -n "    waiting for the app to start"
for i in $(seq 1 60); do
    if curl -s -o /dev/null http://127.0.0.1:8081/admin/login; then echo " - up"; break; fi
    echo -n "."; sleep 2
    if [ "$i" = 60 ]; then echo; echo "App did not start. Last log lines:"; journalctl -u $SERVICE -n 40 --no-pager; exit 1; fi
done

echo "==> 4/5 nginx: route /admin to the app"
if grep -q "location ^~ /admin" "$NGINX_SITE"; then
    echo "    /admin rule already present"
else
    BACKUP="$NGINX_SITE.bak-$(date +%Y%m%d-%H%M%S)"
    cp "$NGINX_SITE" "$BACKUP"
    # Insert before the first "location / {" of the HTTPS server block.
    # ^~ makes this win over the static-file regex rule, so /admin/assets/* reach the app too.
    awk 'BEGIN{done=0} /^[[:space:]]*location \/ \{/ && !done {
        print "    location ^~ /admin {";
        print "        proxy_pass http://127.0.0.1:8081;";
        print "        proxy_set_header Host $host;";
        print "        proxy_set_header X-Real-IP $remote_addr;";
        print "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;";
        print "        proxy_set_header X-Forwarded-Proto $scheme;";
        print "        proxy_set_header X-Forwarded-Host $host;";
        print "        client_max_body_size 60m;";
        print "    }";
        print "";
        done=1 } { print }' "$BACKUP" > "$NGINX_SITE"
    if nginx -t 2>/dev/null; then
        systemctl reload nginx
        echo "    added (backup: $BACKUP)"
    else
        cp "$BACKUP" "$NGINX_SITE"
        echo "    nginx -t FAILED - original config restored, nothing changed:"
        nginx -t || true
        exit 1
    fi
fi

echo "==> 5/5 Check"
for u in /admin /admin/login /admin/api/about; do
    printf "    %-20s %s\n" "$u" "$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' https://skyspireinfotech.com$u)"
done

if [ "$FIRST_RUN" = 1 ]; then
    echo
    echo "================================================================"
    echo " Admin login:  https://skyspireinfotech.com/admin"
    echo " Username:     admin"
    echo " Password:     $(grep ADMIN_SEED_PASSWORD "$ENV_FILE" | cut -d= -f2)"
    echo " Change it after first login: Profile -> Change password."
    echo "================================================================"
fi
