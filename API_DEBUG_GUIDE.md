# Medico API Debugging Guide

## Issue: Frontend Not Sending Requests to Backend

### Quick Checks

1. **Is the backend running?**
   ```bash
   # In Medico directory
   ./mvnw spring-boot:run
   ```
   Look for: `Started MedicoApplication on port 8080`

2. **Is ngrok running?**
   ```bash
   ngrok http 8080
   ```
   Copy the HTTPS forwarding URL (e.g., `https://xxxx.ngrok-free.app`)

3. **Update frontend with ngrok URL**
   Edit `MedicoDoctorApp/src/api/client.ts`:
   ```typescript
   const BASE_URL = 'https://YOUR-NGROK-URL.ngrok-free.app/api';
   ```

---

## Debugging Steps

### Step 1: Check Console Logs

In Expo Go app, shake device → "Show Dev Menu" → "Debug Remote JS"

Look for console logs:
- `🚀 API Request: POST /auth/login`
- `✅ API Response: 200 /auth/login`
- `❌ API Error Response: ...`

### Step 2: Test Backend Directly

**Option A: Browser**
Visit: `https://your-ngrok-url.ngrok-free.app/api/auth/login`
- Should show ngrok warning page first
- This is why we added `ngrok-skip-browser-warning` header

**Option B: Postman/CURL**
```bash
curl -X POST "https://your-ngrok-url.ngrok-free.app/api/auth/register" \
  -H "Content-Type: application/json" \
  -H "ngrok-skip-browser-warning: true" \
  -d '{
    "email": "test@doctor.com",
    "password": "test123",
    "fullName": "Test Doctor"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJ...",
    "doctor": { ... }
  }
}
```

### Step 3: Common Issues

| Issue | Solution |
|-------|----------|
| **Network Error** | Backend not running or ngrok URL wrong |
| **401 Unauthorized** | JWT token issue or missing auth header |
| **404 Not Found** | Wrong endpoint URL |
| **CORS Error** | Already fixed in SecurityConfig |
| **Ngrok Warning Page** | Already fixed with `ngrok-skip-browser-warning` header |
| **Timeout** | Backend taking too long / database not connected |

---

## Testing Login Flow

1. **Start backend**: `./mvnw spring-boot:run` in `Medico/`
2. **Start ngrok**: `ngrok http 8080`
3. **Update client.ts** with ngrok URL
4. **Restart Expo**: `npx expo start --clear`
5. **Open app** on device via Expo Go
6. **Try to register** a new doctor
7. **Check logs** in both:
   - React Native debugger (console)
   - Spring Boot console (backend)

---

## Ngrok Alternative (Physical Device)

If on same WiFi network, use your computer's IP:

```typescript
// Find your IP: ipconfig (Windows) or ifconfig (Mac/Linux)
const BASE_URL = 'http://192.168.1.XXX:8080/api';
```

**Note**: Ngrok is required for:
- Testing on different networks
- HTTPS (some features require HTTPS)
- External device testing
