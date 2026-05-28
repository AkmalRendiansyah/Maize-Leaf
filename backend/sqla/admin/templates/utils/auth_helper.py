import jwt
from flask import request, jsonify
from functools import wraps
import os

JWT_SECRET = os.getenv('JWT_SECRET', 'your_jwt_secret_key')


def token_required(f):

    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"msg": "Missing or invalid token"}), 401

        token = auth_header.split(" ")[1]
        try:
            decoded = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
            kwargs["user_id"] = decoded["userId"]
        except jwt.ExpiredSignatureError:
            return jsonify({"msg": "Token expired"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"msg": "Invalid token"}), 401

        return f(*args, **kwargs)
    return decorated