package com.example.pwc.model

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("cca3")
    val name: String, 
    @SerializedName("borders")
    val neighbours: List<String>
) 