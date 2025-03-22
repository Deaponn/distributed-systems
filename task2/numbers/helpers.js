export const parseInBinary = async (response) => {
    const result = await response.json();
    if (response.status >= 400)
        return Promise.resolve(`HTML Return code ${result.error.code}: ${result.error.message}`);
    return Promise.resolve(result.contents.answer);
};

export const parseInWords = async (response) => {
    const result = await response.json();
    if (response.status >= 400)
        return Promise.resolve(`HTML Return code ${result.error.code}: ${result.error.message}`);
    return Promise.resolve(result.contents.result);
};

export const parseFact = async (response) => {
    const text = await response.text();
    if (response.status >= 400)
        return Promise.resolve(
            `Błąd uzyskania odpowiedzi z zewnętrznego API z kodem ${response.status}, treść: ${text}`
        );
    return Promise.resolve(text);
};

export const parseIsEven = async (response) => {
    const json = await response.json();
    if (response.status >= 400)
        return Promise.resolve({
            iseven: `Błąd uzyskania odpowiedzi z zewnętrznego API z kodem ${response.status}, treść: ${json.error}`,
            ad: `Błąd uzyskania odpowiedzi z zewnętrznego API z kodem ${response.status}, treść: ${json.error}`,
        });
    return Promise.resolve({...json, iseven: `Liczba ta jest ${json.iseven ? "parzysta" : "nieparzysta"}`});
};

const numberCharacters = "0123456789";
const operationCharacters = "+-*/";
const parenthesisCharacters = "()";
const validCharacters = numberCharacters + operationCharacters + parenthesisCharacters;

export const validateInput = (input) => {
    let openParenthesis = 0;
    if (!validCharacters.includes(input[0]))
        return [false, `Wyrażenie zawiera nielegalny symbol: '${input[0]}' pod indeksem 0`];
    if ("*/)".includes(input[0]))
        return [false, `Wyrażenie nie może zaczynać się od symbolu '${input[0]}' pod indeksem 0`];
    if (input[0] == "(") openParenthesis++;
    for (let i = 1; i < input.length; i++) {
        if (!validCharacters.includes(input[i]))
            return [false, `Wyrażenie zawiera nielegalny symbol: '${input[i]}' pod indeksem ${i}`];
        if (operationCharacters.includes(input[i - 1]) && operationCharacters.includes(input[i]))
            return [
                false,
                `Symbole matematyczne sąsiadują ze sobą: '${input[i - 1]}${
                    input[i]
                }' pod indeksem ${i - 1}`,
            ];
        if (input[i] == "(") openParenthesis++;
        if (input[i] == ")") openParenthesis--;
        if (openParenthesis < 0)
            return [
                false,
                `Nawias zamykający bez poprzedzającego nawiasu otwierającego pod indeksem ${i}`,
            ];
        if (
            (input[i - 1] == ")" && numberCharacters.includes(input[i])) ||
            (numberCharacters.includes(input[i - 1]) && input[i] == "(")
        )
            return [
                false,
                `Brak operacji matematycznej między nawiasem a liczbą pod indeksem ${i}`,
            ];
        if (
            (input[i - 1] == "(" && operationCharacters.includes(input[i])) ||
            (operationCharacters.includes(input[i - 1]) && input[i] == ")")
        )
            return [false, `Brak liczby między nawiasem a operacją matematyczną pod indeksem ${i}`];
    }
    if (openParenthesis > 0)
        return [false, "Zbyt dużo otwartych nawiasów, nie wszystkie zostały zamknięte"];
    return [true, "ok"];
};

export const applyElementwise = (elements, functions) =>
    elements.map((element, idx) => functions[idx](element));
