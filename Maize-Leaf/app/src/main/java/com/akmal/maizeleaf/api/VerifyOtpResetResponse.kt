package com.akmal.maizeleaf.api

import com.google.gson.annotations.SerializedName

data class VerifyOtpResetResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("resetToken")
	val resetToken: String? = null
)
