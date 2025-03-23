export default class RateLimitter {
    usersMap = {};

    constructor(rateLimit, purgeTime) {
        setInterval(() => {
            for (const key of Object.keys(this.usersMap)) {
                this.usersMap[key].requests = Math.max(0, this.usersMap[key].requests - 1);
            }
        }, rateLimit * 1000); // rateLimit in seconds

        setInterval(() => {
            for (const key of Object.keys(this.usersMap)) {
                if (this.usersMap[key].eat < Date.now()) delete this.usersMap[key];
            }
        }, purgeTime * 1000); // purgeTime in seconds
    }

    rateRequest({ uid, rateLimit, eat }) {
        if (this.usersMap[uid] == undefined) {
            this.usersMap[uid] = { requests: 0, eat };
            return true;
        }
        if (this.usersMap[uid].requests > rateLimit) return false;
        this.usersMap[uid].requests++;
        return true;
    }
}