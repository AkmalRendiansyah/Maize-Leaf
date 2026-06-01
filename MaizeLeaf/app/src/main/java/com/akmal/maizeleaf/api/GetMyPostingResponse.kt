package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class GetMyPostingResponse(

	@field:SerializedName("GetMyPostingResponse")
	val getMyPostingResponse: List<GetMyPostingResponseItem?>? = null
)

data class GetMyPostingResponseItem(

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("jumlah_komentar")
	val jumlahKomentar: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
