
const HOST = "https://stage.ipification.com";



export default Constants ={
  
  CLIENT_ID : "EDIT_HERE", 
  REDIRECT_URI: "EDIT_HERE",
  CLIENT_SECRET: "EDIT_HERE",
  
  // CHECK_COVERAGE_URL :
  // HOST + "/auth/realms/ipification/coverage",
  // AUTH_URL :
  // HOST + "/auth/realms/ipification/protocol/openid-connect/auth",
  TOKEN_URL:
  HOST + "/auth/realms/ipification/protocol/openid-connect/token",
  GET_USER_INFO_URL:
  HOST+ "/auth/realms/ipification/protocol/openid-connect/userinfo",

  REGISTER_DEVICE_TOKEN_URL:
    "https://cases.ipification.com/merchant-service/register-device",


  CURRENT_DEVICE_TOKEN: "",
  CURRENT_STATE: "",
};

