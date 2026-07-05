import http from "k6/http";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    scenarios: {
        ramp: {
            executor: "ramping-vus",
            stages: [
                { duration: "30s", target: 10 },
                { duration: "30s", target: 25 },
                { duration: "30s", target: 50 },
                { duration: "30s", target: 75 },
                { duration: "30s", target: 100 },
                { duration: "30s", target: 0 },
            ],
        },
    },
};

export default function () {
    http.get(`${BASE_URL}/${SHORT_CODE}`, {
        redirects: 0,
    });
}