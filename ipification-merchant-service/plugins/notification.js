const fp = require("fastify-plugin");


module.exports = fp(
  async (fastify, options, done) => {
    const appConfig = fastify.config;

    const send_notification = async (device_info, title, body) => {
      const { device_type, device_token } = device_info;
      let data

      if(device_type == 'android') {
        data = {
          to: device_token,
          data: {
            sound: 'default',
            content_available: true,
            priority: 'high',
            title: title,
            body: body
          }
        }
      } else {
        data = {
          to: device_token,
          notification: {
            title: title,
            body: body
          }
        }
      }

      const response = await fastify.axios.request({
        url: 'https://fcm.googleapis.com/fcm/send',
        method: 'POST',
        data: data,
        headers: {
          Authorization: `key=${appConfig.FIREBASE_SERVER_KEY}`
        }
      });

      return response;
    }

    fastify.decorate("send_notification", send_notification);
    done();
  },
  {
    fastify: "3.x",
    name: "fastify-push-notification",
  }
);
