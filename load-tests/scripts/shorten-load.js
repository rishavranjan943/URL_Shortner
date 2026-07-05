import http from "k6/http";
import { check } from "k6";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";

export const options = {
    vus: 20,
    duration: "30s",
};

export default function () {

    const payload = JSON.stringify({
        longUrl:
            "https://example.com/" +
            __VU +
            "-" +
            __ITER,
    });

    const res = http.post(
        `${BASE_URL}/shorten`,
        payload,
        {
            headers: {
                "Content-Type":
                    "application/json",
            },
        }
    );

    check(res, {
        "Status 200": (r) => r.status === 200,
    });
}