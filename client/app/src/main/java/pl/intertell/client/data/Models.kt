package pl.intertell.client.data

data class Account(
    val fullName: String,
    val initials: String,
    val email: String,
    val contractNumber: String,
    val city: String,
)

/** connectionUp/routerUptimeDays are null when unknown — not LMS-linked, LMS unreachable, or no router record yet. */
data class ServiceStatus(
    val planName: String,
    val speedLabel: String,
    val connectionUp: Boolean?,
    val routerUptimeDays: Int?,
)

data class Invoice(
    val id: Long,
    val number: String,
    val issuedOn: String,
    val dueOn: String,
    val amountZl: String,
    val status: String,
    val statusLabel: String,
) {
    val paid: Boolean get() = status == "paid"
}

enum class PlanDirection { LOWER, CURRENT, HIGHER }

data class Plan(
    val id: Long,
    val name: String,
    val speedLabel: String,
    val priceLabel: String,
    val priceCents: Long,
    val direction: PlanDirection,
    val badge: String,
    val deltaLabel: String,
) {
    val requiresRequest: Boolean get() = direction != PlanDirection.HIGHER
}

data class DmzSettings(
    val enabled: Boolean,
    val hostIp: String,
    val publicIp: String,
)

/** One turn of the "Czat z BOK" AI assistant conversation — role is "user" or "assistant". */
data class ChatMessage(
    val role: String,
    val content: String,
)

/**
 * Outcome of a plan-change request. [limited] is true when the monthly
 * self-service limit (once/30 days) has already been used — [applied] is
 * then always false and [message] explains the request was filed for BOK
 * to verify instead. For a normal downgrade (not limited), [message] is
 * null and the screen falls back to its own copy.
 */
data class PlanChangeResult(
    val applied: Boolean,
    val limited: Boolean,
    val message: String?,
)
