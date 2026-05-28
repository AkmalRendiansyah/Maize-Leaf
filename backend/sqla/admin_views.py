from flask import redirect, url_for
from flask_admin import Admin
from flask_admin.base import MenuLink
from flask_admin.contrib.sqla import ModelView
from flask_admin.form import FileUploadField
from werkzeug.security import generate_password_hash

from admin import app, db
from admin.models import User, DeskripsiPenyakit, History, Posting, Komentar, Artikel, OTP
from admin.templates.utils.upload_helper import UPLOAD_FOLDER, ALLOWED_EXTENSIONS

class UserAdmin(ModelView):
    can_set_page_size    = True
    page_size            = 10
    can_view_details     = True
    column_list          = ["id", "username", "email", "password","status" ,"created_at"]
    column_searchable_list = ["username", "email"]
    form_columns         = ["username", "email", "password","status"]
    form_widget_args     = {"password": {"type": "password"}}

    def on_model_change(self, form, model, is_created):
        password = form.password.data
        if not password.startswith("pbkdf2:sha256:"):
            model.password = generate_password_hash(password)
        return super().on_model_change(form, model, is_created)


class DeskripsiPenyakitAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "penyakit", "deskripsi", "created_at"]
    form_columns           = ["penyakit", "deskripsi"]
    column_searchable_list = ["penyakit"]


class HistoryAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "id_user", "id_penyakit", "gambar", "created_at"]
    form_columns           = ["id_user", "id_penyakit", "gambar"]
    column_searchable_list = ["id_penyakit", "id_user"]
    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }


class PostingAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "id_user", "deskripsi", "gambar", "created_at"]
    form_columns           = ["id_user", "deskripsi", "gambar"]
    column_searchable_list = ["id_user"]
    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }


class KomentarAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "id_user", "id_posting", "komentar", "created_at"]
    form_columns           = ["id_user", "id_posting", "komentar"]
    column_searchable_list = ["id_user", "id_posting"]


class ArtikelAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "judul", "deskripsi", "referensi", "gambar", "created_at"]
    form_columns           = ["judul", "deskripsi", "referensi", "gambar"]
    column_searchable_list = ["judul", "referensi"]
    form_extra_fields      = {
        "gambar": FileUploadField(
            "Gambar",
            base_path=UPLOAD_FOLDER,
            allowed_extensions=ALLOWED_EXTENSIONS
        )
    }

class OTPAdmin(ModelView):
    can_set_page_size      = True
    page_size              = 10
    can_view_details       = True
    column_list            = ["id", "id_user", "kode", "expired_at", "is_used", "created_at"]
    form_columns           = ["id_user", "kode", "expired_at", "is_used"]
    column_searchable_list = ["id_user"]
    can_create             = False 
    can_edit               = False


admin = Admin(app, name="Maize Leaf")
admin.add_view(UserAdmin(User, db.session))
admin.add_view(DeskripsiPenyakitAdmin(DeskripsiPenyakit, db.session))
admin.add_view(HistoryAdmin(History, db.session))
admin.add_view(PostingAdmin(Posting, db.session))
admin.add_view(KomentarAdmin(Komentar, db.session))
admin.add_view(ArtikelAdmin(Artikel, db.session))
admin.add_view(OTPAdmin(OTP, db.session))
admin.add_link(MenuLink(name="Back Home", url="/"))