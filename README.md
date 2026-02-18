# Medico - Doctor Patient Monitoring System

A hospital-grade, production-ready IoT-based patient monitoring system with AI clinical decision support.

## Project Structure

```
e:\IOT Based doctor app\
├── Medico/                      # Spring Boot Backend
│   └── src/main/java/com/medico/
│       ├── config/              # Security configuration
│       ├── controller/          # REST API endpoints
│       ├── dto/                 # Data transfer objects
│       ├── entity/              # JPA entities
│       ├── exception/           # Global exception handling
│       ├── repository/          # Data access layer
│       ├── security/            # JWT authentication
│       └── service/             # Business logic
│
├── MedicoDoctorApp/             # Expo React Native App
│   └── src/
│       ├── api/                 # API client & services
│       ├── components/          # Reusable UI components
│       ├── contexts/            # React context (Auth)
│       ├── navigation/          # App navigation
│       ├── screens/             # App screens
│       └── types/               # TypeScript types
│
└── IoT-Simulator/               # IoT Device Simulator
    └── simulator.js
```

## Getting Started

### 1. Backend Setup

```bash
cd Medico

# Configure database (MySQL required)
# Edit src/main/resources/application.properties

# Run the backend
./mvnw spring-boot:run
```

### 2. Mobile App Setup

```bash
cd MedicoDoctorApp

# Install dependencies
npm install

# Update API URL in src/api/client.ts

# Start the app
npx expo start
```

### 3. IoT Simulator (Node.js)

```bash
cd IoT-Simulator

# Run with normal vitals
node simulator.js

# Run with critical vitals (triggers AI analysis)
node simulator.js --critical

# Run with moderate vitals
node simulator.js --moderate

# Single reading
node simulator.js --once
```

### 4. IoT Client (Python/Raspberry Pi)

```bash
cd IoT-Simulator

# Install dependencies
pip install requests

# Update backend URL in raspberry_pi_client.py
# Then run:
python raspberry_pi_client.py
```

**Important**: Before sending vitals, create a patient in the Doctor App with Device ID matching your Python script (default: `RASPBERRY_PI_001`).

See [PYTHON_CLIENT_SETUP.md](IoT-Simulator/PYTHON_CLIENT_SETUP.md) for detailed setup instructions.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register doctor |
| POST | `/api/auth/login` | Login |
| GET | `/api/patients` | List patients |
| POST | `/api/patients` | Add patient |
| POST | `/api/iot/vitals` | Receive IoT data |
| GET | `/api/alerts` | Get alerts |
| GET | `/api/patients/{id}/ai-analysis` | Get AI insights |

## Features

- ✅ JWT-based doctor authentication
- ✅ Patient management with IoT device assignment
- ✅ Real-time vital data ingestion
- ✅ Rule-based risk prediction (NORMAL/MODERATE/CRITICAL)
- ✅ Gemini AI clinical decision support
- ✅ Push notifications for critical alerts
- ✅ Legal disclaimers on all AI outputs

## Configuration

### Gemini API Key
Edit `Medico/src/main/resources/application.properties`:
```properties
app.gemini.api-key=YOUR_GEMINI_API_KEY_HERE
```

### Database
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medico_db
spring.datasource.username=root
spring.datasource.password=root
```

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security, JPA, MySQL
- **Mobile**: Expo (React Native), TypeScript
- **AI**: Google Gemini API
- **IoT**: REST API for device integration
