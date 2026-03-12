package com.example.gestiondeydemv3

data class PartnerTransaction(
    val id: Int,
    val type: String,
    val amount: Int,
    val oldSolde: Int,
    val newSolde: Int,
    val createdAt: String
)