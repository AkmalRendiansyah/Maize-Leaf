package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("otp")
	val otp: Int? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("token")
	val token: String? = null
)
