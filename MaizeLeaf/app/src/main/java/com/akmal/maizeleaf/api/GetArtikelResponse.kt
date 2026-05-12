package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class GetArtikelResponse(

	@field:SerializedName("GetArtikelResponse")
	val getArtikelResponse: List<GetArtikelResponseItem?>? = null
)

data class GetArtikelResponseItem(

	@field:SerializedName("referensi")
	val referensi: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("judul")
	val judul: String? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null
)
