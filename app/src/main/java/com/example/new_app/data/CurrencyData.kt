package com.example.new_app.data

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val flagEmoji: String,
    val isFavorite: Boolean = false
)

object WorldCurrencies {
    val FAVORITES = listOf(
        CurrencyItem("USD", "$", "US Dollar", "United States", "🇺🇸", true),
        CurrencyItem("EUR", "€", "Euro", "European Union", "🇪🇺", true),
        CurrencyItem("GBP", "£", "British Pound", "United Kingdom", "🇬🇧", true),
        CurrencyItem("INR", "₹", "Indian Rupee", "India", "🇮🇳", true),
        CurrencyItem("CAD", "C$", "Canadian Dollar", "Canada", "🇨🇦", true),
        CurrencyItem("AUD", "A$", "Australian Dollar", "Australia", "🇦🇺", true)
    )

    val ALL_CURRENCIES = listOf(
        CurrencyItem("USD", "$", "US Dollar", "United States", "🇺🇸", true),
        CurrencyItem("EUR", "€", "Euro", "European Union", "🇪🇺", true),
        CurrencyItem("GBP", "£", "British Pound", "United Kingdom", "🇬🇧", true),
        CurrencyItem("INR", "₹", "Indian Rupee", "India", "🇮🇳", true),
        CurrencyItem("CAD", "C$", "Canadian Dollar", "Canada", "🇨🇦", true),
        CurrencyItem("AUD", "A$", "Australian Dollar", "Australia", "🇦🇺", true),
        CurrencyItem("JPY", "¥", "Japanese Yen", "Japan", "🇯🇵"),
        CurrencyItem("CHF", "CHF", "Swiss Franc", "Switzerland", "🇨🇭"),
        CurrencyItem("CNY", "¥", "Chinese Yuan", "China", "🇨🇳"),
        CurrencyItem("SGD", "S$", "Singapore Dollar", "Singapore", "🇸🇬"),
        CurrencyItem("AED", "AED", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
        CurrencyItem("SAR", "SAR", "Saudi Riyal", "Saudi Arabia", "🇸🇦"),
        CurrencyItem("BRL", "R$", "Brazilian Real", "Brazil", "🇧🇷"),
        CurrencyItem("MXN", "$", "Mexican Peso", "Mexico", "🇲🇽"),
        CurrencyItem("ZAR", "R", "South African Rand", "South Africa", "🇿🇦"),
        CurrencyItem("KES", "KSh", "Kenyan Shilling", "Kenya", "🇰🇪"),
        CurrencyItem("NGN", "₦", "Nigerian Naira", "Nigeria", "🇳🇬"),
        CurrencyItem("PLN", "zł", "Polish Zloty", "Poland", "🇵🇱"),
        CurrencyItem("SEK", "kr", "Swedish Krona", "Sweden", "🇸🇪"),
        CurrencyItem("NOK", "kr", "Norwegian Krone", "Norway", "🇳🇴"),
        CurrencyItem("MYR", "RM", "Malaysian Ringgit", "Malaysia", "🇲🇾"),
        CurrencyItem("IDR", "Rp", "Indonesian Rupiah", "Indonesia", "🇮🇩"),
        CurrencyItem("PHP", "₱", "Philippine Peso", "Philippines", "🇵🇭"),
        CurrencyItem("THB", "฿", "Thai Baht", "Thailand", "🇹🇭"),
        CurrencyItem("VND", "₫", "Vietnamese Dong", "Vietnam", "🇻🇳"),
        CurrencyItem("KRW", "₩", "South Korean Won", "South Korea", "🇰🇷"),
        CurrencyItem("TRY", "₺", "Turkish Lira", "Turkey", "🇹🇷"),
        CurrencyItem("EGP", "E£", "Egyptian Pound", "Egypt", "🇪🇬"),
        CurrencyItem("PKR", "Rs", "Pakistani Rupee", "Pakistan", "🇵🇰"),
        CurrencyItem("BDT", "৳", "Bangladeshi Taka", "Bangladesh", "🇧🇩"),
        CurrencyItem("LKR", "Rs", "Sri Lankan Rupee", "Sri Lanka", "🇱🇰"),
        CurrencyItem("NPR", "Rs", "Nepalese Rupee", "Nepal", "🇳🇵"),
        CurrencyItem("ARS", "$", "Argentine Peso", "Argentina", "🇦🇷"),
        CurrencyItem("CLP", "$", "Chilean Peso", "Chile", "🇨🇱"),
        CurrencyItem("COP", "$", "Colombian Peso", "Colombia", "🇨🇴"),
        CurrencyItem("PEN", "S/", "Peruvian Sol", "Peru", "🇵🇪"),
        CurrencyItem("NZD", "NZ$", "New Zealand Dollar", "New Zealand", "🇳🇿"),
        CurrencyItem("ILS", "₪", "Israeli Shekel", "Israel", "🇮🇱")
    )
}
