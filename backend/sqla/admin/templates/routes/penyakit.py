from flask import Blueprint, request, jsonify
from admin.models import DeskripsiPenyakit

penyakit_bp = Blueprint("penyakit_api", __name__)


@penyakit_bp.route("/deskripsipenyakit", methods=["GET"])
def get_deskripsi_penyakit():
    nama_penyakit = request.args.get("penyakit")
    if not nama_penyakit:
        return jsonify({"error": "Parameter 'penyakit' wajib diisi"}), 400

    penyakit = DeskripsiPenyakit.query.filter_by(penyakit=nama_penyakit).first()
    if not penyakit:
        return jsonify({"error": "Penyakit tidak ditemukan"}), 404

    return jsonify({
        "id":        penyakit.id,
        "penyakit":  penyakit.penyakit,
        "deskripsi": penyakit.deskripsi
    })