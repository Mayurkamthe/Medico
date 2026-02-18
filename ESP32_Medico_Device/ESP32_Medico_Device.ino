/*
 * Medico IoT Device - ESP32 Vital Signs Monitor
 * Sensors: MLX90614 (Temperature) + MAX30100 (Heart Rate + SpO2)
 * 
 * Hardware Connections:
 * - MLX90614: SDA -> GPIO21, SCL -> GPIO22 (I2C)
 * - MAX30100: SDA -> GPIO21, SCL -> GPIO22 (I2C - shared bus)
 * 
 * Libraries Required:
 * - Adafruit MLX90614 Library
 * - MAX30100_PulseOximeter Library
 * - ArduinoJson
 * - WiFi (ESP32 built-in)
 * - HTTPClient (ESP32 built-in)
 * 
 * Control Flow:
 * - Device checks with backend if monitoring is active
 * - Only sends vitals when assigned to a patient
 * - Doctor controls start/stop from mobile app
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "MAX30100_PulseOximeter.h"

// ================== CONFIGURATION ==================
// WiFi Credentials
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// Backend Server Configuration
const char* BASE_URL = "http://YOUR_SERVER_IP:8080";
String VITALS_URL;
String STATUS_URL;

// Device Configuration
const char* DEVICE_ID = "ESP32-001";  // Unique device ID - must match the one assigned in app

// Timing Configuration (in milliseconds)
const unsigned long READING_INTERVAL = 10000;    // Send readings every 10 seconds
const unsigned long STATUS_CHECK_INTERVAL = 5000; // Check status every 5 seconds
const unsigned long REPORTING_PERIOD = 1000;      // MAX30100 reporting period

// ================== GLOBAL OBJECTS ==================
Adafruit_MLX90614 mlx = Adafruit_MLX90614();
PulseOximeter pox;

// Variables for vital readings
float temperature = 0.0;
int heartRate = 0;
int spo2 = 0;
bool sensorsReady = false;

// Device status
bool isMonitoringActive = false;
String currentPatientName = "";

// Timing variables
unsigned long lastReadingTime = 0;
unsigned long lastStatusCheckTime = 0;
unsigned long lastReportTime = 0;

// LED pins (optional - for status indication)
const int LED_GREEN = 2;   // Built-in LED on most ESP32 boards

// ================== CALLBACK FUNCTION ==================
void onBeatDetected() {
    Serial.println("♥ Beat detected!");
}

// ================== SETUP ==================
void setup() {
    Serial.begin(115200);
    Serial.println("\n=================================");
    Serial.println("Medico IoT Device Starting...");
    Serial.println("=================================\n");

    // Setup LED
    pinMode(LED_GREEN, OUTPUT);
    digitalWrite(LED_GREEN, LOW);

    // Build URLs
    VITALS_URL = String(BASE_URL) + "/api/iot/vitals";
    STATUS_URL = String(BASE_URL) + "/api/patients/device/" + DEVICE_ID + "/active-patient";

    // Initialize I2C
    Wire.begin(21, 22);  // SDA, SCL for ESP32
    
    // Initialize WiFi
    setupWiFi();
    
    // Initialize Sensors
    setupSensors();
    
    Serial.println("\n✓ Device Ready!");
    Serial.println("Device ID: " + String(DEVICE_ID));
    Serial.println("Waiting for monitoring to be started from app...");
    Serial.println("=================================\n");
}

// ================== WIFI SETUP ==================
void setupWiFi() {
    Serial.print("Connecting to WiFi: ");
    Serial.println(WIFI_SSID);
    
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n✓ WiFi Connected!");
        Serial.print("  IP Address: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println("\n✗ WiFi Connection Failed!");
        Serial.println("  Restarting in 5 seconds...");
        delay(5000);
        ESP.restart();
    }
}

// ================== SENSOR SETUP ==================
void setupSensors() {
    Serial.println("\nInitializing Sensors...");
    
    // Initialize MLX90614
    if (mlx.begin()) {
        Serial.println("✓ MLX90614 Temperature Sensor Ready");
    } else {
        Serial.println("✗ MLX90614 not found!");
    }
    
    // Initialize MAX30100
    Serial.print("Initializing MAX30100...");
    if (pox.begin()) {
        Serial.println(" ✓ Ready");
        pox.setIRLedCurrent(MAX30100_LED_CURR_7_6MA);
        pox.setOnBeatDetectedCallback(onBeatDetected);
        sensorsReady = true;
    } else {
        Serial.println(" ✗ Failed!");
        Serial.println("  Check wiring and try again.");
    }
}

// ================== CHECK DEVICE STATUS ==================
void checkDeviceStatus() {
    if (WiFi.status() != WL_CONNECTED) {
        isMonitoringActive = false;
        return;
    }
    
    HTTPClient http;
    http.begin(STATUS_URL);
    
    int httpResponseCode = http.GET();
    
    if (httpResponseCode == 200) {
        String response = http.getString();
        StaticJsonDocument<512> doc;
        DeserializationError error = deserializeJson(doc, response);
        
        if (!error) {
            bool success = doc["success"] | false;
            
            if (success && !doc["data"].isNull()) {
                // Device is assigned to a patient - monitoring is active
                bool wasActive = isMonitoringActive;
                isMonitoringActive = true;
                currentPatientName = doc["data"]["fullName"].as<String>();
                
                if (!wasActive) {
                    Serial.println("\n🟢 MONITORING STARTED");
                    Serial.println("   Patient: " + currentPatientName);
                    digitalWrite(LED_GREEN, HIGH);
                }
            } else {
                // Device is not assigned - monitoring stopped
                if (isMonitoringActive) {
                    Serial.println("\n🔴 MONITORING STOPPED");
                    Serial.println("   Waiting for assignment...");
                    digitalWrite(LED_GREEN, LOW);
                }
                isMonitoringActive = false;
                currentPatientName = "";
            }
        }
    } else {
        // Server error or not reachable
        if (httpResponseCode == 400 || httpResponseCode == 404) {
            // Device not assigned
            if (isMonitoringActive) {
                Serial.println("\n🔴 MONITORING STOPPED");
                digitalWrite(LED_GREEN, LOW);
            }
            isMonitoringActive = false;
        }
    }
    
    http.end();
}

// ================== READ SENSORS ==================
void readSensors() {
    // Read Temperature from MLX90614
    temperature = mlx.readObjectTempC();
    
    // Validate temperature reading
    if (temperature < 25.0 || temperature > 45.0) {
        delay(100);
        temperature = mlx.readObjectTempC();
    }
    
    // Get Heart Rate and SpO2 from MAX30100
    heartRate = (int)pox.getHeartRate();
    spo2 = (int)pox.getSpO2();
    
    // Ensure valid ranges
    if (heartRate < 40 || heartRate > 200) heartRate = 0;
    if (spo2 < 70 || spo2 > 100) spo2 = 0;
}

// ================== SEND DATA TO BACKEND ==================
void sendVitalsToServer() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("✗ WiFi not connected. Reconnecting...");
        setupWiFi();
        return;
    }
    
    // Skip if readings are not valid
    if (heartRate == 0 || spo2 == 0) {
        Serial.println("⚠ Invalid readings, skipping transmission");
        Serial.printf("  HR: %d, SpO2: %d, Temp: %.1f°C\n", heartRate, spo2, temperature);
        return;
    }
    
    // Create JSON payload
    StaticJsonDocument<256> doc;
    doc["deviceId"] = DEVICE_ID;
    doc["heartRate"] = heartRate;
    doc["spo2"] = spo2;
    doc["temperature"] = round(temperature * 10) / 10.0;
    
    String jsonPayload;
    serializeJson(doc, jsonPayload);
    
    Serial.println("\n📤 Sending Vitals for: " + currentPatientName);
    Serial.println("   Payload: " + jsonPayload);
    
    // Send HTTP POST request
    HTTPClient http;
    http.begin(VITALS_URL);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(jsonPayload);
    
    if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.printf("✓ Server Response: %d\n", httpResponseCode);
        
        if (httpResponseCode == 200) {
            StaticJsonDocument<512> responseDoc;
            DeserializationError error = deserializeJson(responseDoc, response);
            
            if (!error) {
                const char* riskLevel = responseDoc["data"]["riskLevel"] | "UNKNOWN";
                
                Serial.printf("  Risk Level: %s\n", riskLevel);
                
                // Visual indicator
                if (String(riskLevel) == "CRITICAL") {
                    Serial.println("  ⚠️ CRITICAL - Immediate attention required!");
                    // Blink LED rapidly for critical
                    for (int i = 0; i < 5; i++) {
                        digitalWrite(LED_GREEN, LOW);
                        delay(100);
                        digitalWrite(LED_GREEN, HIGH);
                        delay(100);
                    }
                } else if (String(riskLevel) == "MODERATE") {
                    Serial.println("  ⚡ MODERATE - Monitor closely");
                } else {
                    Serial.println("  ✓ NORMAL - All vitals OK");
                }
            }
        }
    } else {
        Serial.printf("✗ HTTP Error: %d\n", httpResponseCode);
        
        // Check if device was unassigned
        if (httpResponseCode == 400) {
            Serial.println("  Device may have been unassigned");
            isMonitoringActive = false;
            digitalWrite(LED_GREEN, LOW);
        }
    }
    
    http.end();
}

// ================== DISPLAY STATUS ==================
void displayStatus() {
    Serial.println("\n--- Device Status ---");
    if (isMonitoringActive) {
        Serial.println("🟢 ACTIVE - Monitoring: " + currentPatientName);
        Serial.printf("🌡️ Temperature: %.1f °C\n", temperature);
        Serial.printf("❤️ Heart Rate:  %d BPM\n", heartRate);
        Serial.printf("💨 SpO2:        %d %%\n", spo2);
    } else {
        Serial.println("🔴 INACTIVE - Waiting for assignment");
        Serial.println("   Open the Medico app and tap 'Start Monitoring'");
    }
    Serial.println("---------------------");
}

// ================== MAIN LOOP ==================
void loop() {
    // IMPORTANT: MAX30100 needs to be called frequently
    if (sensorsReady) {
        pox.update();
    }
    
    unsigned long currentTime = millis();
    
    // Check device status (is monitoring active?)
    if (currentTime - lastStatusCheckTime >= STATUS_CHECK_INTERVAL) {
        lastStatusCheckTime = currentTime;
        checkDeviceStatus();
    }
    
    // Report readings at defined interval
    if (currentTime - lastReportTime >= REPORTING_PERIOD) {
        lastReportTime = currentTime;
        heartRate = (int)pox.getHeartRate();
        spo2 = (int)pox.getSpO2();
    }
    
    // Only process and send if monitoring is active
    if (isMonitoringActive) {
        // Send to server at defined interval
        if (currentTime - lastReadingTime >= READING_INTERVAL) {
            lastReadingTime = currentTime;
            
            // Read temperature
            temperature = mlx.readObjectTempC();
            
            // Display status
            displayStatus();
            
            // Send to backend
            sendVitalsToServer();
        }
        
        // Keep LED on while active
        digitalWrite(LED_GREEN, HIGH);
    } else {
        // Blink LED slowly when waiting
        if ((currentTime / 1000) % 2 == 0) {
            digitalWrite(LED_GREEN, HIGH);
        } else {
            digitalWrite(LED_GREEN, LOW);
        }
    }
}
