from flask_mail import Mail, Message
from flask import current_app
from dotenv import load_dotenv
import os

load_dotenv()


mail = Mail()



def init_mail(app):
    app.config["MAIL_SERVER"]         = os.getenv('MAIL_SERVER', 'smtp.gmail.com')
    app.config["MAIL_PORT"]           = os.getenv('MAIL_PORT', 587)
    app.config["MAIL_USE_TLS"]        = os.getenv('MAIL_USE_TLS', 'True') == 'True'
    app.config["MAIL_USE_SSL"]        = os.getenv('MAIL_USE_SSL', 'False') == 'True'
    app.config["MAIL_USERNAME"]       = os.getenv('MAIL_USERNAME')
    app.config["MAIL_PASSWORD"]       = os.getenv('MAIL_PASSWORD')
    app.config["MAIL_DEFAULT_SENDER"] = (os.getenv('MAIL_DEFAULT_SENDER'))
    mail.init_app(app)


def send_welcome_email(username: str, email: str):
    """Kirim email selamat datang setelah registrasi"""
    try:
        msg = Message(
            subject="Selamat Datang di Maize Leaf App! ",
            recipients=[email]
        )
        msg.html = f"""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px;
                    border: 1px solid #e0e0e0; border-radius: 8px;">
            <h2 style="color: #2e7d32;">Selamat Datang, {username}! </h2>
            <p>Akun kamu di <strong>Maize Leaf App</strong> berhasil dibuat.</p>
            <p>Kamu sekarang bisa:</p>
            <ul>
                <li>Mendeteksi penyakit daun jagung</li>
                <li>Melihat riwayat deteksi</li>
                <li>Membaca artikel seputar pertanian</li>
            </ul>
            <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
            <p style="color: #757575; font-size: 12px;">
                Jika kamu tidak mendaftar, abaikan email ini.
            </p>
        </div>
        """
        mail.send(msg)
        return True
    except Exception as e:
        print(f"[EMAIL ERROR] Gagal kirim email ke {email}: {e}")
        return False


def send_otp_email(username: str, email: str, kode_otp: str):
    """Kirim kode OTP verifikasi ke email user"""
    try:
        msg = Message(
            subject="Kode Verifikasi Maize Leaf App",
            recipients=[email]
        )
        msg.html = f"""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px;
                    border: 1px solid #e0e0e0; border-radius: 8px;">
            <h2 style="color: #2e7d32;">Verifikasi Akun Kamu, {username}! </h2>
            <p>Gunakan kode OTP berikut untuk mengaktifkan akunmu:</p>
            <div style="text-align: center; margin: 24px 0;">
                <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px;
                             color: #1b5e20; background: #f1f8e9; padding: 12px 24px;
                             border-radius: 8px; display: inline-block;">
                    {kode_otp}
                </span>
            </div>
            <p style="color: #757575;">Kode ini berlaku selama <strong>5 menit</strong>.</p>
            <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
            <p style="color: #757575; font-size: 12px;">
                Jika kamu tidak mendaftar, abaikan email ini.
            </p>
        </div>
        """
        mail.send(msg)
        return True
    except Exception as e:
        print(f"[EMAIL ERROR] Gagal kirim OTP ke {email}: {e}")
        return False
