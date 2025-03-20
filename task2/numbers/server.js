import express from "express";
import { engine } from "express-handlebars";
import dotenv from "dotenv";
import { GoogleGenerativeAI } from "@google/generative-ai";

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

const mockInBinary = (url) =>
    new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve({
                text: () =>
                    Promise.resolve(
                        JSON.stringify({
                            success: {
                                total: 1,
                            },
                            copyright: {
                                copyright: "2019-21 https://math.tools",
                            },
                            contents: {
                                number: 15,
                                base: {
                                    from: 10,
                                    to: 2,
                                },
                                answer: "1111",
                            },
                        })
                    ),
            });
        }, 300);
    });

const mockInWords = (url) =>
    new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve({
                text: () =>
                    Promise.resolve(
                        JSON.stringify({
                            success: {
                                total: 1,
                            },
                            copyright: {
                                copyright: "2019-21 https://math.tools",
                            },
                            contents: {
                                number: 10,
                                language: "en_US",
                                result: "ten",
                                cardinal: "ten",
                            },
                        })
                    ),
            });
        }, 300);
    });

const parseInBinary = (result) => {
    const parsed = JSON.parse(result);
    if (parsed.error != undefined) return `HTML Return code ${parsed.error.code}: ${parsed.error.message}`;
    return parsed.contents.answer;
}

const parseInWords = (result) => {
    const parsed = JSON.parse(result);
    if (parsed.error != undefined) return `HTML Return code ${parsed.error.code}: ${parsed.error.message}`;
    return parsed.contents.result;
}
