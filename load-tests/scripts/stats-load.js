import http from "k6/http";
import { check } from "k6";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    vus: 20,
    duration: "30s",
};

export default function () {

    const res = http.get(
        `${BASE_URL}/stats/${SHORT_CODE}`
    );

    check(res, {
        "Status 200": (r) => r.status === 200,
    });

}