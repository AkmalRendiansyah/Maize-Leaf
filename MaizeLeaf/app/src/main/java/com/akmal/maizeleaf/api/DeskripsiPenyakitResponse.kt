package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class DeskripsiPenyakitResponse(

	@field:SerializedName("penyakit")
	val penyakit: String? = null,

	@field:SerializedName("deskripsi")
	val deskripsi: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
