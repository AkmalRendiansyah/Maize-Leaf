from admin import app, db
from admin.models import *  # load semua model

with app.app_context():
    db.create_all()
    print("Semua tabel berhasil dibuat!")