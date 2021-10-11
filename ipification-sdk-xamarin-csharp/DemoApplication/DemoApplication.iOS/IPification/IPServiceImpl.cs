using System.Threading.Tasks;
using IPificationSDK;
using Xamarin.Forms;


[assembly: Dependency(typeof(DemoApplication.iOS.IPServiceImpl))]
namespace DemoApplication.iOS
{

    public class IPServiceImpl : IIPService
    {
        public IPServiceImpl()
        {
            InitIPConfiguration();
        }
        public void InitIPConfiguration()
        {
            IPConfiguration.Shared.CoverageServiceEndpoint = IPConstants.CoverageServiceEndpoint;
            IPConfiguration.Shared.AuthServiceEndpoint = IPConstants.AuthServiceEndpoint;
            IPConfiguration.Shared.ClientId = IPConstants.ClientId;
            IPConfiguration.Shared.RedirectUri = IPConstants.RedirectUri;
        }
        // 1. Check Coverage
        public Task<CoverageResult> CheckCoverage()
        {
            var tcs = new TaskCompletionSource<CoverageResult>();
            var coverageService = new IPificationSDK.CoverageService();
            coverageService.CallbackSuccess = (response) =>
            {
                System.Console.WriteLine("OnCoverageDidCompleted");
                System.Console.WriteLine(response.IsAvailable);
                var CvResult = new CoverageResult
                {
                    IsAvailable = response.IsAvailable,
                    OperatorCode = response.OperatorCode

                };
                tcs.SetResult(CvResult);
            };
            coverageService.CallbackFailed = (error) =>
            {
                System.Console.WriteLine("OnCoverageDidError");
                System.Console.WriteLine(error.ErrorMessage);
                var CvResult = new CoverageResult
                {
                    IsAvailable = false,
                    ErrorMessage = error.ErrorMessage
                };

                tcs.SetResult(CvResult);
            };

            coverageService.CheckCoverage();
            return tcs.Task;
        }

        // 2. Do Authorization with phone number
        Task<AuthorizationResult> IIPService.DoAuthorization(string login_hint)
        {
            var tcs = new TaskCompletionSource<AuthorizationResult>();
            var authorizationService = new IPificationSDK.AuthorizationService();
            authorizationService.CallbackSuccess = (response) =>
            {
                System.Console.WriteLine("OnAuthDidCompleted");
                System.Console.WriteLine(response.Code);
                System.Console.WriteLine(response.State);
                System.Console.WriteLine(response.PlainResponse);
                var AuthResult = new AuthorizationResult();
                if (response.Code != null)
                {
                    AuthResult.IsSuccess = true;
                    AuthResult.Code = response.Code;
                    AuthResult.State = response.State;
                    AuthResult.FullResponse = response.PlainResponse;
                }
                else
                {
                    AuthResult.IsSuccess = false;
                }

                tcs.SetResult(AuthResult);
            };
            authorizationService.CallbackFailed = (error) =>
            {
                System.Console.WriteLine("OnAuthDidError");
                var AuthResult = new AuthorizationResult
                {
                    IsSuccess = false,
                    ErrorMessage = error.ErrorMessage
                };
                tcs.SetResult(AuthResult);
            };
            var authRe = new IPificationSDK.Builder();
            authRe.AddQueryParamWithKey("login_hint", login_hint);
            authRe.SetScopeWithValue("openid ip:phone_verify ip:mobile_id");
            // authRe.SetStateWithValue("abcd1234abcd1234");

            var req = authRe.Build;
            authorizationService.DoAuthorization(req);
            return tcs.Task;
        }

        public void Dispose()
        {
            // do nothing
        }
    }

}