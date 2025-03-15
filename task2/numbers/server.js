const express = require("express");
const { engine } = require('express-handlebars');

const app = express();
const port = 3000;

app.engine('.hbs', engine({
    extname: ".hbs",
    layoutsDir: "./views"
}));
app.set('view engine', '.hbs');

app.get('/', (req, res) => {
    res.render('index', { "layout": "index", "name": "Alan", "hometown": "Somewhere, TX",
        "kids": [{"name": "Jimmy", "age": "12"}, {"name": "Sally", "age": "4"}]});
});

app.listen(port, () => {
    console.log(`Application is up and running on port ${port}`);
});
