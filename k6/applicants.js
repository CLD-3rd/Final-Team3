import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 10,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const POST_ID = __ENV.POST_ID || "2";
const TOKEN = __ENV.TOKEN;

export default function () {
  const url = `${BASE_URL}/api/posts/${POST_ID}/applicants`;
  const params = {
    headers: {
      Authorization: `Bearer ${TOKEN}`,
    },
  };

  const res = http.get(url, params);
  check(res, {
    "status is 200": (r) => r.status === 200,
  });
  sleep(1);
}
