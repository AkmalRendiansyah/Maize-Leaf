from flask import Blueprint, request, jsonify
from admin import db
from admin.models import Posting, Komentar
from ..utils.auth_helper import token_required
from ..utils.date_helper import format_tanggal
from ..utils.upload_helper import save_file

posting_bp = Blueprint("posting_api", __name__)



@posting_bp.route("/posting", methods=["POST"])
@token_required
def save_postchat(user_id):
    deskripsi = request.form.get("deskripsi")
    file      = request.files.get("gambar")
    filename  = save_file(file)

    posting = Posting(id_user=user_id, deskripsi=deskripsi, gambar=filename)
    db.session.add(posting)
    db.session.commit()

    return jsonify({
        "msg": "Postingan saved successfully",
        "posting": {
            "id_user":    user_id,
            "deskripsi":  deskripsi,
            "gambar":     filename,
            "created_at": posting.created_at.isoformat()
        }
    }), 201


@posting_bp.route("/posting", methods=["GET"])
@token_required
def get_posting(user_id):
    postchats = Posting.query.order_by(Posting.created_at.desc()).all()

    results = []
    for p in postchats:
        sum_komentar = Komentar.query.filter_by(id_posting=p.id).count()
        results.append({
            "id":              p.id,
            "username":        p.user.username,
            "deskripsi":       p.deskripsi,
            "jumlah_komentar": sum_komentar,
            "gambar":          f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at":      format_tanggal(p.created_at)
        })

    return jsonify(results)


@posting_bp.route("/posting/<int:id>", methods=["GET"])
@token_required
def get_posting_by_id(user_id, id):
    posting = Posting.query.filter_by(id=id).first()
    if not posting:
        return jsonify({"msg": "Post Chat not found or unauthorized"}), 404

    return jsonify({
        "id":         posting.id,
        "deskripsi":  posting.deskripsi,
        "gambar":     f"{request.host_url}static/uploads/{posting.gambar}" if posting.gambar else None,
        "created_at": posting.created_at.isoformat()
    })



@posting_bp.route("/komentar", methods=["POST"])
@token_required
def save_komentar(user_id):
    data = request.get_json() if request.is_json else request.form

    id_posting   = data.get("id_posting")
    isikomentar  = data.get("komentar")

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
            "id_user":    user_id,
            "id_posting": id_posting,
            "komentar":   isikomentar,
            "created_at": komentar_baru.created_at.isoformat()
        }
    }), 201


@posting_bp.route("/komentar/<int:id_posting>", methods=["GET"])
@token_required
def get_komentar(user_id, id_posting):
    komentars = Komentar.query.filter_by(id_posting=id_posting)\
                              .order_by(Komentar.created_at.desc()).all()

    results = []
    for c in komentars:
        results.append({
            "id":         c.id,
            "username":   c.user.username,
            "komentar":   c.komentar,
            "created_at": format_tanggal(c.created_at)
        })

    return jsonify(results)