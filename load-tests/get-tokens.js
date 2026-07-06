import http from 'k6/http';

export const options = { vus: 1, iterations: 1 };

export default function () {
  const tokens = [];

  for (let i = 0; i < 25; i++) {
    const email = `racetest_${i}@test.com`;

    const res = http.post('http://localhost:8080/api/auth/login', JSON.stringify({
      email: email,
      password: 'Password123',
    }), { headers: { 'Content-Type': 'application/json' } });

    if (res.status === 200) {
      const token = JSON.parse(res.body).access_token;
      tokens.push(token);
      console.log(`${email}: OK`);
    } else {
      console.log(`${email}: FAILED (${res.status}) ${res.body}`);
    }

    // Sleep between logins to avoid tripping the rate limiter
    if (i < 24) {
      const start = Date.now();
      while (Date.now() - start < 3000) { /* busy wait */ }
    }
  }

  console.log('--- ALL TOKENS ---');
  console.log(JSON.stringify(tokens));
}