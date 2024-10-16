package com.senseicoder.quickcart.core.model

data class DraftOrderReqRes(val draft_order: DraftOrder)

data class DraftOrder(
    val email: String,
    val id: Long,
    val line_items: List<LineItem>,
    val customer: Customer,
    val billing_address: Address,
    val shipping_address: Address,
    val applied_discount: Applied_Discount? =null,
    val note_attributes :List<noteAttributesData>
)
data class noteAttributesData(
    val name: String,
    val value: String
)

data class LineItem(
    val title: String,
    val variant_id: Long,
    val quantity: Int,
    val price: String
)

data class Customer(
    val email: String
)

data class Address(
    val address1: String,
    val city: String,
    val province: String,
    val zip: String = "321",
    val country: String
)
data class Applied_Discount(
    val title: String,
    val value: Double,
    val value_type: String,
    var amount:String? =null
)
