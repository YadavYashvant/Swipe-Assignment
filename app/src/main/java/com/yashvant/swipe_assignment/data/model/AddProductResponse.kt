package com.yashvant.swipe_assignment.data.model

import com.google.gson.annotations.SerializedName


data class AddProductResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("product_id")
    val productId: Int?,

    @SerializedName("product_details")
    val productDetails: Product?
)

