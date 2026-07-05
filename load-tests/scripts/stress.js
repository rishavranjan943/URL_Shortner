import http from "k6/http";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    scenarios: {
        stress: {
            executor: "ramping-vus",
            stages: [
                { duration: "1m", target: 50 },
                { duration: "1m", target: 100 },
                { duration: "1m", target: 200 },
                { duration: "1m", target: 300 },
            ],
        },
    },
};

export default function () {
    http.get(`${BASE_URL}/${SHORT_CODE}`, {
        redirects: 0,
    });
}