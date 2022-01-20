const fp = require("fastify-plugin");
const redis = require("ioredis");
const json_cache = require("redis-json");

const redis_client = new redis(process.env.REDIS_URL);
const redis_store = new json_cache(redis_client, { prefix: "merchant_service:" });

module.exports = fp(
  async (fastify, options, done) => {
    const data_store = {
      // session expire time - 30 mins
      set: async (key, object, options = { expire: 30 * 60 * 1000 }) => {
        await redis_store.set(key, { object }, options);
      },
      get: async (key) => {
        const value = await redis_store.get(key);
        return value ? value.object : null;
      },
      del: async (key) => {
        await redis_store.del(key);
      },
    };

    fastify.decorate("data_store", data_store);
    done();
  },
  {
    fastify: "3.x",
    name: "fastify-data-store",
  }
);
