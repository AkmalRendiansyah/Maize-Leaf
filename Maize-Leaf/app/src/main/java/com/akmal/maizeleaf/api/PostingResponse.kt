package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class PostingResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("posting")
	val posting: Posting? = null
)

data class Posting(

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id_user")
	val idUser: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null
)
