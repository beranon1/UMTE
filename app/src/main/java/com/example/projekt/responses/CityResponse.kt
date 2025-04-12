package com.example.projekt.responses

import com.google.gson.annotations.SerializedName

data class CityResponse(
    @SerializedName("Key") val key: String,
    @SerializedName("LocalizedName") val localizedName: String
)
        