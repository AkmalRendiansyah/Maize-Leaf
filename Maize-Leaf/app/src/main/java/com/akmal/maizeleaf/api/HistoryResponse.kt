package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("history")
	val history: History? = null
)

data class History(

	@field:SerializedName("penyakit")
	val penyakit: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id_user")
	val idUser: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null
)
