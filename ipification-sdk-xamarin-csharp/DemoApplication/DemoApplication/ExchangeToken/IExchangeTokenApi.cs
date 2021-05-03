using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Refit;

namespace DemoApplication.ExchangeToken
{
    public interface IExchangeTokenApi
    {

        [Post("/auth/realms/ipification/protocol/openid-connect/token")]
        Task<ApiResponse<TokenObj>> PostToken([Body(BodySerializationMethod.UrlEncoded)] Dictionary<string, string> request);
    }

    public class TokenObj
    {
        [JsonProperty("access_token")]
        public string access_token { get; set; }
    }
    
}


