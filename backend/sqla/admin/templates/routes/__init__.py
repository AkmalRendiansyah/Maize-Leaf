from .auth import auth_bp
from .history import history_bp
from .posting import posting_bp
from .penyakit import penyakit_bp
from .artikel import artikel_bp

def register_routes(app):
    app.register_blueprint(auth_bp)
    app.register_blueprint(history_bp)
    app.register_blueprint(posting_bp)
    app.register_blueprint(penyakit_bp)
    app.register_blueprint(artikel_bp)