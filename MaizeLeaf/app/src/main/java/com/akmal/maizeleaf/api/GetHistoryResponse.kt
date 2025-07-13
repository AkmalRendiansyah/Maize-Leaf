package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class GetHistoryResponse(

	@field:SerializedName("GetHistoryResponse")
	val getHistoryResponse: List<GetHistoryResponseItem?>? = null
)

data class GetHistoryResponseItem(

	@field:SerializedName("penyakit")
	val penyakit: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null
)
