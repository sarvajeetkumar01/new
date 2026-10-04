from flask import Flask, jsonify, request, Response
from werkzeug.security import generate_password_hash, check_password_hash
from pathlib import Path
from datetime import datetime
import json, csv, io

app = Flask(__name__)
ROOT = Path(__file__).resolve().parent
DATA = ROOT / "data"
DATA.mkdir(exist_ok=True)
FILES = {n: DATA / f"{n}.json" for n in ("users", "events", "registrations", "attendance")}

for p in FILES.values():
    if not p.exists():
        p.write_text("[]", encoding="utf-8")

def load(name):
    with FILES[name].open(encoding="utf-8") as f:
        return json.load(f)

def save(name, value):
    temp = FILES[name].with_suffix(".tmp")
    with temp.open("w", encoding="utf-8") as f:
        json.dump(value, f, indent=4, ensure_ascii=False)
    temp.replace(FILES[name])

def next_id(items):
    return max((x["id"] for x in items), default=0) + 1

def error(message, code=400):
    return jsonify({"status":"error","message":message}), code

def safe_user(u):
    return {k:u[k] for k in ("id","name","email","role")}

@app.get("/")
@app.get("/api/test")
def test():
    return jsonify({"status":"success","message":"Campus Events API is running"})

@app.post("/api/register")
def register():
    d=request.get_json(silent=True) or {}
    name=str(d.get("name","")).strip()
    email=str(d.get("email","")).strip().lower()
    password=str(d.get("password",""))
    if not name or not email or not password: return error("Name, email and password are required")
    if len(password)<6: return error("Password must contain at least 6 characters")
    users=load("users")
    if any(u["email"].lower()==email for u in users): return error("Email already registered",409)
    u={"id":next_id(users),"name":name,"email":email,
       "password":generate_password_hash(password),"role":"student"}
    users.append(u); save("users",users)
    return jsonify({"status":"success","message":"Registration successful","user":safe_user(u)}),201

@app.post("/api/login")
def login():
    d=request.get_json(silent=True) or {}
    email=str(d.get("email","")).strip().lower()
    password=str(d.get("password",""))
    if not email or not password: return error("Email and password are required")
    for u in load("users"):
        if u["email"].lower()==email and check_password_hash(u["password"],password):
            return jsonify({"status":"success","message":"Login successful","user":safe_user(u)})
    return error("Invalid email or password",401)

@app.get("/api/users/<int:uid>")
def user_profile(uid):
    u=next((x for x in load("users") if x["id"]==uid),None)
    return (jsonify({"status":"success","user":safe_user(u)}) if u else error("User not found",404))

@app.get("/api/events")
def events_list():
    items=load("events")
    cat=request.args.get("category","").strip().lower()
    q=request.args.get("search","").strip().lower()
    if cat and cat!="all": items=[e for e in items if e.get("category","").lower()==cat]
    if q: items=[e for e in items if q in e.get("title","").lower() or q in e.get("description","").lower() or q in e.get("venue","").lower()]
    return jsonify({"status":"success","count":len(items),"events":items})

@app.get("/api/events/<int:eid>")
def event_detail(eid):
    e=next((x for x in load("events") if x["id"]==eid),None)
    return jsonify({"status":"success","event":e}) if e else error("Event not found",404)

@app.post("/api/events")
def create_event():
    d=request.get_json(silent=True) or {}
    title=str(d.get("title","")).strip()
    if not title: return error("Event title is required")
    try:
        cap=int(d.get("capacity",0))
        if cap<0: return error("Capacity cannot be negative")
    except (TypeError,ValueError): return error("Capacity must be a number")
    items=load("events")
    e={"id":next_id(items),"title":title,"category":str(d.get("category","Other")),
       "date":str(d.get("date","")),"time":str(d.get("time","")),
       "venue":str(d.get("venue","")),"description":str(d.get("description","")),
       "capacity":cap,"created_at":datetime.now().isoformat(timespec="seconds")}
    items.append(e); save("events",items)
    return jsonify({"status":"success","message":"Event created","event":e}),201

@app.put("/api/events/<int:eid>")
def update_event(eid):
    d=request.get_json(silent=True) or {}; items=load("events")
    for e in items:
        if e["id"]==eid:
            for k in ("title","category","date","time","venue","description"):
                if k in d: e[k]=str(d[k])
            if "capacity" in d:
                try:
                    cap=int(d["capacity"])
                    if cap<0: return error("Capacity cannot be negative")
                    e["capacity"]=cap
                except (TypeError,ValueError): return error("Capacity must be a number")
            if not e.get("title","").strip(): return error("Event title is required")
            save("events",items)
            return jsonify({"status":"success","message":"Event updated","event":e})
    return error("Event not found",404)

@app.delete("/api/events/<int:eid>")
def delete_event(eid):
    items=load("events"); updated=[e for e in items if e["id"]!=eid]
    if len(items)==len(updated): return error("Event not found",404)
    save("events",updated)
    for name in ("registrations","attendance"):
        save(name,[x for x in load(name) if x["event_id"]!=eid])
    return jsonify({"status":"success","message":"Event and related records deleted"})

@app.post("/api/events/<int:eid>/register")
def register_for_event(eid):
    d=request.get_json(silent=True) or {}
    try: uid=int(d.get("user_id"))
    except (TypeError,ValueError): return error("Valid user_id is required")
    users,events,regs=load("users"),load("events"),load("registrations")
    if not any(u["id"]==uid for u in users): return error("User not found",404)
    event=next((e for e in events if e["id"]==eid),None)
    if not event: return error("Event not found",404)
    if any(r["user_id"]==uid and r["event_id"]==eid for r in regs): return error("Already registered",409)
    if event.get("capacity",0)>0 and sum(r["event_id"]==eid for r in regs)>=event["capacity"]: return error("Event is full",409)
    r={"id":next_id(regs),"user_id":uid,"event_id":eid,"registered_at":datetime.now().isoformat(timespec="seconds")}
    regs.append(r); save("registrations",regs)
    return jsonify({"status":"success","message":"Registration successful","registration":r}),201

@app.get("/api/my-events/<int:uid>")
def my_events(uid):
    ids={r["event_id"] for r in load("registrations") if r["user_id"]==uid}
    return jsonify({"status":"success","events":[e for e in load("events") if e["id"] in ids]})

@app.delete("/api/events/<int:eid>/register/<int:uid>")
def cancel_registration(eid,uid):
    regs=load("registrations"); updated=[r for r in regs if not (r["event_id"]==eid and r["user_id"]==uid)]
    if len(regs)==len(updated): return error("Registration not found",404)
    save("registrations",updated)
    return jsonify({"status":"success","message":"Registration cancelled"})

@app.get("/api/events/<int:eid>/participants")
def participants(eid):
    users,regs=load("users"),load("registrations"); result=[]
    for r in regs:
        if r["event_id"]==eid:
            u=next((x for x in users if x["id"]==r["user_id"]),None)
            if u: result.append({"user_id":u["id"],"name":u["name"],"email":u["email"],"registered_at":r["registered_at"]})
    return jsonify({"status":"success","count":len(result),"participants":result})

@app.post("/api/attendance")
def mark_attendance():
    d=request.get_json(silent=True) or {}
    try: eid,uid=int(d.get("event_id")),int(d.get("user_id"))
    except (TypeError,ValueError): return error("Valid event_id and user_id are required")
    if not any(e["id"]==eid for e in load("events")): return error("Event not found",404)
    if not any(u["id"]==uid for u in load("users")): return error("User not found",404)
    regs,att=load("registrations"),load("attendance")
    if not any(r["event_id"]==eid and r["user_id"]==uid for r in regs): return error("Student is not registered",409)
    if any(a["event_id"]==eid and a["user_id"]==uid for a in att): return error("Attendance already marked",409)
    a={"id":next_id(att),"event_id":eid,"user_id":uid,"present":True,"marked_at":datetime.now().isoformat(timespec="seconds")}
    att.append(a); save("attendance",att)
    return jsonify({"status":"success","message":"Attendance marked","attendance":a}),201

@app.get("/api/admin/dashboard")
def dashboard():
    return jsonify({"status":"success","total_users":len(load("users")),"total_events":len(load("events")),
                    "total_registrations":len(load("registrations")),"total_attendance":len(load("attendance"))})

@app.get("/api/events/<int:eid>/export")
def export_csv(eid):
    users,regs=load("users"),load("registrations"); out=io.StringIO(); writer=csv.writer(out)
    writer.writerow(["User ID","Name","Email","Registration Date"])
    for r in regs:
        if r["event_id"]==eid:
            u=next((x for x in users if x["id"]==r["user_id"]),None)
            if u: writer.writerow([u["id"],u["name"],u["email"],r["registered_at"]])
    return Response(out.getvalue(),mimetype="text/csv",headers={"Content-Disposition":f"attachment; filename=event_{eid}_participants.csv"})

@app.post("/api/chat")
def chat():
    d=request.get_json(silent=True) or {}; msg=str(d.get("message","")).strip().lower()
    if not msg: return error("Message is required")
    events=load("events")
    if "workshop" in msg: matches=[e for e in events if e.get("category","").lower()=="workshop"]
    elif "seminar" in msg: matches=[e for e in events if e.get("category","").lower()=="seminar"]
    else: matches=events
    reply=("Here are matching events:\n"+"\n".join(f'{e["title"]} | {e.get("date","")} | {e.get("venue","")}' for e in matches)) if matches else "No matching events are currently listed."
    return jsonify({"status":"success","reply":reply})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
