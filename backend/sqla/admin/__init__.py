from flask import Flask, request, session
from flask_babel import Babel
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config.from_pyfile("config.py")
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/maizeleaf'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy()      
db.init_app(app)       
def get_locale():
    override = request.args.get("lang")
    if override:
        session["lang"] = override
    return session.get("lang", "en")

babel = Babel(app, locale_selector=get_locale)

import admin.main  # noqa: F401, E402