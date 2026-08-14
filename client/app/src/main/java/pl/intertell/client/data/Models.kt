package pl.intertell.client.data

data class Account(
    val fullName: String,
    val initials: String,
    val email: String,
    val contractNumber: String,
    val city: String,
)

data class ServiceStatus(
    val planName: String,
    val downloadMbps: Int,
    val uploadMbps: Int,
    val ontOnline: Boolean,
    val routerDaysSinceRestart: Int,
)

data class Invoice(
    val id: String,
    val period: String,
    val amountLabel: String,
    val amountGrossZl: Double,
    val dueDate: String,
    val paid: Boolean,
) {
    val statusLabel: String get() = if (paid) "Zapłacona" else "Do zapłaty"
    val routerLeaseZl: Double get() = 6.59
    val subscriptionZl: Double get() = amountGrossZl - routerLeaseZl
}

data class ContractDocument(
    val name: String,
    val meta: String,
)

enum class PlanDirection { LOWER, CURRENT, HIGHER }

data class Plan(
    val name: String,
    val speedLabel: String,
    val priceLabel: String,
    val direction: PlanDirection,
    val badge: String,
    val deltaLabel: String,
) {
    val requiresRequest: Boolean get() = direction != PlanDirection.HIGHER
}
