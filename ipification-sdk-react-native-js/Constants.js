const ENV = {
  SANDBOX: 'sandbox',
  PRODUCTION: 'production'
};

export default Constants ={
  ENV: ENV.SANDBOX, //supports "sandbox" or "production"
  CLIENT_ID : "CORRECT_HERE", 
  REDIRECT_URI: "CORRECT_HERE",
  CLIENT_SECRET: "CORRECT_HERE", // for demo purpose only
  
  TOKEN_EXCHANGE_HOST : "https://stage.ipification.com",
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

