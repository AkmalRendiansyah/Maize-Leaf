BULAN = {
    "January": "Januari", "February": "Februari", "March": "Maret",
    "April": "April", "May": "Mei", "June": "Juni",
    "July": "Juli", "August": "Agustus", "September": "September",
    "October": "Oktober", "November": "November", "December": "Desember"
}


def format_tanggal(dt) -> str:
    """Mengubah datetime ke format: '01 Januari 2025, 08:00'"""
    bulan = BULAN[dt.strftime("%B")]
    return f"{dt.strftime('%d')} {bulan} {dt.strftime('%Y')}, {dt.strftime('%H:%M')}"