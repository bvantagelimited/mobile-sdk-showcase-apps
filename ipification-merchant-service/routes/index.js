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

    const device_id = req.body ? req.body.device_id : null;
    if(!device_id) {
      fastify.log.debug(`[ipification_notification] device_id is required`);
      res.status(400).send("device_id is required");
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
      // send push notification to mobile app
      fastify.send_notification(device_info,
        'Merchant Service',
        'Verification is successful. please back to your app/website'
      );

      res.send();
    } catch (error) {
      res.status(400).send(error.message);
    }
  });
};