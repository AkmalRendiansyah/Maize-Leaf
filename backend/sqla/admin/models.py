from admin import db

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)  # Simpan hash password

    histories = db.relationship('History', backref='user', lazy=True)

    def __repr__(self):
        return f"<User {self.username}>"
    
class Article(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    judul = db.Column(db.String(255), nullable=False)
    isi = db.Column(db.Text, nullable=False)
    gambar = db.Column(db.String(255), nullable=True)

    def __repr__(self):
        return f"<Article {self.judul}>"

class DeskripsiPenyakit(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    penyakit = db.Column(db.String(100), unique=True, nullable=False)
    deskripsi = db.Column(db.Text, nullable=False)

    def __repr__(self):
        return f"<DeskripsiPenyakit {self.penyakit}>"


class History(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    id_user = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    penyakit = db.Column(db.String(100), nullable=False)
    deskripsi = db.Column(db.Text, nullable=False)

    def __repr__(self):
        return f"<History User {self.id_user} - {self.penyakit}>"