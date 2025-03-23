import express from "express";
import cookieParser from "cookie-parser";
import { engine } from "express-handlebars";
import dotenv from "dotenv";
import { GoogleGenerativeAI } from "@google/generative-ai";
import {
    parseInBinary,
    parseInWords,
    validateInput,
    applyElementwise,
    parseFact,
    parseIsEven,
} from "./helpers.js";
import { createToken, validateToken, getUUID } from "./cryptography.js";
import RateLimitter from "./RateLimitter.js";

const { parsed: env } = dotenv.config();

const secret = env.SERVER_SECRET;
const genAI = new GoogleGenerativeAI(env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

const rateLimitter = new RateLimitter(env.RATE_LIMIT, env.PURGE_TIME);

const app = express();
const port = 3000;

app.use(express.static("public"));
app.use(cookieParser());

app.engine(
    ".hbs",
    engine({
        extname: ".hbs",
        layoutsDir: "./views",
    })
);
app.set("view engine", ".hbs");

app.get("/", (req, res) => {
    const tokenUrl = `${req.protocol}://${req.get('host')}/token`;
    res.render("index", { tokenUrl });
});

app.use("/result", (req, res, next) => {
    const tokenUrl = `${req.protocol}://${req.get('host')}/`;

    if (req.cookies.jwt == undefined) {
        return res.render("failure", { errorCode: 401, errorMessage: `Nie masz dostępu do tej strony. Zdobądź klucz do API pod adresem ${tokenUrl}.` })
    }

    const [success, jwt] = validateToken(req.cookies.jwt, secret);

    if (!success) {
        return res.render("failure", { errorCode: 401, errorMessage: `Nie masz dostępu do tej strony. ${jwt.error}. Zdobądź klucz do API pod adresem ${tokenUrl}.` })
    }

    if (!rateLimitter.rateRequest(jwt)) {
        return res.render("failure", { errorCode: 429, errorMessage: `Nie masz dostępu do tej strony. Przekroczono limit ${jwt.rateLimit} zapytań na ${env.RATE_LIMIT} sekund.` })
    }

    req.jwt = jwt;
    next();
});

app.get("/result", (req, res) => {
    const [result, error] = validateInput(req.query.equation);
    if (!result) {
        res.status(400);
        return res.render("failure", { errorCode: 400, errorMessage: error });
    }
    computeResult(req.query.equation, res);
});

app.get("/token", (req, res) => {
    const validFor = (req.query.seconds ?? 300) * 1000; // in miliseconds
    const rateLimit = req.query.rateLimit ?? 10;
    const now = Date.now();
    const jwt = {
        iat: now,
        eat: now + validFor,
        uid: getUUID(),
        rateLimit,
    };

    const cookieOptions = {
        httpOnly: true,
        secure: true,
        sameSite: true,
        expires: new Date(now + 2 * validFor)
    };
    res.type("application/json").cookie("jwt", createToken(jwt, secret), cookieOptions).send({ success: true });
});

app.listen(port, () => {
    console.log(`Application is up and running on port ${port}`);
});

async function computeResult(equation, res) {
    const query = await fetch(
        `https://newton.now.sh/api/v2/simplify/${encodeURIComponent(equation)}`
    );

    const { result } = await query.json();

    if (!query.ok || result.includes("?")) {
        res.status(400);
        return res.render("failure", {
            errorCode: 400,
            errorMessage: "Zewnętrzny serwis nie mógł obliczyć wyniku",
        });
    }

    const sanitizedResult = !Number.isNaN(Number(result)) ? Number(result) : 5;
    const wholeResult = Math.round(sanitizedResult);
    const naturalResult = Math.abs(wholeResult);

    const response = await Promise.all([
        await fetch(`https://api.math.tools/numbers/base/binary?number=${wholeResult}`),
        await fetch(`https://api.math.tools/numbers/cardinal?number=${wholeResult}`),
        await fetch(`http://numbersapi.com/${wholeResult}/trivia`),
        await fetch(`http://numbersapi.com/${wholeResult}/math`),
        await fetch(`http://numbersapi.com/${wholeResult}/year`),
        await fetch(`https://api.isevenapi.xyz/api/iseven/${naturalResult}`),
    ]);

    const [inBinary, inWords, triviaFact, mathFact, yearFact, isEven] = await Promise.all(
        applyElementwise(response, [
            parseInBinary,
            parseInWords,
            parseFact,
            parseFact,
            parseFact,
            parseIsEven,
        ])
    );

    const prompt = `Napisz krótką historyjkę wykorzystując następujące fakty na temat liczby ${wholeResult}:
    * ${triviaFact}
    * ${mathFact}
    * ${yearFact}
    Niech historyjka będzie po polsku, niezbyt długa. Do 5 zdań.
    `;

    const output = await model.generateContent(prompt);
    const story = output.response.text();

    const disclaimer = Number.isNaN(Number(result))
        ? `Do dalszych zapytań API wykorzystano liczbę ${wholeResult},
        ponieważ wynik zwrócony pierwszym zapytaniem daje NaN przy użyciu Number(result)`
        : "";

    res.render("result", {
        equation,
        result,
        disclaimer,
        inBinary,
        inWords,
        isEven: isEven.iseven,
        advertisement: isEven.ad,
        facts: [triviaFact, mathFact, yearFact],
        story,
        prompt,
    });
}
