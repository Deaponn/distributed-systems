import * as crypto from "node:crypto";
import base64 from "base-64";

const computeHash = (string, secret) => {
    const hasher = crypto.createHash("RSA-SHA256");
    hasher.update(string + secret);
    return hasher.digest("hex");
}

export const createToken = (jwt, secret) => {
    const jwt_base64 = base64.encode(JSON.stringify(jwt));

    return jwt_base64 + "." + computeHash(jwt_base64, secret);
};

export const validateToken = (wholeJwt, secret) => {
    const [jwt_base64, hash] = wholeJwt.split(".");

    if (computeHash(jwt_base64, secret) != hash) return [false, { error: "Suma kontrolna tokenu jest niepoprawna" }];

    const token = JSON.parse(base64.decode(jwt_base64));

    if (token.eat <= Date.now()) return [false, { error: "Token utracił ważność" }];

    return [true, JSON.parse(base64.decode(jwt_base64))];
};

export const getUUID = () => crypto.randomUUID();
