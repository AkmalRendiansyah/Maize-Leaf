from flask_admin import Admin
from flask_admin.base import MenuLink
from flask_admin.contrib.sqla import ModelView
from flask_admin.form import FileUploadField
from werkzeug.utils import secure_filename

from flask import redirect, url_for
from werkzeug.security import generate_password_hash, check_password_hash
from admin import app, db
from admin.models import User, DeskripsiPenyakit, History, Posting, Komentar, Artikel
from flask import request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
import jwt
import datetime
import re
import os


JWT_SECRET = "your_jwt_secret_key"  # Ganti dengan secret key yang aman
# Fungsi untuk memverifikasi token JWT registrasi



@app.route("/register", methods=["POST"])
def register():
    if request.is_json:
        data = request.get_json()
    else:
        data = request.form  # untuk application/x-www-form-urlencoded

    username = data.get("username")
    email = data.get("email")
    password = data.get("password")

    # Validasi data kosong
    if not username or not email or not password:
        return jsonify({"error": True, "message": "Missing data"}), 400

    # Validasi format email
    email_regex = r"[^@]+@[^@]+\.[^@]+"
    if not re.match(email_regex, email):
        return jsonify({"error": True, "message": "Invalid email format"}), 400

    # Validasi panjang password
    if len(password) < 8:
        return jsonify({"error": True, "message": "Password must be at least 8 characters"}), 400

    # Cek apakah user/email sudah ada
    if User.query.filter((User.username == username) | (User.email == email)).first():
        return jsonify({"error": True, "message": "Username or email already exists"}), 409

    # Simpan user baru
    hashed_password = generate_password_hash(password)
    user = User(username=username, email=email, password=hashed_password)
    db.session.add(user)
    db.session.commit()

    # (Opsional) Generate token jika masih ingin digunakan
    # token = jwt.encode({
    #     "user_id": user.id,
    #     "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    # }, JWT_SECRET, algorithm="HS256")

    return jsonify({"error": False, "message": "User Created"}), 201

# Fungsi untuk memverifikasi token JWT login
@app.route("/login", methods=["POST"])
def login():
    if request.is_json:
        data = request.get_json()
    else:
        data = request.form  

    email = data.get("email")
    password = data.get("password")

    if not email or not password:
        return jsonify({
            "error": True,
            "message": "Email and password are required"
        }), 400

    user = User.query.filter_by(email=email).first()
    if not user or not check_password_hash(user.password, password):
        return jsonify({
            "error": True,
            "message": "Invalid email or password"
        }), 401

    token = jwt.encode({
        "userId": user.id,
        "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    }, JWT_SECRET, algorithm="HS256")

    return jsonify({
        "error": False,
        "message": "success",
        "loginResult": {
            "userId": f"user-{user.id}",
            "name": user.username,
            "token": token
        }
    }), 200



# ==== SAVE HISTORY ====
@app.route("/history", methods=["POST"])
def save_history():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    penyakit = request.form.get("penyakit")
    file = request.files.get("gambar")

    if not penyakit:
        return jsonify({"msg": "Penyakit is required"}), 400

    deskripsi_record = DeskripsiPenyakit.query.filter_by(penyakit=penyakit).first()
    if not deskripsi_record:
        return jsonify({"msg": f"Penyakit '{penyakit}' tidak ditemukan."}), 404

    filename = None
    if file:
        filename = secure_filename(file.filename)
        file.save(os.path.join(UPLOAD_FOLDER, filename))

    history = History(
        id_user=user_id,
        id_penyakit=deskripsi_record.id,
        gambar=filename
    )
    db.session.add(history)
    db.session.commit()

    return jsonify({
        "msg": "History saved successfully",
        "history": {
            "id_user": user_id,
            "penyakit": penyakit,
            "deskripsi": deskripsi_record.deskripsi,
            "gambar": filename,
            "created_at": history.created_at.isoformat()

        }
    }), 201

@app.route("/history", methods=["GET"])
def get_user_history():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    histories = History.query.filter_by(id_user=user_id).all()
    BULAN = {
    "January": "Januari", "February": "Februari", "March": "Maret",
    "April": "April", "May": "Mei", "June": "Juni",
    "July": "Juli", "August": "Agustus", "September": "September",
    "October": "Oktober", "November": "November", "December": "Desember"
    }

    def format_tanggal(dt):
        bulan = BULAN[dt.strftime("%B")]
        return f"{dt.strftime('%d')} {bulan} {dt.strftime('%Y')}, {dt.strftime('%H:%M')}"

    results = []
    for h in histories:
        results.append({
            "id": h.id,
            "penyakit": h.deskripsi_penyakit.penyakit,
            "deskripsi": h.deskripsi_penyakit.deskripsi,           
            "gambar": f"{request.host_url}static/uploads/{h.gambar}" if h.gambar else None,
            "created_at": format_tanggal(h.created_at)
            
        })

    return jsonify(results)


@app.route("/history/<int:id>", methods=["DELETE"])
def delete_history(id):
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    history = History.query.filter_by(id=id, id_user=user_id).first()
    if not history:
        return jsonify({"msg": "History not found or unauthorized"}), 404

    # Hapus gambar jika ada
    if history.gambar:
        image_path = os.path.join(UPLOAD_FOLDER, history.gambar)
        if os.path.exists(image_path):
            os.remove(image_path)

    db.session.delete(history)
    db.session.commit()

    return jsonify({"msg": "History deleted successfully"}), 200

# Get all deskripsi penyakit
@app.route("/deskripsipenyakit", methods=["GET"])
def get_deskripsi_penyakit():
    nama_penyakit = request.args.get("penyakit") 
    if not nama_penyakit:
        return jsonify({"error": "Parameter 'penyakit' wajib diisi"}), 400

    penyakit = DeskripsiPenyakit.query.filter_by(penyakit=nama_penyakit).first()
    
    if penyakit:
        result = {
            "id": penyakit.id,
            "penyakit": penyakit.penyakit,
            "deskripsi": penyakit.deskripsi
        }
        return jsonify(result)  
    else:
        return jsonify({"error": "Penyakit tidak ditemukan"}), 404

# ==== Post Chat ====
@app.route("/posting", methods=["POST"])
def save_postchat():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    deskripsi = request.form.get("deskripsi")
    file = request.files.get("gambar")

    filename = None
    if file:
        filename = secure_filename(file.filename)
        file.save(os.path.join(UPLOAD_FOLDER, filename))

    posting = Posting(
        id_user=user_id,
        deskripsi=deskripsi,
        gambar=filename
    )
    db.session.add(posting)
    db.session.commit()

    return jsonify({
        "msg": "Postingan saved successfully",
        "posting": {
            "id_user": user_id,
            "deskripsi": deskripsi,
            "gambar": filename,
            "created_at": posting.created_at.isoformat()

        }
    }), 201

@app.route("/posting", methods=["GET"])
def get_posting():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    # postchats = PostChat.query.filter_by(id_user=user_id).all()
    BULAN = {
    "January": "Januari", "February": "Februari", "March": "Maret",
    "April": "April", "May": "Mei", "June": "Juni",
    "July": "Juli", "August": "Agustus", "September": "September",
    "October": "Oktober", "November": "November", "December": "Desember"
    }

    def format_tanggal(dt):
        bulan = BULAN[dt.strftime("%B")]
        return f"{dt.strftime('%d')} {bulan} {dt.strftime('%Y')}, {dt.strftime('%H:%M')}"
    postchats = Posting.query.order_by(Posting.created_at.desc()).all()
 

    results = []
    for p in postchats:
        sum_komentar = Komentar.query.filter(Komentar.id_posting == p.id).count()
        results.append({
            "id": p.id,
            "username" :p.user.username,
            "deskripsi": p.deskripsi,   
            "jumlah_komentar": sum_komentar,
            "gambar": f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at": format_tanggal(p.created_at)
            
        })

    return jsonify(results)

@app.route("/posting/<int:id>", methods=["GET"])
def get_posting_by_id(id):
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    posting = Posting.query.filter_by(id=id).first()
    if not posting:
        return jsonify({"msg": "Post Chat not found or unauthorized"}), 404

    return jsonify({
        "id": posting.id,
        "deskripsi": posting.deskripsi,
        "gambar": f"{request.host_url}static/uploads/{posting.gambar}" if posting.gambar else None,
        "created_at": posting.created_at.isoformat()
    })

@app.route("/komentar", methods=["POST"])
def save_komentar():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    if request.is_json:
        data = request.get_json()
    else:
        data = request.form

    id_posting = data.get("id_posting")
    isikomentar = data.get("komentar")  

    if not id_posting or not isikomentar:
        return jsonify({"msg": "id_posting dan komentar wajib diisi"}), 400
    post = Posting.query.get(id_posting)
    if not post:
        return jsonify({"msg": f"PostChat dengan id {id_posting} tidak ditemukan"}), 404

    komentar_baru = Komentar(
        id_user=user_id,
        id_posting=id_posting,
        komentar=isikomentar
    )
    db.session.add(komentar_baru)
    db.session.commit()

    return jsonify({
        "msg": "Komentar saved successfully",
        "komentar": {
            "id_user": user_id,
            "id_posting": id_posting,
            "komentar": isikomentar,
            "created_at": komentar_baru.created_at.isoformat()
        }
    }), 201

@app.route("/komentar/<int:id_posting>", methods=["GET"])
def get_komentar(id_posting):
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    komentars = Komentar.query.filter_by(id_posting=id_posting).order_by(Komentar.created_at.desc()).all()
    BULAN = {
    "January": "Januari", "February": "Februari", "March": "Maret",
    "April": "April", "May": "Mei", "June": "Juni",
    "July": "Juli", "August": "Agustus", "September": "September",
    "October": "Oktober", "November": "November", "December": "Desember"
    }

    def format_tanggal(dt):
        bulan = BULAN[dt.strftime("%B")]
        return f"{dt.strftime('%d')} {bulan} {dt.strftime('%Y')}, {dt.strftime('%H:%M')}"

    results = []
    for c in komentars:
        results.append({
            "id": c.id,
            # "id_user": c.id_user,
            "username" : c.user.username,
            # "id_posting": c.id_posting,
            "komentar": c.komentar,
            "created_at": format_tanggal(c.created_at)
        })

    return jsonify(results)

@app.route("/artikel", methods=["GET"])
def get_artikel():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]
    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["userId"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    # postchats = PostChat.query.filter_by(id_user=user_id).all()
    artikel = Artikel.query.order_by(Artikel.created_at.desc()).all()
    BULAN = {
    "January": "Januari", "February": "Februari", "March": "Maret",
    "April": "April", "May": "Mei", "June": "Juni",
    "July": "Juli", "August": "Agustus", "September": "September",
    "October": "Oktober", "November": "November", "December": "Desember"
    }

    def format_tanggal(dt):
        bulan = BULAN[dt.strftime("%B")]
        return f"{dt.strftime('%d')} {bulan} {dt.strftime('%Y')}, {dt.strftime('%H:%M')}"

    results = []
    for p in artikel:
        results.append({
            "id": p.id,
            "judul": p.judul,
            "deskripsi": p.deskripsi,           
            "referensi": p.referensi,
            "gambar": f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at": format_tanggal(p.created_at)
            
        })

    return jsonify(results)

# Flask views
@app.route("/")
def index():
    return """
    <p><a href="/admin/">Go to Admin</a></p>
    """

@app.route("/favicon.ico")
def favicon():
    return redirect(url_for("static", filename="/favicon.ico"))

# Custom User admin view
class UserAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "username", "email", "password", "created_at"]
    column_searchable_list = ["username", "email"]
    form_columns = ["username", "email", "password"]
    form_widget_args = {"password": {"type": "password"}}

    def on_model_change(self, form, model, is_created):
        # Always hash the password if it's not already hashed
        password = form.password.data
        if not password.startswith("pbkdf2:sha256:"):
            model.password = generate_password_hash(password)
        else:
            model.password = password
        return super().on_model_change(form, model, is_created)

UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), 'static/uploads')
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

class DeskripsiPenyakitAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "penyakit", "deskripsi","created_at"]
    form_columns = ["penyakit", "deskripsi"]
    column_searchable_list = ["penyakit"]


class HistoryAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "id_user", "id_penyakit", "gambar", "created_at"]
    form_columns = ["id_user", "id_penyakit", "gambar"]
    column_searchable_list = ["id_penyakit", "id_user"]
    form_extra_fields = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }

def on_model_change(self, form, model, is_created):
    deskripsi_record = DeskripsiPenyakit.query.filter_by(penyakit=model.penyakit).first()
    if deskripsi_record:
        model.deskripsi = deskripsi_record.deskripsi or "Deskripsi tidak tersedia"
    else:
        model.deskripsi = "Deskripsi tidak ditemukan"
    return super().on_model_change(form, model, is_created)

class PostingAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "id_user", "deskripsi", "gambar", "created_at"]
    form_columns = ["id_user", "deskripsi", "gambar"]
    column_searchable_list = ["id_user"]
    form_extra_fields = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }

class KomentarAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "id_user", "id_posting", "komentar", "created_at"]
    form_columns = ["id_user", "id_posting", "komentar"]  
    column_searchable_list = ["id_user","id_posting"]

class ArtikelAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "judul", "deskripsi", "referensi", "gambar", "created_at"]
    form_columns = ["judul", "deskripsi", "referensi", "gambar"]
    column_searchable_list = ["judul", "referensi"]
    form_extra_fields = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }
# Create admin
admin = Admin(app, name="Maize Leaf")
admin.add_view(UserAdmin(User, db.session))
admin.add_view(DeskripsiPenyakitAdmin(DeskripsiPenyakit, db.session))
admin.add_view(HistoryAdmin(History, db.session))
admin.add_view(PostingAdmin(Posting, db.session))
admin.add_view(KomentarAdmin(Komentar, db.session))
admin.add_view(ArtikelAdmin(Artikel, db.session))
admin.add_link(MenuLink(name="Back Home", url="/"))