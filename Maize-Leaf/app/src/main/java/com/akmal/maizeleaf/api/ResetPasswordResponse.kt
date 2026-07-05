package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
