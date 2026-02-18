# Medico ESP32 IoT Device

ESP32-based vital signs monitor using MLX90614 (temperature) and MAX30100 (heart rate + SpO2) sensors.

## Hardware Requirements

| Component | Description |
|-----------|-------------|
| ESP32 DevKit | ESP32-WROOM-32 or similar |
| MLX90614 | Infrared Temperature Sensor |
| MAX30100 | Pulse Oximeter and Heart Rate Sensor |
| 4.7kΩ Resistors (2x) | I2C Pull-up resistors (optional) |

## Wiring Diagram

```
ESP32           MLX90614        MAX30100
─────           ────────        ────────
3.3V    ───────────────────────► VIN/VCC
GND     ───────────────────────► GND
GPIO21 (SDA) ─────────────────► SDA
GPIO22 (SCL) ─────────────────► SCL
```

**Note:** Both sensors share the same I2C bus (GPIO21/GPIO22).

## Required Libraries

Install these libraries via Arduino IDE Library Manager:

1. **Adafruit MLX90614 Library**
   - Search: "Adafruit MLX90614"
   - Author: Adafruit

2. **MAX30100lib**
   - Search: "MAX30100lib"
   - Author: OXullo Intersecans

3. **ArduinoJson**
   - Search: "ArduinoJson"
   - Author: Benoit Blanchon

## Configuration

Edit these values in `ESP32_Medico_Device.ino`:

```cpp
// WiFi Credentials
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// Backend Server URL
const char* SERVER_URL = "http://YOUR_SERVER_IP:8080/api/iot/vitals";

// Device ID (must match the one assigned in the mobile app)
const char* DEVICE_ID = "ESP32-001";
```

## Device Setup in Mobile App

1. Open Medico Doctor App
2. Go to Patient Details
3. Tap "Connect Device"
4. Enter the **Device ID** (e.g., `ESP32-001`)
5. Device will now send vitals for this patient

## API Endpoint

The ESP32 sends data to:
```
POST /api/iot/vitals
Content-Type: application/json

{
    "deviceId": "ESP32-001",
    "heartRate": 75,
    "spo2": 98,
    "temperature": 36.5
}
```

## LED Status Indicators (Optional)

You can add LED indicators by modifying the code:

| LED Color | Status |
|-----------|--------|
| Green Blink | WiFi Connected, Normal |
| Yellow Blink | Moderate Risk |
| Red Blink | Critical Risk |
| Blue Steady | Sending Data |

## Troubleshooting

### "MLX90614 not found"
- Check I2C connections
- Verify 3.3V power supply
- Try adding 4.7kΩ pull-up resistors on SDA/SCL

### "MAX30100 Failed"
- Check wiring connections
- Ensure proper 3.3V power
- Some modules need 5V on VIN with 3.3V on I2C

### "WiFi Connection Failed"
- Verify SSID and password
- Ensure 2.4GHz network (ESP32 doesn't support 5GHz)
- Check router is within range

### "HTTP Error"
- Verify server URL and port
- Ensure ESP32 and server are on same network
- Check if backend is running

## Serial Monitor Output

```
=================================
Medico IoT Device Starting...
=================================

Connecting to WiFi: MyNetwork
.....
✓ WiFi Connected!
  IP Address: 192.168.1.100

Initializing Sensors...
✓ MLX90614 Temperature Sensor Ready
Initializing MAX30100... ✓ Ready

✓ Device Ready!
=================================

--- Current Readings ---
🌡️ Temperature: 36.5 °C
❤️ Heart Rate:  72 BPM
💨 SpO2:        98 %
------------------------

📤 Sending Vitals to Server...
   Payload: {"deviceId":"ESP32-001","heartRate":72,"spo2":98,"temperature":36.5}
✓ Server Response: 200
  Success: Yes
  Risk Level: NORMAL
  ✓ NORMAL - All vitals OK
```

## Power Consumption

- Active Mode: ~80mA
- Consider deep sleep mode for battery-powered applications

## License

Part of the Medico Doctor App project.
