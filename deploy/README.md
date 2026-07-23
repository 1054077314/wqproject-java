# Deployment

This directory keeps the ECS deployment process reproducible without storing production secrets.

## Server Layout

```text
/opt/campus-share/
├── app/campus-share-1.0.0.jar
├── frontend/dist/
├── logs/app.log
└── media/

/etc/campus-share/campus-share.env
/etc/systemd/system/campus-share.service
/etc/nginx/sites-available/campus-share
```

## First-Time Setup

1. Install JDK 17+, MySQL, Nginx and systemd on the ECS host.
2. Create the database and user in MySQL.
3. Copy `campus-share.env.example` to `/etc/campus-share/campus-share.env`, then fill real database credentials on the server.
4. Copy `campus-share.service.example` to `/etc/systemd/system/campus-share.service`.
5. Copy `nginx-campus-share.conf.example` to `/etc/nginx/sites-available/campus-share`, enable it from `sites-enabled`, then test Nginx.
   - No domain: use the default HTTP config (security headers included).
   - Optional demo HTTPS on public IP only: use `nginx-campus-share-tls-selfsigned.conf.example` (self-signed; browsers warn). Let's Encrypt needs a domain.

```bash
sudo systemctl daemon-reload
sudo systemctl enable campus-share
sudo nginx -t
sudo systemctl reload nginx
```

## Deploy

Run from the repository root on your local machine:

```bash
DEPLOY_HOST=root@120.26.174.97 bash deploy/deploy.sh
```

The script builds the Spring Boot jar, builds the React frontend, uploads both artifacts, backs up the previous release, replaces files under `/opt/campus-share`, restarts `campus-share`, and reloads Nginx.

## Runtime Ports

- Nginx: `80` (optional `443` with self-signed cert)
- Spring Boot API: `8085`
- MySQL: `3306` on the ECS host, usually bound to `127.0.0.1`
- API docs: `http://127.0.0.1:8085/swagger-ui.html` (prefer SSH tunnel in production)

## Health Checks

```bash
systemctl status campus-share
curl http://127.0.0.1:8085/actuator/health
curl http://127.0.0.1:8085/api/categories/
curl http://120.26.174.97/
```
