# Campus Events Backend

## Setup (Windows PowerShell)

```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
python app.py
```

Test: http://127.0.0.1:5000/api/test

## APIs
GET /api/test
POST /api/register
POST /api/login
GET /api/users/<user_id>
GET /api/events
GET /api/events/<event_id>
POST /api/events
PUT /api/events/<event_id>
DELETE /api/events/<event_id>
POST /api/events/<event_id>/register
GET /api/my-events/<user_id>
DELETE /api/events/<event_id>/register/<user_id>
GET /api/events/<event_id>/participants
POST /api/attendance
GET /api/admin/dashboard
GET /api/events/<event_id>/export
POST /api/chat

JSON body examples:
Register: {"name":"Demo Student","email":"demo@example.com","password":"123456"}
Login: {"email":"demo@example.com","password":"123456"}
Create event: {"title":"Tech Fest","category":"Workshop","date":"2026-11-10","time":"10:00 AM","venue":"Seminar Hall","description":"Demo event","capacity":100}
Register event: {"user_id":1}
Attendance: {"event_id":1,"user_id":1}
Chat: {"message":"What events are available?"}

Passwords are stored as hashes. This is a local learning/demo backend; authorization is not yet enforced and JSON files are not suitable for concurrent production traffic. Do not expose it publicly as-is.
