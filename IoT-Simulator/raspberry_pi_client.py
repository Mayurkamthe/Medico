import requests

# Configuration
BACKEND_URL = "http://localhost:8080/api/iot/vitals"  # Change to your ngrok URL or server IP
API_KEY = "MEDICO_IOT_KEY_2026"
DEVICE_ID = "RASPBERRY_PI_001"

def send_vitals_to_backend(temperature, heart_rate, spo2):
    """
    Send patient vitals to Medico backend
    
    Args:
        temperature: Body temperature in Celsius
        heart_rate: Heart rate in BPM
        spo2: Blood oxygen saturation percentage
    """
    payload = {
        "deviceId": DEVICE_ID,
        "temperature": temperature,  # Note: 'temperature' not 'bodyTemperature'
        "heartRate": heart_rate,
        "spo2": spo2
    }
    
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY  # Required for authentication
    }

    try:
        response = requests.post(
            BACKEND_URL,
            json=payload,
            headers=headers,
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Vitals sent successfully")
            print(f"   Risk Level: {data.get('data', {}).get('riskLevel', 'N/A')}")
            print(f"   Patient: {data.get('data', {}).get('patientId', 'N/A')}")
        else:
            print(f"❌ Error: {response.status_code}")
            print(f"   Message: {response.json().get('message', 'Unknown error')}")
            
    except requests.exceptions.Timeout:
        print("❌ Request timed out - check if backend is running")
    except requests.exceptions.ConnectionError:
        print("❌ Connection failed - check backend URL and network")
    except Exception as e:
        print(f"❌ Failed to send data: {e}")


# Example usage
if __name__ == "__main__":
    # Critical vitals (will trigger AI analysis)
    send_vitals_to_backend(
        temperature=40.2,
        heart_rate=165,
        spo2=87
    )
    
    # Normal vitals
    # send_vitals_to_backend(
    #     temperature=37.1,
    #     heart_rate=72,
    #     spo2=98
    # )
