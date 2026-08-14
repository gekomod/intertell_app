package pl.intertell.technik.data

import kotlinx.coroutines.delay

class MockTechnicianRepository : TechnicianRepository {

    private val customers = listOf(
        Customer(
            address = "ul. Wrocławska 88/2", name = "M. Kowalska", contract = "88214/OST", plan = "Światłowód 600",
            ont = "HG8245X6", sn = "77B2-0043", phone = "+48 601 224 118", rx = "−31,8 dBm", olt = "OST-1 / 0/2/5",
            state = CustomerState.AWARIA,
            history = listOf(
                HistoryEntry("Brak sygnału (LOS)", "13.08.2026", "Zgłoszenie z aplikacji klienta, ONT bez rejestracji od 06:40."),
                HistoryEntry("Podwyższenie pakietu do 600", "04.11.2025", "Zmiana profilu zdalnie, wymiana routera na Wi-Fi 6."),
                HistoryEntry("Instalacja", "12.03.2024", "Spaw w przełącznicy klatkowej, RX −19,4 dBm."),
            ),
        ),
        Customer(
            address = "ul. Wrocławska 88/7", name = "R. Sikora", contract = "88301/OST", plan = "Światłowód 300",
            ont = "HG8245Q2", sn = "61C4-9920", phone = "+48 602 118 940", rx = "−20,6 dBm", olt = "OST-1 / 0/2/5",
            state = CustomerState.OK,
            history = listOf(
                HistoryEntry("Reklamacja prędkości", "02.06.2026", "Pomiar 318 Mb/s, zgodnie z pakietem. Zamknięte bez wizyty."),
                HistoryEntry("Instalacja", "18.09.2025", "ONT w przedpokoju, router na parterze."),
            ),
        ),
        Customer(
            address = "ul. Wrocławska 90", name = "Sklep Dominik", contract = "88477/OST", plan = "Światłowód 1000",
            ont = "EG8145X6", sn = "90A1-3312", phone = "+48 62 737 21 08", rx = "−18,2 dBm", olt = "OST-1 / 0/2/6",
            state = CustomerState.OK,
            history = listOf(
                HistoryEntry("Stałe IP", "11.01.2026", "Przypisano 89.64.12.7, konfiguracja NAT dla terminala płatniczego."),
                HistoryEntry("Instalacja biznesowa", "03.03.2025", "Podwójna trasa kabla, UPS dla ONT."),
            ),
        ),
        Customer(
            address = "ul. Wrocławska 92/1", name = "K. Malinowski", contract = "88512/OST", plan = "Światłowód 300",
            ont = "HG8245Q2", sn = "55D9-1187", phone = "+48 605 771 302", rx = "−22,9 dBm", olt = "OST-1 / 0/2/6",
            state = CustomerState.ZAWIESZONA,
            history = listOf(
                HistoryEntry("Zawieszenie usługi", "01.07.2026", "Brak płatności, blokada profilu w LMS. ONT online."),
                HistoryEntry("Instalacja", "22.05.2024", "RX −22,9 dBm, długi odcinek wewnętrzny."),
            ),
        ),
    )

    // index into [customers] each job is tied to
    private val jobCustomerIndex = listOf(1, 0, 2, 3)

    private val jobs = listOf(
        Job("ZL-4821", "08:30", "90 min", "Instalacja", JobStatus.ZAPLANOWANE,
            "Doprowadzenie włókna do lokalu, spaw, montaż ONT i konfiguracja routera. Klient nowy, brak gniazda abonenckiego.", jobCustomerIndex[0]),
        Job("ZL-4822", "11:00", "45 min", "Awaria — brak sygnału", JobStatus.PILNE,
            "Zgłoszenie z aplikacji klienta: ONT bez sygnału od 06:40. Sprawdzić pomiar mocy i przełącznicę w budynku.", jobCustomerIndex[1]),
        Job("ZL-4823", "13:30", "30 min", "Wymiana routera", JobStatus.ZAPLANOWANE,
            "Wymiana routera na Wi-Fi 6 w ramach podwyższenia pakietu. Odbiór starego urządzenia.", jobCustomerIndex[2]),
        Job("ZL-4824", "15:45", "60 min", "Instalacja", JobStatus.ZAPLANOWANE,
            "Instalacja biznesowa ze stałym IP. Wymagane uzgodnienie trasy kabla z administracją budynku.", jobCustomerIndex[3]),
    )

    private val baseTeam = listOf(
        TeamMember("Marek Wilk", "TCH-118", "Ostrów", TechStatus.NA_SLUZBIE),
        TeamMember("Kamil Bąk", "TCH-124", "Gorzyce", TechStatus.NA_SLUZBIE),
        TeamMember("Tomasz Rej", "TCH-131", "Odolanów", TechStatus.WOLNE),
    )
    private val addedTeam = mutableListOf<TeamMember>()

    private val networkClients = listOf(
        NetworkClient("iPhone Marta", "192.168.1.24", "5 GHz · −48 dBm", "866 Mb/s"),
        NetworkClient("TV Salon", "192.168.1.31", "LAN 1", "1 Gb/s"),
        NetworkClient("NVR-Kamery", "192.168.1.40", "LAN 3", "100 Mb/s"),
        NetworkClient("Laptop-Praca", "192.168.1.52", "5 GHz · −61 dBm", "433 Mb/s"),
        NetworkClient("Termostat", "192.168.1.77", "2,4 GHz · −70 dBm", "54 Mb/s"),
    )

    override suspend fun login(technicianId: String, password: String): Boolean {
        // Real accounts are provisioned/blocked by an admin on intertell.pl;
        // there is no such backend yet, so any non-blank credentials pass.
        delay(500)
        return technicianId.isNotBlank() && password.isNotBlank()
    }

    override fun getTechnicianId() = "TCH-118"
    override fun getTodayLabel() = "czwartek, 13.08.2026"
    override fun getJobs() = jobs
    override fun getCustomers() = customers
    override fun getNetworkClients() = networkClients
    override fun getTeam() = addedTeam + baseTeam

    override fun addTeamMember(name: String, email: String, role: String, area: String): TeamMember {
        val member = TeamMember(name, "TCH-142", area, TechStatus.ZAPROSZONY)
        addedTeam.add(0, member)
        return member
    }
}
