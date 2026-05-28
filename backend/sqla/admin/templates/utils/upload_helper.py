import os
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'static', 'uploads')
ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

# Buat folder jika belum ada
os.makedirs(UPLOAD_FOLDER, exist_ok=True)


def save_file(file) -> str | None:
    """Simpan file upload, kembalikan nama file atau None jika tidak ada file."""
    if not file:
        return None
    filename = secure_filename(file.filename)
    file.save(os.path.join(UPLOAD_FOLDER, filename))
    return filename