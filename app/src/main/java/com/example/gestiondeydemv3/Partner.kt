package com.example.gestiondeydemv3

data class Partner(
    val id: Int,
    val nom: String,
    val email: String,
    val telephone: String,
    val adresse: String,
    val commissionPercent: Int,
    val solde: Int,
    val createdAt: String
)