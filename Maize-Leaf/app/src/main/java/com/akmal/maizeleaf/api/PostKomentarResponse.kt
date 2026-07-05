package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class PostKomentarResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("komentar")
	val komentar: Komentar? = null
)

data class Komentar(

	@field:SerializedName("id_posting")
	val idPosting: Int? = null,

	@field:SerializedName("komentar")
	val komentar: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id_user")
	val idUser: Int? = null
)
