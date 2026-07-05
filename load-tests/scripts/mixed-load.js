import http from "k6/http";
import { sleep } from "k6";

const BASE_URL = "https://url-shortener-latest-ulj8.onrender.com";
const SHORT_CODE = "QM7eGB";

export const options = {
    vus: 50,
    duration: "1m",
};

export default function () {

    if (Math.random() < 0.9) {

        http.get(
            `${BASE_URL}/${SHORT_CODE}`,
            {
                redirects: 0,
            }
        );

    } else {

        http.post(
            `${BASE_URL}/shorten`,
            JSON.stringify({
                longUrl:
                    "https://example.com/" +
                    __VU +
                    "-" +
                    __ITER,
            }),
            {
                headers: {
                    "Content-Type":
                        "application/json",
                },
            }
        );

    }

    sleep(1);
}