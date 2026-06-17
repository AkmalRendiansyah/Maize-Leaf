from admin import app, db
from admin.templates.routes import register_routes
import admin_views  
from flask import redirect, url_for, render_template, request, session, flash
from werkzeug.security import check_password_hash
from admin.models import User
from werkzeug.middleware.proxy_fix import ProxyFix  


register_routes(app)

app.wsgi_app = ProxyFix(app.wsgi_app, x_proto=1, x_host=1)  


@app.route("/", methods=["GET", "POST"])
def login():
  
    if session.get("admin_logged_in"):
        return redirect(url_for("admin.index"))

    error_message = None
    if request.method == "POST":
        email = request.form.get("email")
        password = request.form.get("password")

        if not email or not password:
            error_message = "Email dan password wajib diisi."
        else:
      
            user = User.query.filter_by(email=email).first()
            
        
            if not user or not check_password_hash(user.password, password):
                error_message = "Email atau password salah."
            elif not user.status:
                error_message = "Akun belum aktif atau belum diverifikasi."
            elif user.role_id != 1:
                error_message = "Anda tidak memiliki akses sebagai admin."
            else:
                
                session["admin_logged_in"] = True
                session["admin_user_id"] = user.id
                session["admin_username"] = user.username
                
                next_url = request.args.get("next")
                return redirect(next_url or url_for("admin.index"))

    return render_template("admin/login.html", error_message=error_message)


@app.route("/logout")
def logout():

    session.pop("admin_logged_in", None)
    session.pop("admin_user_id", None)
    session.pop("admin_username", None)
    flash("Anda telah berhasil keluar.", "info")
    return redirect(url_for("login"))


@app.route("/favicon.ico")
def favicon():
    return redirect(url_for("static", filename="favicon.ico"))



if __name__ == "__main__":
    with app.app_context():
        db.create_all()
    # app.run(debug=True)
    app.run(host='0.0.0.0', port=5000, debug=True)