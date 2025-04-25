// client.js
const { Ice } = require("ice");
const { Demo } = require("./calculator"); // Zaimportuj wygenerowany kod
const readline = require("readline/promises");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

async function main() {
    let communicator;
    let status = 0;
    const serverAddress = "default -p 10000";

    const sharedIdentities = ["calc1", "calc2", "anything"];
    const nextIdentityIdx = () => {
        this.idx = this.idx === undefined ? sharedIdentities.length : this.idx + 1;
        if (this.idx === sharedIdentities.length) this.idx = 0;
        return this.idx;
    };

    try {
        communicator = Ice.initialize(process.argv);
        while (true) {
            const action = await rl.question(
                [
                    "1. Distance Point to Point (shared servant)",
                    "2. Distance Place to Point (individual servant)",
                    "3. Initialize Place with Point (setup before option 2.)",
                    "> ",
                ].join("\n")
            );
            switch (action[0]) {
                case "1": {
                    const points = await rl.question(
                        "Enter points in the following format: 3.14, -1.0 <> -1.2, 3.4\n> "
                    );
                    const [ax, ay, bx, by] = points
                        .split(" <> ")
                        .flatMap((p) => p.split(", "))
                        .map((s) => Number(s));

                    const proxyStr = `SharedCalc/${
                        sharedIdentities[nextIdentityIdx()]
                    }:${serverAddress}`;
                    const baseProxy = communicator.stringToProxy(proxyStr);
                    const sharedCalc = await Demo.DistanceCalculatorPrx.uncheckedCast(baseProxy);

                    let distance = await sharedCalc.calculateDistance(
                        new Demo.Point(ax, ay),
                        new Demo.Point(bx, by)
                    );

                    console.log(
                        `[${proxyStr}] Distance between A(${ax}, ${ay}) and B(${bx}, ${by}) = ${distance}`
                    );

                    break;
                }
                case "2": {
                    const request = await rl.question(
                        "Enter Place and Point in the following format: Krakow <> -1.0, 3.4\n> "
                    );
                    const [place, point] = request.split(" <> ");
                    const [px, py] = point.split(", ").map((s) => Number(s));

                    const proxyStr = `PlaceCalc/${place}:${serverAddress}`;
                    const baseProxy = communicator.stringToProxy(proxyStr);

                    const checkedFixedCalc = await Demo.PlaceDistanceCalculatorPrx.checkedCast(
                        baseProxy
                    );

                    console.log(
                        await new Promise((resolve) => {
                            console.log("Short sleep...");
                            setTimeout(() => resolve("Resuming..."), 5000);
                        })
                    );

                    let distance = await checkedFixedCalc.calculateDistance(new Demo.Point(px, py));
                    console.log(
                        `[${proxyStr}] Distance from ${place} to P(${px}, ${py}) = ${distance}`
                    );

                    break;
                }
                case "3": {
                    const request = await rl.question(
                        "Enter Place and Point in the following format: Krakow = -1.0, 3.4\n> "
                    );
                    const [place, point] = request.split(" = ");
                    const [px, py] = point.split(", ").map((s) => Number(s));

                    const proxyStr = `PlaceCalc/${place}:${serverAddress}`;
                    const baseProxy = communicator.stringToProxy(proxyStr);

                    const uncheckedFixedCalc =
                        Demo.PlaceDistanceCalculatorPrx.uncheckedCast(baseProxy);

                    console.log(
                        await new Promise((resolve) => {
                            console.log("Short sleep...");
                            setTimeout(() => resolve("Resuming..."), 5000);
                        })
                    );

                    await uncheckedFixedCalc.setPoint(new Demo.Point(px, py));
                    
                    const { x, y } = await uncheckedFixedCalc.getPoint();
                    console.log(`[${proxyStr}] Location of ${place} is set to P(${x}, ${y})`);

                    break;
                }
                default: {
                    console.log("Illegal option. Try again.");
                }
            }
        }
    } catch (e) {
        console.error("Client main error:", e);
        status = 1;
    } finally {
        if (communicator) await communicator.destroy();
    }
    process.exit(status);
}

main();
