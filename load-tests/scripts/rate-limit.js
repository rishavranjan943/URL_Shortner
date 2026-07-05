import http from "k6/http";
import { check } from "k6";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    vus: 150,
    iterations: 150,
};

export default function () {

    const res = http.get(
        `${BASE_URL}/${SHORT_CODE}`,
        {
            redirects: 0,
        }
    );

    check(res, {
        "302 or 429": (r) =>
            r.status === 302 ||
            r.status === 429,
    });
}