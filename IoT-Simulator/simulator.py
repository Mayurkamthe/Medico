import requests
import random
import time

# ==============================
# CONFIGURATION
# ==============================

# Replace with your actual backend URL (localhost or ngrok)
# BACKEND_BASE_URL = "http://localhost:8080/api" 
BACKEND_BASE_URL = "https://f26f9576662d.ngrok-free.app/api"

API_KEY = "MEDICO_IOT_KEY_2026"
DEVICE_ID = "RASPBERRY_PI_001"

SEND_INTERVAL_SECONDS = 5  # seconds between sends

# Derived URLs
VITALS_URL = f"{BACKEND_BASE_URL}/iot/vitals"
STATUS_URL = f"{BACKEND_BASE_URL}/iot/device-status/{DEVICE_ID}"

# ==============================
# HELPER FUNCTIONS
# ==============================

def check_device_status():
    """
    Checks if the device is currently assigned to an active patient.
    Returns True if active, False otherwise.
    """
    try:
        headers = {
            "Content-Type": "application/json",
            "X-API-Key": API_KEY
        }
        response = requests.get(STATUS_URL, headers=headers, timeout=5)
        
        if response.status_code == 200:
            result = response.json()
            # Expected format: { "success": true, "data": true }
            is_active = result.get("data", False)
            return is_active
        else:
            print(f"⚠️ Status check failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"⚠️ Status check error: {e}")
        return False

def send_vitals_to_backend(temperature, heart_rate, spo2):
    payload = {
        "deviceId": DEVICE_ID,
        "temperature": temperature,
        "heartRate": heart_rate,
        "spo2": spo2
    }

    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY
    }

    try:
        response = requests.post(
            VITALS_URL,
            json=payload,
            headers=headers,
            timeout=5
        )

        if response.status_code == 200:
            data = response.json()
            print("✅ Sent successfully")
            print(f"   Temp: {temperature}°C | HR: {heart_rate} BPM | SpO₂: {spo2}%")
            risk = data.get('data', {}).get('riskLevel', 'N/A')
            print(f"   Risk Level: {risk}")
        else:
            print("❌ Backend error")
            print("Status:", response.status_code)
            print("Response:", response.text)

    except requests.exceptions.Timeout:
        print("❌ Timeout: Backend not responding")
    except requests.exceptions.ConnectionError:
        print("❌ Connection error: Check server / network")
    except Exception as e:
        print("❌ Unexpected error:", e)

# ==============================
# DUMMY DATA GENERATORS
# ==============================

def generate_random_vitals():
    temperature = round(random.uniform(36.0, 41.5), 1)
    heart_rate = random.randint(60, 180)
    spo2 = random.randint(85, 100)
    return temperature, heart_rate, spo2

# ==============================
# MAIN LOOP
# ==============================

def main():
    print("🚀 Dummy IoT Vitals Sender Started")
    print("📡 Backend URL:", BACKEND_BASE_URL)
    print("🆔 Device ID:", DEVICE_ID)
    print("-" * 50)

    while True:
        # 1. Check if we should send data
        is_active = check_device_status()

        if is_active:
             # 2. Generate and Send
            temperature, heart_rate, spo2 = generate_random_vitals()
            send_vitals_to_backend(temperature, heart_rate, spo2)
            print(f"⏳ Waiting {SEND_INTERVAL_SECONDS}s...")
        else:
            print("💤 Device Standby - Waiting for patient assignment...")
        
        time.sleep(SEND_INTERVAL_SECONDS)

# ==============================
# ENTRY POINT
# ==============================

if __name__ == "__main__":
    main()
