const { Issuer } = require('openid-client');

module.exports = async (fastify, options) => {
  const dataStore = fastify.data_store;
  const appConfig = fastify.config;

  const redirectUri = `${appConfig.WEB_URL}/s2s/callback`;

  // receive s2s redirect_uri from IP
  fastify.get("/s2s/callback", async (req, res) => {
    const { state } = req.query || {};
    const ipBackchannelAuth = req.headers['ip-backchannel-im-auth'] === 'true';
    if(!ipBackchannelAuth) {
      console.log('ipBackchannelAuth is false');
      return res.send();
    }

    // if have error from IPification, client can redirect to error page
    const errorMessage = req.query.error || req.query.error_description;
    if(errorMessage){
      // redirect to error page or render error page
      console.log('callback errorMessage', errorMessage);
      return res.send(errorMessage);
    }

    const ipIssuer = await Issuer.discover(appConfig.IP_DISCOVER_URL);

    const client = new ipIssuer.Client({
      client_id: appConfig.S2S_CLIENT_ID,
      client_secret: appConfig.S2S_CLIENT_SECRET,
      redirect_uris: [redirectUri],
      response_types: ['code']
    });

    try {
      const params = req.query || {};
      const tokenSet = await client.callback(redirectUri, params, { state })

      if(tokenSet.error) {
        console.log('callback token error', tokenSet.error_description);
        return res.status(400).send(tokenSet.error_description);
      }

      const userInfo = await client.userinfo(tokenSet.access_token);
      await dataStore.set(state, userInfo);

      console.log("callback userInfo", userInfo);

      res.send(userInfo);

    } catch (err) {
      console.log("callback exception", err.message);
		  res.status(400).send(err.message);
	  }
  })

  // receive user info by state and exchange to your token
  fastify.post("/s2s/signin", async (req, res) => {
    const { state } = req.body || {};
    const userinfo = await dataStore.get(state);

    if(userinfo) {
      // add your code here to create your app token and response to client
      res.send(userinfo);
    } else {
      res.status(401).send();
    }
  })
}