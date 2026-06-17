import os
import uuid
from io import BytesIO
from PIL import Image
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), 'static', 'uploads')
ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

MAX_SIZE = (800, 800)

JPEG_QUALITY = 85

os.makedirs(UPLOAD_FOLDER, exist_ok=True)


def save_file(file) -> str | None:
    if not file:
        return None

    ext = os.path.splitext(secure_filename(file.filename))[1].lower()
    if ext not in [".jpg", ".jpeg", ".png"]:
        return None

    unique_name = f"{uuid.uuid4().hex}.jpg"
    save_path   = os.path.join(UPLOAD_FOLDER, unique_name)

    # Baca gambar dengan Pillow
    img = Image.open(BytesIO(file.read()))

    # Konversi ke RGB supaya bisa disimpan sebagai JPEG (RGBA / P tidak bisa)
    if img.mode in ("RGBA", "P", "LA"):
        img = img.convert("RGB")

    # Resize dengan mempertahankan rasio aspek (tidak melebihi MAX_SIZE)
    img.thumbnail(MAX_SIZE, Image.LANCZOS)

    # Simpan sebagai JPEG
    img.save(save_path, format="JPEG", quality=JPEG_QUALITY, optimize=True)

    return unique_name