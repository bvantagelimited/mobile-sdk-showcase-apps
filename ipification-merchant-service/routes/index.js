module.exports = async (fastify, options) => {
  const data_store = fastify.data_store;
  const app_config = fastify.config;

  // store device_id(state) and device token to send push after success message from IM
  fastify.post("/register-device", async (req, res) => {
    const { device_id, device_token, device_type } = req.body || {};
    fastify.log.debug(`[register_device] get token -- device_id: ${device_id}, device_type: ${device_type}, device_token: ${device_token}`);
    if(device_id && device_token) await data_store.set(`device:${device_id}`, { device_token, device_type });
    res.send({ device_id, device_token, device_type });
  })

  // IMBox invoke notification url after message complete
  fastify.post("/ipification_notification/:secret_key", async (req, res) => {
    if(req.params.secret_key !== app_config.NOTIFICATION_SECRET_KEY) {
      fastify.log.debug(`[ipification_notification] wrong secret key: ${req.params.secret_key}`);
      res.status(400).send();
      return;
    }

    const device_id = req.body ? req.body.state : null;
    const notification_type = req.body ? req.body.notification_type : null;
    const channel = req.body ? req.body.channel : null;

    if(!device_id) {
      fastify.log.debug(`[ipification_notification] device_id is required`);
      res.status(400).send("device_id is required");
      return;
    }

    if(!notification_type) {
      fastify.log.debug(`[ipification_notification] notification_type is required`);
      res.status(400).send("notification_type is required");
      return;
    }

    const device_info = await data_store.get(`device:${device_id}`);
    fastify.log.debug(`[ipification_notification] device_id: ${device_id}, device_info: ${JSON.stringify(device_info)}`);

    if(!device_info) {
      fastify.log.debug(`[ipification_notification] device not found`);
      res.send();
      return;
    }

    try {
      fastify.log.debug(`[ipification_notification] invoke send_notification`);
      const push_message = notification_type === 'session_completed' ? 'Verification is successful. please back to your app/website' : 'Your session is expired. Please back into your application';
      // send push notification to mobile app
      await fastify.send_notification(device_info,
        'Merchant Service',
        push_message
      );

      res.send();
    } catch (error) {
      fastify.log.error(error);
      res.status(400).send(error.message);
    }
  });
};