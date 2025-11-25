package com.yashvant.swipe_assignment.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("product_type")
    val productType: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("tax")
    val tax: Double,

    @SerializedName("image")
    val image: String? = null,

    val isSynced: Boolean = true,

    val createdAt: Long = System.currentTimeMillis()
)

