document.addEventListener("DOMContentLoaded", updateMath, false);

function updateMath() {
    const input = document.getElementById("equation");
    const mathContainer = document.getElementById("math");
    if (!input || !mathContainer) return;
    mathContainer.innerHTML = "$$" + parseValue(input.value) + "$$";
    MathJax.typeset();
}

function parseValue(value) {
    return value.replace("*", "\\cdot");
}

async function getToken(url) {
    const response = await fetch(url);
    const success = await response.json();
    alert(`Zdobycie tokenu${success.success ? " " : " nie "}powiodło się`);
}
