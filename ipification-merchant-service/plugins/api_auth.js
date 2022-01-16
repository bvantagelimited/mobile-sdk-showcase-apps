const fp = require("fastify-plugin");

module.exports = fp(
  async (fastify, options, done) => {
    // xac thuc cho phep goi api theo server key
    const opts = {
      preValidation: (req, res, done) => {
        req.required_api_auth = true;
        done();
      },
    };
    fastify.decorate("api_auth", opts);

    // force check api auth before process
    fastify.addHook("preHandler", (req, res, hook_done) => {
      const appConfig = fastify.config;
      const required_api_auth = req.required_api_auth;
      const server_key = req.headers["server-key"];
      if (required_api_auth && appConfig.SERVER_KEY !== server_key) {
        res.code(401).send("you are not authenticated");
        hook_done();
      }
      hook_done();
    });
    done();
  },
  {
    fastify: "3.x",
    name: "fastify-imbox-auth",
  }
);
