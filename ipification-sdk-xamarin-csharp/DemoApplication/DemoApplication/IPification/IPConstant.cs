using System;
namespace DemoApplication
{
    public class IPConstants
    {
#if DEBUG
        public const string CoverageServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/coverage/202.175.50.128";
        public const string AuthServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth";
        public const string RedirectUri = "https://api.dev.ipification.com/api/v1/callback";
        public const string ClientId = "9f49df46a311454d882824607136c68f";
#else
        public const string CoverageServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/coverage/202.175.50.128";
        public const string AuthServiceEndpoint = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth";
        public const string RedirectUri = "https://api.dev.ipification.com/api/v1/callback";
        public const string ClientId = "9f49df46a311454d882824607136c68f";
#endif
    }
}