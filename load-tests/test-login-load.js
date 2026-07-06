import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 30,
  duration: '20s',
};

export default function () {
  const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
  const email = `loadtest_${uniqueId}@test.com`;

  http.post('http://localhost:8080/api/auth/register/attendee', JSON.stringify({
    full_name: 'Load Test User',
    email: email,
    password: 'Password123',
    phone: '+20100' + Math.floor(1000000 + Math.random() * 9000000),
    city: 'Cairo',
    interests: ['Technology', 'Gaming', 'Music'],
  }), { headers: { 'Content-Type': 'application/json' } });

  sleep(0.5);

  const res = http.post('http://localhost:8080/api/auth/login', JSON.stringify({
    email: email,
    password: 'Password123',
  }), { headers: { 'Content-Type': 'application/json' } });

  if (res.status !== 200) {
    console.log(`Login failed - Status: ${res.status}, Body: ${res.body}`);
  }

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response under 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}