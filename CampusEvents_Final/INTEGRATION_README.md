# Campus Events: Android + Flask integration

## What was connected
- Android manifest INTERNET permission and HTTP cleartext allowance for local development.
- `ApiClient.java` shared HTTP helper.
- Login sends email/password to Flask `/api/login` and stores returned user id/name/email locally.
- Student home fetches `/api/events`, renders up to three events in the existing cards, and filters categories.
- Event details fetches `/api/events/<id>` and registration posts to `/api/events/<id>/register`.

## Run the backend
In PowerShell, open `CampusEvents_Backend`:
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python app.py
```

## Network configuration
`ApiClient.java` currently uses `http://192.168.1.6:5000`.
If your laptop's Wi-Fi IPv4 address changes, update `BASE_URL` there and rebuild the APK.
Keep the phone and laptop on the same Wi-Fi. Keep Flask running.

## First use
1. Start Flask.
2. Create a student account through the backend `POST /api/register` endpoint (the supplied login screen has no sign-up form yet).
3. Sign in with that email/password.
4. Add events through the backend `POST /api/events` endpoint.
5. Open Student Home and tap an event to see details/register.

## Important
This is a local learning/demo setup, not production-ready. Backend admin routes are not authenticated, JSON storage is not designed for concurrent production traffic, and HTTP is unencrypted. Do not expose it to the public internet.
