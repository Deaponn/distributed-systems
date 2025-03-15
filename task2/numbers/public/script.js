document.addEventListener('DOMContentLoaded', updateMath, false);

function updateMath() {
    const input = document.getElementById("equation");
    const mathContainer = document.getElementById("math");
    mathContainer.innerHTML = "$$" + parseValue(input.value) + "$$";
    MathJax.typeset();
}

function parseValue(value) {
    return value.replace("*", "\\cdot");
}
