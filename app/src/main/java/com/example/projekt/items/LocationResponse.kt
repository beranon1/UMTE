package com.example.projekt.items

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("Key") val key: String,
    @SerializedName("LocalizedName") val name: String,
    @SerializedName("Country") val country: CountryResponse
)

data class CountryResponse(
    @SerializedName("LocalizedName") val name: String
)
