from flask import Blueprint, request, jsonify
from admin import db
from admin.models import Posting, Komentar
import os
from ..utils.auth_helper import token_required
from ..utils.date_helper import format_tanggal
from ..utils.upload_helper import save_file,UPLOAD_FOLDER

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
        "msg": "Postingan berhasil disimpan",
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
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 5, type=int)

    pagination = Posting.query.order_by(Posting.created_at.desc()) \
                              .paginate(page=page, per_page=per_page, error_out=False)
    results = []
    for p in pagination.items:
        sum_komentar = Komentar.query.filter_by(id_posting=p.id).count()
        results.append({
            "id":              p.id,
            "username":        p.user.username,
            "deskripsi":       p.deskripsi,
            "jumlah_komentar": sum_komentar,
            "gambar":          f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at":      format_tanggal(p.created_at)
        })

    return jsonify({
        "data":        results,
        "page":        pagination.page,
        "total_pages": pagination.pages,
        "total_items": pagination.total,
        "has_next":    pagination.has_next
    })

@posting_bp.route("/myposting", methods=["GET"])
@token_required
def get_my_posting(user_id):
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 5, type=int)

    pagination = Posting.query.filter_by(id_user=user_id).order_by(Posting.created_at.desc()) \
                              .paginate(page=page, per_page=per_page, error_out=False)
    results = []
    for p in pagination.items:
        sum_komentar = Komentar.query.filter_by(id_posting=p.id).count()
        results.append({
            "id":              p.id,
            "username":        p.user.username,
            "deskripsi":       p.deskripsi,
            "jumlah_komentar": sum_komentar,
            "gambar":          f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at":      format_tanggal(p.created_at)
        })

    return jsonify({
        "data":        results,
        "page":        pagination.page,
        "total_pages": pagination.pages,
        "total_items": pagination.total,
        "has_next":    pagination.has_next
    })



@posting_bp.route("/posting/<int:id>", methods=["GET"])
@token_required
def get_posting_by_id(user_id, id):
    posting = Posting.query.filter_by(id=id).first()
    if not posting:
        return jsonify({"msg": "Postingan tidak ditemukan atau tidak punya hak akses"}), 404

    return jsonify({
        "id":         posting.id,
        "deskripsi":  posting.deskripsi,
        "gambar":     f"{request.host_url}static/uploads/{posting.gambar}" if posting.gambar else None,
        "created_at": posting.created_at.isoformat()
    })

@posting_bp.route("/posting/<int:id>", methods=["DELETE"])
@token_required
def delete_posting(user_id, id):
    posting = Posting.query.filter_by(id=id, id_user=user_id).first()
    if not posting:
        return jsonify({"msg": "Postingan tidak ditemukan atau tidak punya hak akses"}), 404

    if posting.gambar:
        image_path = os.path.join(UPLOAD_FOLDER, posting.gambar)
        if os.path.exists(image_path):
            os.remove(image_path)

    db.session.delete(posting)
    db.session.commit()

    return jsonify({"msg": "Postingan berhasil dihapus"}), 200


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
        "msg": "Komentar berhasil disimpan",
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