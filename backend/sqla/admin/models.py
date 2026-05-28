from admin import db  
from datetime import datetime

class User(db.Model):
    __tablename__ = "user"
    
    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    email = db.Column(db.String(100), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False) 
    status = db.Column(db.Boolean, default=False, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    histories = db.relationship('History', backref='user', lazy=True)
    postings = db.relationship('Posting', backref='user', lazy=True)  
    komentars = db.relationship('Komentar', backref='user', lazy=True) 
    def __repr__(self):
        return f"<User {self.username}>"


class DeskripsiPenyakit(db.Model):
    __tablename__ = "deskripsi_penyakit"

    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True)
    penyakit = db.Column(db.String(50), unique=True, nullable=False)
    deskripsi = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    histories = db.relationship('History', backref='deskripsi_penyakit', lazy=True)

    def __repr__(self):
        return f"<DeskripsiPenyakit {self.penyakit}>"


class History(db.Model):
    __tablename__ = "history"

    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True)
    id_user = db.Column(db.SmallInteger, db.ForeignKey('user.id'), nullable=False)
    id_penyakit = db.Column(db.SmallInteger, db.ForeignKey('deskripsi_penyakit.id'), nullable=False)
    gambar = db.Column(db.String(255), nullable=True) 
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<History User {self.id_user} - {self.penyakit}>"

class Posting(db.Model):
    __tablename__ = "posting"

    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True)
    id_user = db.Column(db.SmallInteger, db.ForeignKey('user.id'), nullable=False)
    gambar = db.Column(db.String(255), nullable=True)
    deskripsi = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)


    komentars = db.relationship('Komentar', backref='posting', lazy=True)

    def __repr__(self):
        return f"<Posting {self.id}>"


class Komentar(db.Model):
    __tablename__ = "komentar"

    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True)
    id_user = db.Column(db.SmallInteger, db.ForeignKey('user.id'), nullable=False)
    id_posting = db.Column(db.SmallInteger, db.ForeignKey('posting.id'), nullable=False)
    komentar = db.Column(db.String(255), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<Komentar User {self.id_user}>"

class Artikel(db.Model):
    __tablename__ = "artikel"

    id = db.Column(db.SmallInteger, primary_key=True, autoincrement=True) 
    judul = db.Column(db.String(100), nullable=True)
    gambar = db.Column(db.String(255), nullable=True)
    deskripsi = db.Column(db.Text, nullable=True)
    referensi = db.Column(db.String(255), nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
        
    def __repr__(self):
        return f"<Artikel {self.id}>"

class OTP(db.Model):
    __tablename__ = "otp"

    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    id_user    = db.Column(db.SmallInteger, db.ForeignKey('user.id'), nullable=False)
    kode       = db.Column(db.String(6), nullable=False)        
    expired_at = db.Column(db.DateTime, nullable=False)         
    is_used    = db.Column(db.Boolean, default=False)           
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User', backref='otp')
