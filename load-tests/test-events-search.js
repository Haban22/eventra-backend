import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 30,
  duration: '20s',
};

export function setup() {
  const loginRes = http.post('http://localhost:8080/api/auth/login', JSON.stringify({
    email: 'benchattdemo@demo.com',
    password: 'Benchdemo123',
  }), { headers: { 'Content-Type': 'application/json' } });

  const token = JSON.parse(loginRes.body).access_token;
  return { token };
}

export default function (data) {
  const res = http.get('http://localhost:8080/api/v1/events?page=0&size=20', {
    headers: { Authorization: `Bearer ${data.token}` },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response under 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}