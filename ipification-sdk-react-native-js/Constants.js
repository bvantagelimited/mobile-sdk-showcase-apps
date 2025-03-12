const ENV = {
  SANDBOX: 'sandbox',
  PRODUCTION: 'production'
};

export default Constants ={
  ENV: ENV.SANDBOX, //supports "sandbox" or "production"
  CLIENT_ID : "TEST", 
  REDIRECT_URI: "TEST",
  CLIENT_SECRET: "TEST", // for demo purpose only
  
  TOKEN_EXCHANGE_HOST : "https://api.stage.ipification.com",
  TOKEN_PATH:
  "/auth/realms/ipification/protocol/openid-connect/token",
  GET_USER_INFO_PATH:
  "/auth/realms/ipification/protocol/openid-connect/userinfo",


  //for IM only
  REGISTER_DEVICE_TOKEN_URL:
    "https://cases.ipification.com/merchant-service/register-device",
  CURRENT_DEVICE_TOKEN: "",
  CURRENT_STATE: "",
};

