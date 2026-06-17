from flask import redirect, url_for, session, request
from flask_admin import Admin, AdminIndexView
from flask_admin.base import MenuLink
from flask_admin.contrib.sqla import ModelView
from flask_admin.form import FileUploadField
from werkzeug.security import generate_password_hash

from admin import app, db
from admin.models import User, DeskripsiPenyakit, History, Posting, Komentar, Artikel, OTP ,Roles
from admin.templates.utils.upload_helper import UPLOAD_FOLDER, ALLOWED_EXTENSIONS

from wtforms.validators import Email, DataRequired
from markupsafe import Markup

class MyAdminIndexView(AdminIndexView):
    def is_accessible(self):
        return session.get('admin_logged_in') == True

    def inaccessible_callback(self, name, **kwargs):
        return redirect(url_for('login', next=request.url))


class SecureModelView(ModelView):
    def is_accessible(self):
        return session.get('admin_logged_in') == True

    def inaccessible_callback(self, name, **kwargs):
       
        return redirect(url_for('login', next=request.url))


class UserAdmin(SecureModelView):
    can_set_page_size    = True
    page_size            = 10
    can_view_details     = True
    column_list          = ["id", "username", "email", "password","status" ,"role_id","created_at"]
    column_searchable_list = ["username", "email"]
    form_columns         = ["username", "email", "password","status","roles"]
    form_widget_args     = {"password": {"type": "password"}}
    column_labels        = {"roles": "Role"}

    form_args = {
        "email": {
            "validators": [DataRequired(), Email(message="Format email tidak valid")]
        }
    }
    
    def on_model_change(self, form, model, is_created):
        password = form.password.data
        if not password.startswith("pbkdf2:sha256:"):
            model.password = generate_password_hash(password)
        return super().on_model_change(form, model, is_created)


class DeskripsiPenyakitAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "penyakit", "deskripsi", "created_at"]
    form_columns           = ["penyakit", "deskripsi"]
    column_searchable_list = ["penyakit"]


class HistoryAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "user", "deskripsi_penyakit", "gambar", "created_at"]
    form_columns           = ["user", "deskripsi_penyakit", "gambar"]
    column_searchable_list = ["id_penyakit", "id_user"]
    column_labels          = {"user": "User", "deskripsi_penyakit": "Penyakit"}
   
    column_formatters = {
        "gambar": lambda v, c, m, p: Markup(
            f'<img src="/static/uploads/{m.gambar}" style="width:80px; height:80px; object-fit:cover; border-radius:6px;">'
        ) if m.gambar else "-"
    }

    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }


class PostingAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "user", "gambar", "deskripsi", "created_at"]
    form_columns           = ["user", "gambar", "deskripsi"]
    column_searchable_list = ["id_user"]
    column_labels          = {"user": "User"}

    column_formatters = {
        "gambar": lambda v, c, m, p: Markup(
            f'<img src="/static/uploads/{m.gambar}" style="width:80px; height:80px; object-fit:cover; border-radius:6px;">'
        ) if m.gambar else "-"
    }

    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }


class KomentarAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "user", "id_posting", "komentar", "created_at"]
    form_columns           = ["user", "posting", "komentar"]
    column_searchable_list = ["id_user", "id_posting"]
    column_labels          = {"user": "User","posting":"Posting"}


class ArtikelAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "judul", "deskripsi", "referensi", "gambar", "created_at"]
    form_columns           = ["judul", "deskripsi", "referensi", "gambar"]
    column_searchable_list = ["judul", "referensi"]

    column_formatters = {
        "gambar": lambda v, c, m, p: Markup(
            f'<img src="/static/uploads/{m.gambar}" style="width:80px; height:80px; object-fit:cover; border-radius:6px;">'
        ) if m.gambar else "-"
    }

    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }

class OTPAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "user", "kode", "expired_at", "is_used", "created_at"]
    form_columns           = ["user", "kode", "expired_at", "is_used"]
    column_searchable_list = ["id_user"]
    column_labels          = {"user": "User"}
    can_create             = False 
    can_edit               = False

class RolesAdmin(SecureModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "role", "created_at"]
    form_columns           = ["role"]
    column_searchable_list = ["role"]
   


admin = Admin(app, name="Maize Leaf", index_view=MyAdminIndexView())
admin.add_view(UserAdmin(User, db.session))
admin.add_view(DeskripsiPenyakitAdmin(DeskripsiPenyakit, db.session))
admin.add_view(HistoryAdmin(History, db.session))
admin.add_view(PostingAdmin(Posting, db.session))
admin.add_view(KomentarAdmin(Komentar, db.session))
admin.add_view(ArtikelAdmin(Artikel, db.session))
admin.add_view(OTPAdmin(OTP, db.session))
admin.add_view(RolesAdmin(Roles, db.session))
admin.add_link(MenuLink(name="Logout", url="/logout"))