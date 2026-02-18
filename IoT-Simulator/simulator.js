/**
 * Medico IoT Device Simulator
 * 
 * This script simulates an ESP32 device sending patient vitals to the backend.
 * Usage: node simulator.js
 * 
 * Requirements: Node.js installed
 */

const http = require('http');

// Configuration
const CONFIG = {
    backendUrl: 'http://localhost:8080',
    deviceId: 'ESP32_001',
    intervalMs: 10000, // Send data every 10 seconds
};

// Generate random vitals
function generateVitals(scenario = 'normal') {
    let heartRate, spo2, temperature;

    switch (scenario) {
        case 'critical':
            heartRate = Math.floor(Math.random() * 30) + 155; // 155-185
            spo2 = Math.floor(Math.random() * 5) + 85; // 85-89
            temperature = (Math.random() * 1.5 + 40).toFixed(1); // 40.0-41.5
            break;
        case 'moderate':
            heartRate = Math.floor(Math.random() * 30) + 125; // 125-155
            spo2 = Math.floor(Math.random() * 4) + 90; // 90-93
            temperature = (Math.random() * 1 + 38.5).toFixed(1); // 38.5-39.5
            break;
        case 'normal':
        default:
            heartRate = Math.floor(Math.random() * 30) + 60; // 60-90
            spo2 = Math.floor(Math.random() * 4) + 96; // 96-99
            temperature = (Math.random() * 0.8 + 36.2).toFixed(1); // 36.2-37.0
            break;
    }

    return {
        deviceId: CONFIG.deviceId,
        heartRate: heartRate,
        spo2: spo2,
        temperature: parseFloat(temperature)
    };
}

// Send vitals to backend
function sendVitals(vitals) {
    const data = JSON.stringify(vitals);

    const url = new URL(CONFIG.backendUrl + '/api/iot/vitals');

    const options = {
        hostname: url.hostname,
        port: url.port || 8080,
        path: url.pathname,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(data),
            'X-API-Key': 'MEDICO_IOT_KEY_2026'
        }
    };

    const req = http.request(options, (res) => {
        let responseData = '';
        res.on('data', (chunk) => {
            responseData += chunk;
        });
        res.on('end', () => {
            const timestamp = new Date().toISOString();
            if (res.statusCode === 200) {
                console.log(`[${timestamp}] ✅ Vitals sent successfully`);
                console.log(`   HR: ${vitals.heartRate} BPM | SpO2: ${vitals.spo2}% | Temp: ${vitals.temperature}°C`);
                try {
                    const response = JSON.parse(responseData);
                    console.log(`   Risk Level: ${response.data?.riskLevel || 'Unknown'}`);
                } catch (e) { }
            } else {
                console.log(`[${timestamp}] ❌ Error: ${res.statusCode}`);
                console.log(`   Response: ${responseData}`);
            }
        });
    });

    req.on('error', (error) => {
        console.log(`[${new Date().toISOString()}] ❌ Connection error: ${error.message}`);
    });

    req.write(data);
    req.end();
}

// Parse command line arguments
const args = process.argv.slice(2);
let scenario = 'normal';
let continuous = true;

args.forEach(arg => {
    if (arg === '--critical') scenario = 'critical';
    if (arg === '--moderate') scenario = 'moderate';
    if (arg === '--normal') scenario = 'normal';
    if (arg === '--once') continuous = false;
    if (arg.startsWith('--device=')) CONFIG.deviceId = arg.split('=')[1];
    if (arg.startsWith('--url=')) CONFIG.backendUrl = arg.split('=')[1];
});

// Main
console.log('========================================');
console.log('    Medico IoT Device Simulator');
console.log('========================================');
console.log(`Device ID: ${CONFIG.deviceId}`);
console.log(`Backend URL: ${CONFIG.backendUrl}`);
console.log(`Scenario: ${scenario.toUpperCase()}`);
console.log(`Mode: ${continuous ? 'Continuous' : 'Single'}`);
console.log('----------------------------------------');

if (continuous) {
    console.log(`Sending vitals every ${CONFIG.intervalMs / 1000} seconds...`);
    console.log('Press Ctrl+C to stop\n');

    // Send immediately, then at intervals
    sendVitals(generateVitals(scenario));
    setInterval(() => {
        sendVitals(generateVitals(scenario));
    }, CONFIG.intervalMs);
} else {
    sendVitals(generateVitals(scenario));
}
