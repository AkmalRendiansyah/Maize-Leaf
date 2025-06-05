from admin import db

from admin.models import User

def build_sample_db():
    """
    Membuat tabel di database sesuai model, tanpa mengisi data dummy.
    """
   
    db.create_all()
    return