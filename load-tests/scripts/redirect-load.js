import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    vus: 50,
    duration: "30s",

    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<500"],
    },
};

export default function () {

    const res = http.get(
        `${BASE_URL}/${SHORT_CODE}`,
        {
            redirects: 0,
        }
    );

    check(res, {
        "302 Redirect": (r) => r.status === 302,
        "Latency <500ms": (r) => r.timings.duration < 500,
    });

    sleep(1);
}