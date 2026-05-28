from flask import Blueprint, request, jsonify
from admin.models import Artikel
from ..utils.auth_helper import token_required
from ..utils.date_helper import format_tanggal

artikel_bp = Blueprint("artikel_api", __name__)


@artikel_bp.route("/artikel", methods=["GET"])
@token_required
def get_artikel(user_id):
    artikels = Artikel.query.order_by(Artikel.created_at.desc()).all()

    results = []
    for p in artikels:
        results.append({
            "id":         p.id,
            "judul":      p.judul,
            "deskripsi":  p.deskripsi,
            "referensi":  p.referensi,
            "gambar":     f"{request.host_url}static/uploads/{p.gambar}" if p.gambar else None,
            "created_at": format_tanggal(p.created_at)
        })

    return jsonify(results)