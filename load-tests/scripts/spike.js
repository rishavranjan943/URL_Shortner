import http from "k6/http";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    scenarios: {
        spike: {
            executor: "ramping-vus",
            stages: [
                { duration: "30s", target: 20 },
                { duration: "10s", target: 300 },
                { duration: "30s", target: 20 },
            ],
        },
    },
};

export default function () {
    http.get(`${BASE_URL}/${SHORT_CODE}`, {
        redirects: 0,
    });
}