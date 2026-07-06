import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 30,
  duration: '20s',
};

export default function () {
  const res = http.post('http://localhost:8080/api/auth/login', JSON.stringify({
    email: 'benchattdemo@demo.com',
    password: 'Benchdemo123',
  }), { headers: { 'Content-Type': 'application/json' } });

  if (res.status !== 200) {
    console.log(`Status: ${res.status}, Body: ${res.body}`);
  }

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response under 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}