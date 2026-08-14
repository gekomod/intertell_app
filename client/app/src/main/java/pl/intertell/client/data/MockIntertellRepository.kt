package pl.intertell.client.data

import kotlinx.coroutines.delay

class MockIntertellRepository : IntertellRepository {

    override suspend fun login(contractOrEmail: String, password: String): Boolean {
        // Mirrors "LMS handles auth, app stores no passwords": any non-blank
        // credentials are accepted while there is no real LMS to call.
        delay(500)
        return contractOrEmail.isNotBlank() && password.isNotBlank()
    }

    override fun getAccount() = Account(
        fullName = "Marta Kowalska",
        initials = "MK",
        email = "m.kowalska@example.pl",
        contractNumber = "88214/OST",
        city = "Ostrów Wlkp.",
    )

    override fun getServiceStatus() = ServiceStatus(
        planName = "Światłowód 600",
        downloadMbps = 600,
        uploadMbps = 200,
        ontOnline = true,
        routerDaysSinceRestart = 88,
    )

    override fun getInvoices() = listOf(
        Invoice("FV/2026/08/1119", "Sierpień 2026", "89,00 zł", 89.00, "15.08.2026", paid = false),
        Invoice("FV/2026/07/1042", "Lipiec 2026", "89,00 zł", 89.00, "15.07.2026", paid = true),
        Invoice("FV/2026/06/0931", "Czerwiec 2026", "89,00 zł", 89.00, "15.06.2026", paid = true),
        Invoice("FV/2026/05/0847", "Maj 2026", "69,00 zł", 69.00, "15.05.2026", paid = true),
    )

    override fun getContractDocuments() = listOf(
        ContractDocument("Umowa abonencka 88214/OST", "podpisana 12.03.2024 · 4 strony"),
        ContractDocument("Aneks — zmiana pakietu na 600", "podpisany 04.11.2025 · 1 strona"),
        ContractDocument("Regulamin świadczenia usług", "wersja 4.2, obowiązuje od 01.01.2026"),
    )

    override fun getPlans() = listOf(
        Plan("Światłowód 300", "300/100 Mb/s", "69 zł", PlanDirection.LOWER, "NIŻEJ", "−20 zł"),
        Plan("Światłowód 600", "600/200 Mb/s", "89 zł", PlanDirection.CURRENT, "AKTUALNY", "obecny"),
        Plan("Światłowód 1000", "1000/300 Mb/s", "119 zł", PlanDirection.HIGHER, "WYŻEJ", "+30 zł"),
    )

    override fun getContractEndDate() = "12.03.2027"
}
