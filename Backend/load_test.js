import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export let options = {
  vus: 50,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE_URL = 'http://127.0.0.1:8080/api';

export default function () {
  group('SignUp and Login', function () {
    const uniqueEmail = `testuser_${uuidv4()}@mail.com`;
    const password = 'abcdefgh';

    const signupPayload = JSON.stringify({
      name: 'Test User',
      email: uniqueEmail,
      password: password,
      batteryCapacityKwh: 70,
      fullRangeKm: 350,
    });

    const signupRes = http.post(`${BASE_URL}/clients/signup`, signupPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(signupRes, {
      'signup status 200': (r) => r.status === 200,
      'signup response has correct email': (r) => r.json('email') === uniqueEmail,
    }) || console.error('Signup failed:', signupRes.body);

    const loginPayload = JSON.stringify({
      email: uniqueEmail,
      password: password,
    });

    const loginRes = http.post(`${BASE_URL}/clients/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(loginRes, {
      'login status 200': (r) => r.status === 200,
      'login response has correct email': (r) => r.json('email') === uniqueEmail,
      'login response has name': (r) => r.json('name') === 'Test User',
    }) || console.error('Login failed:', loginRes.body);

    const clientId = loginRes.json('id');
    if (!clientId) {
      console.error('Client ID not returned in login response');
      return;
    }

    let chargerId;

    group('Create Station and Charger', function () {
      const uniqueLat = 38.6 + Math.random() * 0.01;
      const uniqueLong = -9.1 + Math.random() * 0.01;

      const stationPayload = JSON.stringify({
        name: `Estação Teste ${uuidv4()}`,
        address: 'Rua Nova',
        city: 'Aveiro',
        latitude: uniqueLat,
        longitude: uniqueLong,
      });

      const stationRes = http.post(`${BASE_URL}/stations`, stationPayload, {
        headers: { 'Content-Type': 'application/json' },
      });

      check(stationRes, {
        'station created': (r) => r.status === 200 || r.status === 201,
      }) || console.error('Station creation failed:', stationRes.body);

      const stationId = stationRes.json('id');
      if (!stationId) {
        console.error('Station ID not returned, aborting charger creation');
        return;
      }

      const chargerPayload = JSON.stringify({
        stationId: stationId,
        chargerType: 'AC_STANDARD',
        status: 'AVAILABLE',
        pricePerKwh: 0.8,
      });

      const chargerRes = http.post(`${BASE_URL}/chargers`, chargerPayload, {
        headers: { 'Content-Type': 'application/json' },
      });

      check(chargerRes, {
        'charger created': (r) => r.status === 200 || r.status === 201,
      }) || console.error('Charger creation failed:', chargerRes.body);

      chargerId = chargerRes.json('id');
    });

    if (!chargerId) {
      console.error('No charger created, aborting reservation');
      return;
    }

    group('Create Reservation', function () {
      const now = new Date();
      const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);

      const reservationPayload = JSON.stringify({
        clientId: clientId,
        chargerId: chargerId,
        startTime: now.toISOString(),
        estimatedEndTime: tomorrow.toISOString(),
        batteryLevelStart: 30,
        estimatedKwh: 20,
        estimatedCost: 5,
      });

      const reservationRes = http.post(`${BASE_URL}/reservations`, reservationPayload, {
        headers: { 'Content-Type': 'application/json' },
      });

      check(reservationRes, {
        'reservation created': (r) => r.status === 201,
      }) || console.error('Reservation creation failed:', reservationRes.body);
    });

    group('List Reservations by Client', function () {
      const res = http.get(`${BASE_URL}/reservations/client/${clientId}`, {
        headers: { 'accept': '*/*' },
      });

      check(res, {
        'reservations fetched': (r) => r.status === 200,
      }) || console.error('Fetching client reservations failed:', res.body);
    });

    sleep(1);
  });
}
