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
    res.render('index', { "layout": "index" });
});

app.get('/result', (req, res) => {
    res.render('result', { "layout": "result", "result": req.query.equation });
});

app.listen(port, () => {
    console.log(`Application is up and running on port ${port}`);
});
