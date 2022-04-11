export default {
  CLIENT_SECRET: '4bc14abb-fd00-4fd7-b274-88205f2f11cb',
  TOKEN_URL: 'https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token',
  GET_USER_INFO_URL: 'https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo',
  REGISTER_DEVICE_TOKEN_URL: 'https://cases.ipification.com/merchant-service/register-device',
  CURRENT_STATE: '',
  getRandomStateValues : () => {
    const validChars =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    var result = "";
    for (var i = 0; i < 80; i++) {
      result += validChars.charAt(
        Math.floor(Math.random() * validChars.length)
      );
    }
    return result;
  }
};