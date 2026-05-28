# import os
# import os.path as op

# from admin import app
# from admin.data import build_sample_db
# from jinja2 import StrictUndefined

# # # Untuk MySQL, tidak perlu membuat file database SQLite
# # # Jika ingin mengisi sample data, cukup panggil build_sample_db di dalam app context
# # with app.app_context():
# #     build_sample_db()

# if __name__ == "__main__":
#     # Start app
#     app.jinja_env.undefined = StrictUndefined
#     app.run(host='0.0.0.0', port=5000, debug=True)

from admin import app, db
from admin.templates.routes import register_routes
import admin_views  
from flask import redirect, url_for

# Daftarkan semua blueprint route
register_routes(app)


# ── Halaman utama ──────────────────────────────────────────────────────────────

@app.route("/")
def index():
    return '<p><a href="/admin/">Go to Admin</a></p>'


@app.route("/favicon.ico")
def favicon():
    return redirect(url_for("static", filename="favicon.ico"))


# ── Entry point ────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    with app.app_context():
        db.create_all()
    app.run(debug=True)