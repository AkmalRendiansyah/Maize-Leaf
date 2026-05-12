package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class GetKomentarResponse(

	@field:SerializedName("GetKomentarResponse")
	val getKomentarResponse: List<GetKomentarResponseItem?>? = null
)

data class GetKomentarResponseItem(

	@field:SerializedName("komentar")
	val komentar: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("username")
	val username: String? = null
)
