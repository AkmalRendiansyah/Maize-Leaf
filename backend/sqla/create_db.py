from admin import app, db
from admin.models import *

with app.app_context():
    db.create_all()
    print("Semua tabel berhasil dibuat!")