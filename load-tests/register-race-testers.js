import http from 'k6/http';

export const options = { vus: 1, iterations: 1 };

export default function () {
  for (let i = 0; i < 25; i++) {
    const email = `racetest_${i}@test.com`;
    const phone = `+2010${String(1000000 + i).padStart(7, '0')}`;

    const res = http.post('http://localhost:8080/api/auth/register/attendee', JSON.stringify({
      full_name: `Race Tester ${i}`,
      email: email,
      password: 'Password123',
      phone: phone,
      city: 'Cairo',
      interests: ['Technology', 'Gaming', 'Music'],
    }), { headers: { 'Content-Type': 'application/json' } });

    console.log(`${email}: ${res.status}`);
  }
}