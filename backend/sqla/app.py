import os
import os.path as op

from admin import app
from admin.data import build_sample_db
from jinja2 import StrictUndefined

# # Untuk MySQL, tidak perlu membuat file database SQLite
# # Jika ingin mengisi sample data, cukup panggil build_sample_db di dalam app context
# with app.app_context():
#     build_sample_db()

if __name__ == "__main__":
    # Start app
    app.jinja_env.undefined = StrictUndefined
    app.run(host='0.0.0.0', port=5000, debug=True)