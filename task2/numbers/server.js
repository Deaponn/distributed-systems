import express from "express";
import { engine } from "express-handlebars";
import dotenv from "dotenv";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { parseInBinary, parseInWords, mockInBinary, mockInWords, validateInput } from "./helpers.js";

const { parsed: env } = dotenv.config();

const genAI = new GoogleGenerativeAI(env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

const app = express();
const port = 3000;

app.use(express.static("public"));

app.engine(
    ".hbs",
    engine({
        extname: ".hbs",
        layoutsDir: "./views",
    })
);
app.set("view engine", ".hbs");

app.get("/", (req, res) => {
    res.render("index");
});

app.get("/result", (req, res) => {
    const [result, error] = validateInput(req.query.equation);
    if (!result) {
        res.status(400);
        return res.render("failure", { errorCode: 400, errorMessage: error });
    }
    computeResult(req.query.equation, res);
});

app.listen(port, () => {
    console.log(`Application is up and running on port ${port}`);
});

async function computeResult(equation, res) {
    const query = await fetch(
        `https://newton.now.sh/api/v2/simplify/${encodeURIComponent(equation)}`
    );
    if (!query.ok) {
        return res.render("failure", {
            errorCode: query.status,
            errorMessage: "Could not calculate the result",
        });
    }
    
    const { result } = await query.json();
    const sanitizedResult = !Number.isNaN(Number(result)) ? Number(result) : 5;
    const wholeResult = Math.round(sanitizedResult);
    const naturalResult = Math.abs(wholeResult);

    const response = await Promise.all([
        await mockInBinary(`https://api.math.tools/numbers/base/binary?number=${wholeResult}`),
        await mockInWords(`https://api.math.tools/numbers/cardinal?number=${wholeResult}`),
        await fetch(`http://numbersapi.com/${wholeResult}/trivia`),
        await fetch(`http://numbersapi.com/${wholeResult}/math`),
        await fetch(`http://numbersapi.com/${wholeResult}/year`),
        await fetch(`https://api.isevenapi.xyz/api/iseven/${naturalResult}`),
    ]);

    const [inBinary, inWords, triviaFact, mathFact, yearFact, isEven] = await Promise.all(
        response.map((v) => v.text())
    );

    const prompt = `Write me a short story using these three facts about number ${wholeResult}:
    * ${triviaFact}
    * ${mathFact}
    * ${yearFact}
    Dont make it too long, 5 phrases is enough.
    `;

    const output = await model.generateContent(prompt);
    const story = output.response.text();

    res.render("result", {
        equation,
        result,
        disclaimer: Number.isNaN(Number(result))
            ? `Do dalszych zapytań API wykorzystano liczbę ${wholeResult}, ponieważ wynik zwrócony pierwszym zapytaniem daje NaN przy użyciu Number(result)`
            : "",
        inBinary: parseInBinary(inBinary),
        inWords: parseInWords(inWords),
        isEven: JSON.parse(isEven).iseven ? "parzysta" : "nieparzysta",
        advertisement: JSON.parse(isEven).ad,
        facts: [triviaFact, mathFact, yearFact],
        story,
        prompt,
    });
}
