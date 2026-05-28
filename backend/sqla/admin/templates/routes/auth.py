from flask import Blueprint, request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
from admin import db
from admin.models import User, OTP
from ..utils.auth_helper import JWT_SECRET
from ..utils.email_helper import send_otp_email
import jwt
import datetime
import random
import re

auth_bp = Blueprint("auth_api", __name__)


def _buat_otp(user_id: int) -> str:
  
    # Hapus OTP lama milik user ini yang belum dipakai
    OTP.query.filter_by(id_user=user_id, is_used=False).delete()

    kode = str(random.randint(100000, 999999))
    expired = datetime.datetime.utcnow() + datetime.timedelta(minutes=5)
    otp = OTP(id_user=user_id, kode=kode, expired_at=expired)
    db.session.add(otp)
    db.session.commit()
    return kode


# ── Register ───────────────────────────────────────────────────────────────────

@auth_bp.route("/register", methods=["POST"])
def register():
    data = request.get_json() if request.is_json else request.form

    username = data.get("username")
    email    = data.get("email")
    password = data.get("password")

    if not username or not email or not password:
        return jsonify({"error": True, "message": "Missing data"}), 400

    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        return jsonify({"error": True, "message": "Invalid email format"}), 400

    if len(password) < 8:
        return jsonify({"error": True, "message": "Password must be at least 8 characters"}), 400

    if User.query.filter((User.username == username) | (User.email == email)).first():
        return jsonify({"error": True, "message": "Username or email already exists"}), 409

    # Simpan user baru dengan status=False (belum terverifikasi)
    user = User(
        username=username,
        email=email,
        password=generate_password_hash(password),
        status=False
    )
    db.session.add(user)
    db.session.commit()

    # Buat & kirim OTP
    kode = _buat_otp(user.id)
    send_otp_email(username, email, kode)

    return jsonify({
        "error": False,
        "message": "Registrasi berhasil. Kode OTP telah dikirim ke email kamu.",
        "userId": user.id
    }), 201


# ── Verify OTP ─────────────────────────────────────────────────────────────────

@auth_bp.route("/verify-otp", methods=["POST"])
def verify_otp():
    data = request.get_json() if request.is_json else request.form

    user_id = data.get("userId")
    kode    = data.get("otp")

    if not user_id or not kode:
        return jsonify({"error": True, "message": "userId dan otp wajib diisi"}), 400

    otp = OTP.query.filter_by(
        id_user=user_id,
        kode=kode,
        is_used=False
    ).first()

    if not otp:
        return jsonify({"error": True, "message": "Kode OTP tidak valid"}), 400

    if datetime.datetime.utcnow() > otp.expired_at:
        return jsonify({"error": True, "message": "Kode OTP sudah kedaluwarsa"}), 400

    # Tandai OTP sudah dipakai & aktifkan user
    otp.is_used = True
    user = User.query.get(user_id)
    user.status = True
    token = jwt.encode(
        {
            "userId": user.id,
            "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
        },
        JWT_SECRET,
        algorithm="HS256"
    )
    db.session.commit()
    return jsonify({
        "error": False,
        "message": "Akun berhasil diverifikasi",
        "userId": user.id,
        "token": token
        
    }), 200


# ── Resend OTP ─────────────────────────────────────────────────────────────────

@auth_bp.route("/resend-otp", methods=["POST"])
def resend_otp():
    data    = request.get_json() if request.is_json else request.form
    user_id = data.get("userId")

    if not user_id:
        return jsonify({"error": True, "message": "userId wajib diisi"}), 400

    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": True, "message": "User tidak ditemukan"}), 404

    if user.status:
        return jsonify({"error": True, "message": "Akun sudah aktif"}), 400

    kode = _buat_otp(user.id)
    send_otp_email(user.username, user.email, kode)

    return jsonify({"error": False, "message": "Kode OTP baru telah dikirim"}), 200


# ── Login ──────────────────────────────────────────────────────────────────────

@auth_bp.route("/login", methods=["POST"])
def login():
    data = request.get_json() if request.is_json else request.form

    email    = data.get("email")
    password = data.get("password")

    if not email or not password:
        return jsonify({"error": True, "message": "Email and password are required"}), 400

    user = User.query.filter_by(email=email).first()
    if not user or not check_password_hash(user.password, password):
        return jsonify({"error": True, "message": "Invalid email or password"}), 401

    # Cek apakah user sudah verifikasi OTP
    if not user.status:
        kode = _buat_otp(user.id)
        send_otp_email(user.username, user.email, kode)
        return jsonify({
            "error": True,
            "message": "Akun belum diverifikasi. Silakan cek email kamu.",
            "userId": user.id
        }), 403

    token = jwt.encode(
        {
            "userId": user.id,
            "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
        },
        JWT_SECRET,
        algorithm="HS256"
    )

    return jsonify({
        "error": False,
        "message": "success",
        "loginResult": {
            "userId": f"user-{user.id}",
            "name": user.username,
            "token": token
        }
    }), 200