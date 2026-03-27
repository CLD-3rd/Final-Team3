/**
 * MatchFit 복합 시나리오 부하 테스트
 *
 * 실행 전 필수: k6/setup-test-users.js 를 먼저 한 번 실행해서 테스트 유저 20명 생성
 *   docker run --rm -i grafana/k6 run - < k6/setup-test-users.js
 *
 * 실행 (기본):
 *   docker run --rm -i grafana/k6 run - < k6/realistic-load.js
 *
 * 실행 (Grafana 실시간 모니터링 — Prometheus remote write):
 *   docker run --rm -i \
 *     -e BASE_URL=http://host.docker.internal:8080 \
 *     -e K6_PROMETHEUS_RW_SERVER_URL=http://host.docker.internal:9090/api/v1/write \
 *     grafana/k6 run --out experimental-prometheus-rw - < k6/realistic-load.js
 *
 * 시나리오 구성:
 *   A. read_heavy   (70%) - 필터 조합 목록 조회 (비인증)
 *   B. detail_view  (20%) - 상세 조회 (로그인 시 Redis 활성 유저 자동 기록)
 *   C. apply_cancel (10%) - 신청/취소 쓰기 트랜잭션 (인증 필요)
 */

import http from "k6/http";
import { check, sleep, group } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";

const SPORTS   = ["FOOTBALL", "BASKETBALL", "BADMINTON", "TABLE_TENNIS", "VOLLEYBALL", "TENNIS"];
const GENDERS  = ["MALE", "FEMALE"];
const SORT_TYPES = ["DATE", "POPULAR"];

// 테스트 유저 (setup-test-users.js로 사전 생성된 계정)
const TEST_USERS = Array.from({ length: 20 }, (_, i) => ({
  email: `loadtest${i + 1}@matchfit.test`,
  password: "Test1234!",
}));

// ─── 부하 프로파일 ───────────────────────────────────────────────
export const options = {
  scenarios: {
    // A: 목록 조회 (필터 조합, 비인증) — 전체 VU의 70%
    read_heavy: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 100 },  // 워밍업
        { duration: "1m",  target: 280 },  // 부하
        { duration: "30s", target: 350 },  // 압박
        { duration: "20s", target: 0   },  // 감소
      ],
      exec: "readHeavy",
      gracefulRampDown: "10s",
    },

    // B: 상세 조회 — 전체 VU의 20%
    detail_view: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 30  },
        { duration: "1m",  target: 80  },
        { duration: "30s", target: 100 },
        { duration: "20s", target: 0   },
      ],
      exec: "detailView",
      gracefulRampDown: "10s",
    },

    // C: 신청/취소 (DB 쓰기 트랜잭션 + Redis 갱신) — 전체 VU의 10%
    apply_cancel: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 15 },
        { duration: "1m",  target: 40 },
        { duration: "30s", target: 50 },
        { duration: "20s", target: 0  },
      ],
      exec: "applyAndCancel",
      gracefulRampDown: "10s",
    },
  },

  thresholds: {
    // 전체 에러율 5% 미만
    "http_req_failed": ["rate<0.05"],
    // 시나리오별 응답시간 기준 (태그: scenario = options.scenarios의 key 이름)
    "http_req_duration{scenario:read_heavy}":        ["p(95)<1000"],
    "http_req_duration{scenario:detail_view}":        ["p(95)<1500"],
    "http_req_duration{scenario:apply_cancel}":      ["p(95)<3000"],
  },
};

// ─── Setup: 테스트 유저 로그인 → JWT 토큰 수집 ───────────────────
export function setup() {
  const tokens = [];

  for (const user of TEST_USERS) {
    const res = http.post(
      `${BASE_URL}/api/user/login`,
      JSON.stringify({ email: user.email, password: user.password }),
      { headers: { "Content-Type": "application/json" } }
    );

    if (res.status === 200) {
      const body = JSON.parse(res.body);
      const token = body.result?.token;
      if (token) tokens.push(token);
    } else {
      console.warn(`[setup] login failed for ${user.email} → status ${res.status}`);
    }
  }

  console.log(`[setup] JWT 토큰 확보: ${tokens.length}/${TEST_USERS.length}명`);
  if (tokens.length === 0) {
    console.error("[setup] 유효한 토큰 없음 — setup-test-users.js 먼저 실행하세요");
  }
  return { tokens };
}

// ─── 시나리오 A: 필터 조합 목록 조회 ────────────────────────────
export function readHeavy(_data) {
  group("A. 목록 조회 (필터)", () => {
    const sports   = randItem([...SPORTS, null, null]);   // null 비중 높여 필터 없는 경우 포함
    const gender   = randItem([...GENDERS, null]);
    const sortType = randItem(SORT_TYPES);
    const page     = randInt(0, 9);

    let url = `${BASE_URL}/api/posts/list?page=${page}&size=10&sortType=${sortType}`;
    if (sports) url += `&sports=${sports}`;
    if (gender) url += `&gender=${gender}`;

    const res = http.get(url);
    check(res, { "list 200": (r) => r.status === 200 });
  });

  sleep(0.2 + Math.random() * 0.3); // 0.2 ~ 0.5s
}

// ─── 시나리오 B: 상세 조회 ───────────────────────────────────────
export function detailView(_data) {
  const postId = randInt(1, 1000);

  group("B. 상세 조회", () => {
    const detailRes = http.get(`${BASE_URL}/api/posts/${postId}`);
    check(detailRes, {
      "detail 200 or 404": (r) => r.status === 200 || r.status === 404,
    });
  });

  sleep(1 + Math.random() * 2); // 1 ~ 3s
}

// ─── 시나리오 C: 신청 → (대기) → 취소 ──────────────────────────
export function applyAndCancel(data) {
  if (!data || data.tokens.length === 0) {
    sleep(1);
    return;
  }

  const token   = randItem(data.tokens);
  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };

  // 100~900번 포스트 대상 (CLOSED 가능성 줄이기 위해 끝 구간 제외)
  const postId = randInt(100, 900);

  group("C. 신청", () => {
    const applyRes = http.post(
      `${BASE_URL}/api/posts/${postId}/apply`,
      null,
      { headers }
    );
    // 200: 성공 / 400·409: 이미 신청 or 모집마감 / 404: 존재 않음 → 모두 예상 범위
    const applyOk = check(applyRes, {
      "apply OK (200/400/404/409)": (r) =>
        [200, 400, 404, 409].includes(r.status),
    });

    // 신청 성공 시에만 취소 (Redis 캐시 갱신 경로 검증)
    if (applyRes.status === 200) {
      sleep(0.3 + Math.random() * 0.7); // 짧은 체류 후 취소

      group("C. 취소", () => {
        const cancelRes = http.del(
          `${BASE_URL}/api/posts/${postId}/apply`,
          null,
          { headers }
        );
        check(cancelRes, {
          "cancel OK (200/404)": (r) => r.status === 200 || r.status === 404,
        });
      });
    }
  });

  sleep(0.5 + Math.random() * 1.5); // 0.5 ~ 2s
}

// ─── 유틸 ────────────────────────────────────────────────────────
function randItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
