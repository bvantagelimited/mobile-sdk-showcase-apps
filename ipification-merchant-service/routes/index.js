const qs = require('qs');

module.exports = async (fastify, options) => {
  const appConfig = fastify.config;
  const data_store = fastify.data_store;

  fastify.post("/register-device", async (req, res) => {
    const { state, device_token, device_type } = req.body || {};
    fastify.log.debug(`[register_device] get token -- state: ${state}, device_type: ${device_type}, device_token: ${device_token}`);
    if(state && device_token) await data_store.set(`device:${state}`, { device_token, device_type });
    res.send({ state, device_token, device_type });
  })

  fastify.get("/ipification/callback", async (req, res) => {
    const state = req.query.state;
    const redirect_uri = `${appConfig.ROOT_URL}/ipification/callback`;
    const config = {headers: {'Content-Type': 'application/x-www-form-urlencoded'}};
    
    const body = {
      code: req.query.code,
			redirect_uri: redirect_uri,
			grant_type: 'authorization_code',
			client_id: appConfig.APP_ID,
			client_secret: appConfig.APP_SECRET
    };

    try {
      fastify.log.debug(`[ip_callback] get token -- state: ${state}, body: ${JSON.stringify(body)}`);
      const token_url = `${appConfig.KEYCLOAK_AUTH_URL}/realms/ipification/protocol/openid-connect/token`;
      const response = await fastify.axios.post(token_url, qs.stringify(body), config);

      fastify.log.debug(`[ip_callback] get token -- data: ${JSON.stringify(response.data)}`);
      // store data
      data_store.set(`user_info:${state}`, response.data);

      // send push notification to mobile app
      const device_info = await data_store.get(`device:${state}`);
      if(device_info) await fastify.send_notification(device_info,
        'Merchant Service',
        'Verification is successful. please back to your app/website'
      );

      res.send();
    } catch (error) {
      res.status(400).send(error.message);
    }
  });

  fastify.get("/user-info", async (req, res) => {
    const { state } = req.query || {};
    if(!state) return res.status(401).send();

    const data = await data_store.get(`user_info:${state}`);
    fastify.log.debug(`[user_info] get token -- state: ${state}, data: ${JSON.stringify(data)}`);

    if(!data) return res.status(401).send();

    await data_store.del(`user_info:${state}`);

    res.send(data);
  })

};