from flask import Blueprint, request, jsonify
from admin import db
from admin.models import History, DeskripsiPenyakit
from ..utils.auth_helper import token_required
from ..utils.date_helper import format_tanggal
from ..utils.upload_helper import save_file, UPLOAD_FOLDER
import os

history_bp = Blueprint("history_api", __name__)


@history_bp.route("/history", methods=["POST"])
@token_required
def save_history(user_id):
    penyakit = request.form.get("penyakit")
    file     = request.files.get("gambar")

    if not penyakit:
        return jsonify({"msg": "Penyakit is required"}), 400

    deskripsi_record = DeskripsiPenyakit.query.filter_by(penyakit=penyakit).first()
    if not deskripsi_record:
        return jsonify({"msg": f"Penyakit '{penyakit}' tidak ditemukan."}), 404

    filename = save_file(file)

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
            "id_user":    user_id,
            "penyakit":   penyakit,
            "deskripsi":  deskripsi_record.deskripsi,
            "gambar":     filename,
            "created_at": history.created_at.isoformat()
        }
    }), 201


@history_bp.route("/history", methods=["GET"])
@token_required
def get_user_history(user_id):
    histories = History.query.filter_by(id_user=user_id).all()

    results = []
    for h in histories:
        results.append({
            "id":         h.id,
            "penyakit":   h.deskripsi_penyakit.penyakit,
            "deskripsi":  h.deskripsi_penyakit.deskripsi,
            "gambar":     f"{request.host_url}static/uploads/{h.gambar}" if h.gambar else None,
            "created_at": format_tanggal(h.created_at)
        })

    return jsonify(results)


@history_bp.route("/history/<int:id>", methods=["DELETE"])
@token_required
def delete_history(user_id, id):
    history = History.query.filter_by(id=id, id_user=user_id).first()
    if not history:
        return jsonify({"msg": "History not found or unauthorized"}), 404

    # Hapus file gambar jika ada
    if history.gambar:
        image_path = os.path.join(UPLOAD_FOLDER, history.gambar)
        if os.path.exists(image_path):
            os.remove(image_path)

    db.session.delete(history)
    db.session.commit()

    return jsonify({"msg": "History deleted successfully"}), 200