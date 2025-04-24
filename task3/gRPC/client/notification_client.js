/*
 *
 * Copyright 2015 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

const messages = require("./notifications_pb.js");
const services = require("./notifications_grpc_pb.js");

const grpc = require("@grpc/grpc-js");
const readline = require("readline/promises");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.once("close", () => {
    // hit CTRL-D for linux to end process, CTRL-Z for windows
    console.log("program stopped");
});

const decodeSport = (sport) => {
    for (const [name, value] of Object.entries(messages.Sport)) {
        if (value === sport) return name;
    }
};

const decodePlayersList = (list) => {
    return list.map((player) => `${player.getName()}, number ${player.getNumber()}`).join(", ");
};

const displayEvent = (event) => {
    const [place, sport, prize, players] = [
        event.getPlace(),
        decodeSport(event.getSport()),
        event.getPrize(),
        decodePlayersList(event.getPlayersList()),
    ];
    console.log(
        `New ${sport} event in ${place}. Prize is ${prize} PLN. Players attending are ${players}`
    );
};

class Connection {
    constructor(client, subscriberId) {
        this.client = client;
        this.subscriberId = subscriberId;
        this.subscriptions = [];
        this.connect();
    }

    connect() {
        this.call = this.client.subscribeTo(function (error, obj) {});

        const initialRequest = new messages.SubscriptionDetails();
        initialRequest.setSubscriberid(this.subscriberId);

        this.call.write(initialRequest);

        this.resubscribe();

        this.call.on("data", (event) => {
            if (this.subscriptions.length === 0) {
                for (const sub of event.getSubscriptionsList()) {
                    const request = new messages.SubscriptionDetails();
                    request.setSubscriberid(this.subscriberId);
                    request.setPlace(sub.getPlace());
                    request.setSport(sub.getSport());
                    this.subscriptions.push(request);
                }
            }
            displayEvent(event);
        });

        this.call.on("error", () => {
            console.log("trying to reconnect...");
            setTimeout(() => {
                this.connect();
            }, 1000);
        });
    }

    resubscribe() {
        for (const request of this.subscriptions) this.call.write(request);
    }

    write(message) {
        this.subscriptions.push(message);
        this.call.write(message);
    }

    end() {
        this.call.end();
    }
}

async function main() {
    const target = "localhost:50051";
    const client = new services.EventNotificationsClient(target, grpc.credentials.createInsecure());

    const subscriberId = await rl.question("Whats your subscriberId? ");

    const connection = new Connection(client, subscriberId);

    rl.on("line", (line) => {
        if (line === "stop") {
            connection.end();
            rl.close();
            return;
        }

        const [place, sport] = line.split(" ");

        const request = new messages.SubscriptionDetails();
        request.setSubscriberid(subscriberId);
        request.setPlace(place);
        request.setSport(Number(sport));

        connection.write(request);
    });
}

main();
