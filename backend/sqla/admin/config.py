# Create dummy secret key so we can use sessions
SECRET_KEY = "123456790"

# MySQL database configuration for Laragon
DATABASE_FILE = None  # Not used for MySQL
SQLALCHEMY_DATABASE_URI = "mysql+pymysql://root:@localhost/maizeleaf"
SQLALCHEMY_ECHO = True
SQLALCHEMY_TRACK_MODIFICATIONS = False