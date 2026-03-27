/**
 * 테스트 유저 20명 사전 등록 스크립트
 * realistic-load.js 실행 전 딱 한 번만 실행하면 됩니다.
 *
 * 실행:
 *   docker run --rm -i grafana/k6 run - < k6/setup-test-users.js
 *   docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 grafana/k6 run - < k6/setup-test-users.js
 */

import http from "k6/http";
import { check } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";

const SPORTS  = ["FOOTBALL", "BASKETBALL", "BADMINTON", "TABLE_TENNIS", "VOLLEYBALL", "TENNIS"];
const GENDERS = ["MALE", "FEMALE"];
const TOWNS   = ["SEOUL", "BUSAN", "DAEGU", "INCHEON", "GWANGJU", "DAEJEON", "GYEONGGI", "ULSAN"];

export const options = {
  vus: 1,
  iterations: 1, // 한 번만 실행
};

export default function () {
  let created = 0;
  let skipped = 0;

  for (let i = 1; i <= 20; i++) {
    const user = {
      email:    `loadtest${i}@matchfit.test`,
      password: "Test1234!",
      nickname: `부하테스터${i}`,
      gender:   GENDERS[i % 2],
      sports:   SPORTS[i % SPORTS.length],
      age:      20 + (i % 20),
      town:     TOWNS[i % TOWNS.length],
    };

    const res = http.post(
      `${BASE_URL}/api/user/signup`,
      JSON.stringify(user),
      { headers: { "Content-Type": "application/json" } }
    );

    if (res.status === 200) {
      created++;
      console.log(`[OK] ${user.email} 생성 완료`);
    } else if (res.status === 409) {
      // 이미 존재 (이메일/닉네임 중복) → 무시
      skipped++;
    } else {
      console.warn(`[WARN] ${user.email} 등록 실패: status=${res.status} body=${res.body}`);
    }
  }

  console.log(`\n=== 완료: 생성 ${created}명 / 이미 존재 ${skipped}명 ===`);
  console.log("이제 realistic-load.js를 실행할 수 있습니다.");
}
