"use strict";

const Env = require("fastify-env");
const S = require("fluent-json-schema");
const Sensible = require("fastify-sensible");
const helmet = require("fastify-helmet");
const Rollbar = require("rollbar");
const dirTree = require("directory-tree");

if (process.env.ROLLBAR_TOKEN) {
  new Rollbar({
    accessToken: process.env.ROLLBAR_TOKEN,
    captureUncaught: true,
    captureUnhandledRejections: true,
  });
}

module.exports = async (fastify, opts) => {
  fastify
    .register(Env, {
      schema: S.object()
        .prop("WEB_URL", S.string().required())
        .prop("NOTIFICATION_SECRET_KEY", S.string().required())
        .prop("FIREBASE_SERVER_KEY", S.string().required())
        .prop("IP_DISCOVER_URL", S.string())
        .prop("S2S_CLIENT_ID", S.string())
        .prop("S2S_CLIENT_SECRET", S.string())
        .valueOf()
    })

  // `fastify-sensible` adds many  small utilities, such as nice http errors.
  fastify.register(Sensible);
  // important security headers for Fastify
  fastify.register(helmet, {
    contentSecurityPolicy: false,
  });
  
  fastify.register(require('fastify-axios'));
  // register plugins
  const treePlugins = dirTree("./plugins/", { extensions: /\.js$/ });
  treePlugins.children.forEach((fileInfo) => {
    const filename = fileInfo.name.split(".").shift();
    fastify.register(require(`./plugins/${filename}`));
  });

  // register routes
  const treeRoutes = dirTree("./routes/", { extensions: /\.js$/ });
  treeRoutes.children.forEach((fileInfo) => {
    const filename = fileInfo.name.split(".").shift();
    fastify.register(require(`./routes/${filename}`));
  });
};
