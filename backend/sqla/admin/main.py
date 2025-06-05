from flask_admin import Admin
from flask_admin.base import MenuLink
from flask_admin.contrib.sqla import ModelView
from flask_admin.form import FileUploadField
from werkzeug.utils import secure_filename
from flask import redirect, url_for
from werkzeug.security import generate_password_hash, check_password_hash
from admin import app, db
from admin.models import User,Article,DeskripsiPenyakit, History
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
    data = request.get_json()
    username = data.get("username")
    email = data.get("email")
    password = data.get("password")

    # Validasi data kosong
    if not username or not email or not password:
        return jsonify({"msg": "Missing data"}), 400

    # Validasi format email
    email_regex = r"[^@]+@[^@]+\.[^@]+"
    if not re.match(email_regex, email):
        return jsonify({"msg": "Invalid email format"}), 400

    # Validasi panjang password
    if len(password) < 8:
        return jsonify({"msg": "Password must be at least 8 characters"}), 400

    # Cek apakah user/email sudah ada
    if User.query.filter((User.username == username) | (User.email == email)).first():
        return jsonify({"msg": "Username or email already exists"}), 409

    hashed_password = generate_password_hash(password)
    user = User(username=username, email=email, password=hashed_password)
    db.session.add(user)
    db.session.commit()

    # Generate JWT token
    token = jwt.encode({
        "user_id": user.id,
        "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    }, JWT_SECRET, algorithm="HS256")

    return jsonify({"token": token, "msg": "Registration successful"})

# Fungsi untuk memverifikasi token JWT login
@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    username = data.get("username")
    password = data.get("password")

    user = User.query.filter_by(username=username).first()
    if not user or not check_password_hash(user.password, password):
        return jsonify({"msg": "Invalid username or password"}), 401

    # Generate JWT token
    token = jwt.encode({
        "user_id": user.id,
        "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    }, JWT_SECRET, algorithm="HS256")

    return jsonify({"token": token})


# Get all articles
@app.route("/articles", methods=["GET"])
def get_articles():
    articles = Article.query.all()
    results = []
    for article in articles:
        results.append({
            "id": article.id,
            "judul": article.judul,
            "isi": article.isi,
            "gambar": article.gambar  # sertakan path/URL gambar
        })
    return jsonify(results)

@app.route("/articles/<int:id>", methods=["GET"])
def get_article(id):
    article = Article.query.get_or_404(id)
    return jsonify({
        "id": article.id,
        "judul": article.judul,
        "isi": article.isi,
        "gambar": article.gambar
    })

# ==== SAVE HISTORY ====

@app.route("/history", methods=["POST"])
def save_history():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]

    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["user_id"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    data = request.get_json()
    penyakit = data.get("penyakit")

    if not penyakit:
        return jsonify({"msg": "Penyakit is required"}), 400

    deskripsi_record = DeskripsiPenyakit.query.filter_by(penyakit=penyakit).first()
    if deskripsi_record is None:
        return jsonify({"msg": f"Penyakit '{penyakit}' tidak ditemukan di database."}), 404

    if not deskripsi_record.deskripsi:
        return jsonify({"msg": f"Deskripsi penyakit '{penyakit}' kosong di database."}), 500

    history = History(
        id_user=user_id,
        penyakit=penyakit,
        deskripsi=deskripsi_record.deskripsi
    )
    db.session.add(history)
    db.session.commit()

    return jsonify({"msg": "History saved successfully"})


# ==== GET USER HISTORY ====
@app.route("/history", methods=["GET"])
def get_user_history():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"msg": "Missing or invalid token"}), 401

    token = auth_header.split(" ")[1]

    try:
        decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = decoded["user_id"]
    except jwt.ExpiredSignatureError:
        return jsonify({"msg": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"msg": "Invalid token"}), 401

    histories = History.query.filter_by(id_user=user_id).all()
    results = [{
        "id": h.id,
        "penyakit": h.penyakit,
        "deskripsi": h.deskripsi
    } for h in histories]

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
    column_list = ["id", "username", "email", "password"]
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

class ArticleAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "judul", "isi", "gambar"]
    column_searchable_list = ["judul"]
    form_columns = ["judul", "isi", "gambar"]

    # Gunakan file upload untuk gambar
    form_extra_fields = {
        'gambar': FileUploadField('Gambar',
                                 base_path=UPLOAD_FOLDER,
                                 allow_overwrite=False,
                                 namegen=lambda obj, file_data: secure_filename(file_data.filename))
    }

    # Override on_model_change untuk simpan nama file gambar di db
    def on_model_change(self, form, model, is_created):
        if form.gambar.data:
            # form.gambar.data sudah otomatis disimpan ke base_path oleh FileUploadField
            model.gambar = form.gambar.data.filename
        return super().on_model_change(form, model, is_created)

class DeskripsiPenyakitAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "penyakit", "deskripsi"]
    form_columns = ["penyakit", "deskripsi"]
    column_searchable_list = ["penyakit"]


class HistoryAdmin(ModelView):
    can_set_page_size = True
    page_size = 10
    can_view_details = True
    column_list = ["id", "id_user", "penyakit", "deskripsi"]
    form_columns = ["id_user", "penyakit"]  # Jangan isi manual deskripsi
    column_searchable_list = ["penyakit"]

def on_model_change(self, form, model, is_created):
    deskripsi_record = DeskripsiPenyakit.query.filter_by(penyakit=model.penyakit).first()
    if deskripsi_record:
        model.deskripsi = deskripsi_record.deskripsi or "Deskripsi tidak tersedia"
    else:
        model.deskripsi = "Deskripsi tidak ditemukan"
    return super().on_model_change(form, model, is_created)


# Create admin
admin = Admin(app, name="User Admin")
admin.add_view(UserAdmin(User, db.session))
admin.add_view(ArticleAdmin(Article, db.session))
admin.add_view(DeskripsiPenyakitAdmin(DeskripsiPenyakit, db.session))
admin.add_view(HistoryAdmin(History, db.session))
admin.add_link(MenuLink(name="Back Home", url="/"))