# CompoundCalc

A full-featured **Compound Interest Calculator** built with Spring Boot, Thymeleaf, and Chart.js. It provides both a web UI and a REST API for running simple and complex financial simulations, including Monte Carlo analysis.

## Features

- **Simple simulation** — classic compound interest formula with optional periodic contributions.
- **Complex simulation** — adds monthly contributions with annual growth, inflation adjustment, tax impact, and Monte Carlo risk analysis.
- **Interactive charts** — growth lines, doughnut composition, stacked bars, and inflation-adjusted comparisons powered by Chart.js.
- **Multi-format export** — download results as CSV, Excel (XLSX), PDF, or JSON via the REST API.
- **Internationalization** — Spanish (default) and English; switch with `?lang=es` or `?lang=en`.
- **Dark mode** — toggle between light and dark themes (persisted in `localStorage`).
- **Swagger UI** — interactive API documentation at `/swagger-ui.html`.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 22, Spring Boot 3.4.3 |
| Template engine | Thymeleaf + Layout Dialect |
| Frontend | Bootstrap 5.3, Chart.js 4, Font Awesome 6 |
| Caching | Caffeine |
| PDF export | iText 5 |
| Excel export | Apache POI |
| CSV export | Apache Commons CSV |
| API docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |

## Prerequisites

- **Java 22** (or later)
- **Maven 3.9+** (or use the wrapper if present)

## Getting Started

```bash
# Clone the repository
git clone https://github.com/<your-username>/CompoundCalc.git
cd CompoundCalc

# Build and run
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/simple` | Run a simple simulation |
| `POST` | `/api/v1/complex` | Run a complex simulation |
| `POST` | `/api/v1/export/csv/simple` | Export simple results to CSV |
| `POST` | `/api/v1/export/excel/simple` | Export simple results to XLSX |
| `POST` | `/api/v1/export/pdf/simple` | Export simple results to PDF |
| `POST` | `/api/v1/export/json/simple` | Export simple results to JSON |
| `POST` | `/api/v1/export/csv/complex` | Export complex results to CSV |
| `POST` | `/api/v1/export/excel/complex` | Export complex results to XLSX |
| `POST` | `/api/v1/export/pdf/complex` | Export complex results to PDF |

Full interactive documentation is available at `/swagger-ui.html` when the application is running.

## Docker

```bash
docker build -t compoundcalc .
docker run -p 8080:8080 compoundcalc
```

The included `Dockerfile` uses a multi-stage build (JDK for compilation, JRE for runtime) and runs as a non-root user.

## Project Structure

```
src/main/java/com/calculator/interest/
├── CompoundInterestApplication.java   # Entry point
├── config/                            # WebMvc, cache, locale config
├── controller/                        # Web + REST + export controllers
├── dto/                               # Request/response DTOs
├── exception/                         # Global exception handler
├── model/                             # YearlyBreakdown model
└── service/                           # Calculation & export services

src/main/resources/
├── application.properties
├── messages.properties                # Spanish i18n
├── messages_en.properties             # English i18n
├── static/                            # CSS, JS, favicon
└── templates/                         # Thymeleaf templates
```

## License

This project is provided as-is for educational and demonstration purposes.
