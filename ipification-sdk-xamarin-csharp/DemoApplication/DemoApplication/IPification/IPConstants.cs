using System;
namespace DemoApplication
{
    public class IPConstants
    {
        #if DEBUG
            public const string CoverageServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/coverage/202.175.50.128";
            public const string AuthServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth";
            public const string RedirectUri = "your_debug_redirect_uri";
            public const string ClientId = "your_debug_client_id";
        #else
            public const string CoverageServiceEndpoint = "https://api.ipification.com/auth/realms/ipification/coverage";
            public const string AuthServiceEndpoint = "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/auth";
            public const string RedirectUri = "your_prod_redirect_uri";
            public const string ClientId = "your_prod_client_id";
        #endif
    }
}