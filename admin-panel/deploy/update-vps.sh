#!/usr/bin/env bash
# Update skyspireinfotech.com on the VPS (run as root after uploading to /tmp):
#   /tmp/admin-panel.jar  -> new admin panel (optional)
#   /tmp/dist.tar.gz      -> new website build (optional)
#
#   bash /tmp/update-vps.sh
#
# Backs up what it replaces; touches only the Skyspire admin service, the
# skyspireinfotech.com web folder, and the skyspireinfotech.com nginx site.
set -euo pipefail

APP_DIR=/opt/skyspire-admin
SERVICE=skyspire-admin
WEB=/var/www/skyspireinfotech.com/html
NGINX_SITE=/etc/nginx/sites-available/skyspireinfotech.com
STAMP=$(date +%Y%m%d-%H%M%S)

if [ -f /tmp/admin-panel.jar ]; then
    echo "==> Admin panel"
    cp "$APP_DIR/admin-panel.jar" "$APP_DIR/admin-panel.jar.prev"
    cp /tmp/admin-panel.jar "$APP_DIR/admin-panel.jar"
    chown "$SERVICE:$SERVICE" "$APP_DIR/admin-panel.jar"
    systemctl restart "$SERVICE"
    echo -n "    restarting"
    for i in $(seq 1 60); do
        if curl -s -o /dev/null http://127.0.0.1:8081/admin/login; then echo " - up"; break; fi
        echo -n "."; sleep 2
        if [ "$i" = 60 ]; then
            echo; echo "    New version did not start - rolling back to the previous jar."
            journalctl -u "$SERVICE" -n 30 --no-pager || true
            cp "$APP_DIR/admin-panel.jar.prev" "$APP_DIR/admin-panel.jar"
            systemctl restart "$SERVICE"
            exit 1
        fi
    done
    rm -f /tmp/admin-panel.jar
fi

echo "==> nginx upload limit for /admin (hero videos up to 50 MB)"
if grep -q "client_max_body_size 6m;" "$NGINX_SITE"; then
    cp "$NGINX_SITE" "$NGINX_SITE.bak-$STAMP"
    sed -i 's/client_max_body_size 6m;/client_max_body_size 60m;/' "$NGINX_SITE"
    if nginx -t 2>/dev/null; then
        systemctl reload nginx
        echo "    raised to 60m"
    else
        cp "$NGINX_SITE.bak-$STAMP" "$NGINX_SITE"
        echo "    nginx -t failed - original config restored"
        nginx -t || true
        exit 1
    fi
else
    echo "    already set ($(grep -o 'client_max_body_size [0-9a-z]*' "$NGINX_SITE" | head -1))"
fi

if [ -f /tmp/dist.tar.gz ]; then
    echo "==> Website"
    cp -a "$WEB" "${WEB%/html}/html-backup-$STAMP"
    rm -rf /tmp/skyspire-new && mkdir /tmp/skyspire-new
    tar -xzf /tmp/dist.tar.gz -C /tmp/skyspire-new
    cp -a /tmp/skyspire-new/. "$WEB/"
    chown -R www-data:www-data "$WEB"
    rm -f /tmp/dist.tar.gz
    echo "    updated (backup: ${WEB%/html}/html-backup-$STAMP)"
fi

echo "==> Check"
for u in /admin/login /admin/api/home /; do
    printf "    %-18s %s\n" "$u" "$(curl -s -o /dev/null -w '%{http_code}' https://skyspireinfotech.com$u)"
done
echo "    hero media: $(curl -s https://skyspireinfotech.com/admin/api/home)"
