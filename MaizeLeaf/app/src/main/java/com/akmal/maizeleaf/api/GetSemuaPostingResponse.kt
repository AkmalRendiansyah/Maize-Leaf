package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class GetSemuaPostingResponse(

	@field:SerializedName("data")
	val data: List<DataItem?>? = null,

	@field:SerializedName("has_next")
	val hasNext: Boolean? = null,

	@field:SerializedName("page")
	val page: Int? = null,

	@field:SerializedName("total_pages")
	val totalPages: Int? = null,

	@field:SerializedName("total_items")
	val totalItems: Int? = null
)

data class DataItem(

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
