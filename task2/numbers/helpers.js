export const mockInBinary = (url) =>
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

export const mockInWords = (url) =>
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

export const parseInBinary = (result) => {
    const parsed = JSON.parse(result);
    if (parsed.error != undefined)
        return `HTML Return code ${parsed.error.code}: ${parsed.error.message}`;
    return parsed.contents.answer;
};

export const parseInWords = (result) => {
    const parsed = JSON.parse(result);
    if (parsed.error != undefined)
        return `HTML Return code ${parsed.error.code}: ${parsed.error.message}`;
    return parsed.contents.result;
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
                `Symbole matematyczne sąsiadują ze sobą: '${input[i - 1]}${input[i]}' pod indeksem ${i - 1}`,
            ];
        if (input[i] == "(") openParenthesis++;
        if (input[i] == ")") openParenthesis--;
        if (openParenthesis < 0)
            return [false, `Nawias zamykający bez poprzedzającego nawiasu otwierającego pod indeksem ${i}`];
        if (
            (input[i - 1] == ")" && numberCharacters.includes(input[i])) ||
            (numberCharacters.includes(input[i - 1]) && input[i] == "(")
        )
            return [false, `Brak operacji matematycznej między nawiasem a liczbą pod indeksem ${i}`];
        if (
            (input[i - 1] == "(" && operationCharacters.includes(input[i])) ||
            (operationCharacters.includes(input[i - 1]) && input[i] == ")")
        )
            return [false, `Brak liczby między nawiasem a operacją matematyczną pod indeksem ${i}`];
    }
    if (openParenthesis > 0) return [false, "Zbyt dużo otwartych nawiasów, nie wszystkie zostały zamknięte"];
    return [true, "ok"];
};
