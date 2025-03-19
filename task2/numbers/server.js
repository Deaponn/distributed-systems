const express = require("express");
const { engine } = require('express-handlebars');

const app = express();
const port = 3000;

app.use(express.static('public'))

app.engine('.hbs', engine({
    extname: ".hbs",
    layoutsDir: "./views"
}));
app.set('view engine', '.hbs');

app.get('/', (req, res) => {
    res.render('index');
});

app.get('/result', (req, res) => {
    computeResult(req.query.equation, res);
});

app.listen(port, () => {
    console.log(`Application is up and running on port ${port}`);
});

async function computeResult(equation, res) {
    const query = await fetch(`https://newton.now.sh/api/v2/simplify/${encodeURIComponent(equation)}`);
    if (!query.ok) {
        return res.render('failure', { "errorCode": query.status, "errorMessage": "Could not calculate the result" });
    }
    // error code 429 for too many requests
    const { result } = await query.json();
    const sanitizedResult = !Number.isNaN(Number(result)) ? Number(result) : 3;
    const wholeResult = Math.round(sanitizedResult);
    const naturalResult = Math.abs(wholeResult);

    const response = await Promise.all([
        await fetch(`https://api.math.tools/numbers/base/binary?number=${wholeResult}`),
        await fetch(`https://api.math.tools/numbers/cardinal?number=${wholeResult}`),
        await fetch(`http://numbersapi.com/${naturalResult}/trivia`),
        await fetch(`http://numbersapi.com/${naturalResult}/math`),
        await fetch(`http://numbersapi.com/${naturalResult}/year`),
        await fetch(`https://api.isevenapi.xyz/api/iseven/${naturalResult}`)
    ]);
    const [inBinary, inWords, triviaFact, mathFact, yearFact, isEven] = await Promise.all(response.map(v => v.text()));

    console.log(inBinary, inWords, triviaFact, mathFact, yearFact, isEven);

    res.render('result', { "result": result });
}
