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
const readline = require("readline");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false,
});

rl.once("close", () => {
    // hit CTRL-D for linux to end process, CTRL-Z for windows
    console.log("end of input");
});

function main() {
    const target = "localhost:50051";
    const client = new services.EventNotificationsClient(target, grpc.credentials.createInsecure());
    const subscriberId = "user";

    const call = client.subscribeTo(function (error, obj) {
        if (error) {
            callback(error);
        }
        console.log(obj);
    });

    call.on("data", function (feature) {
        console.log("data");
        console.log(feature.getPrize());
    });

    call.on("end", function () {
        // The server has finished sending
        console.log("end");
    });

    call.on("error", function (e) {
        // An error has occurred and the stream has been closed.
        console.log("error");
        console.log(e);
    });

    call.on("status", function (status) {
        // process status
        console.log("status");
        console.log(status);
    });

    rl.on("line", (line) => {
        if (line === "stop") {
            call.end();
            rl.close();
            return;
        }

        const [place, sport] = line.split(" ");

        const request = new messages.SubscriptionDetails();
        request.setSubscriberid(subscriberId);
        request.setPlace(place);
        request.setSport(Number(sport));

        call.write(request);
    });
}

main();
