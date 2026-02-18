# Setting Up Python IoT Client for Medico

## Prerequisites

```bash
pip install requests
```

## Configuration Steps

### 1. Update Backend URL

In `raspberry_pi_client.py`, update the URL:

**For local testing:**
```python
BACKEND_URL = "http://localhost:8080/api/iot/vitals"
```

**For ngrok (if using):**
```python
BACKEND_URL = "https://YOUR-NGROK-URL.ngrok-free.app/api/iot/vitals"
```

**For production (your server IP):**
```python
BACKEND_URL = "http://YOUR_SERVER_IP:8080/api/iot/vitals"
```

### 2. Create a Patient with Matching Device ID

In the Medico Doctor App:
1. **Login** as a doctor
2. **Add a new patient**
3. **Set Device ID** to `RASPBERRY_PI_001` (must match the `DEVICE_ID` in your Python script)

### 3. Run the Python Script

```bash
python raspberry_pi_client.py
```

## Key Differences from Your Original Script

| Your Field | Medico API Field | Notes |
|------------|------------------|-------|
| `bodyTemperature` | `temperature` | Changed field name |
| `/iot/vitals` | `/api/iot/vitals` | Added `/api` prefix |
| No header | `X-API-Key` header | Required for authentication |

## Expected Response

**Success (200):**
```json
{
  "success": true,
  "message": "Vital reading received successfully",
  "data": {
    "id": 1,
    "patientId": 1,
    "heartRate": 165,
    "spo2": 87,
    "temperature": 40.2,
    "riskLevel": "CRITICAL",
    "recordedAt": "2026-01-02T17:23:00"
  }
}
```

**Error (400):**
```json
{
  "success": false,
  "message": "No patient found with device ID: RASPBERRY_PI_001"
}
```

## Testing Different Scenarios

```python
# Normal vitals
send_vitals_to_backend(
    temperature=37.1,
    heart_rate=72,
    spo2=98
)
# Risk: NORMAL

# Moderate risk
send_vitals_to_backend(
    temperature=38.8,
    heart_rate=130,
    spo2=92
)
# Risk: MODERATE

# Critical (triggers AI analysis)
send_vitals_to_backend(
    temperature=40.2,
    heart_rate=165,
    spo2=87
)
# Risk: CRITICAL, AI analysis initiated
```

## Continuous Monitoring

To send vitals every 10 seconds:

```python
import time

while True:
    # Read from sensors
    temp = read_temperature_sensor()
    hr = read_heart_rate_sensor()
    spo2 = read_spo2_sensor()
    
    send_vitals_to_backend(temp, hr, spo2)
    time.sleep(10)
```
