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
        public Task<CoverageResult> CheckCoverage()
        {
            

            var tcs = new TaskCompletionSource<CoverageResult>();

            var coverageService = new IPificationSDK.CoverageService();
            var coverageCallback = new IPCoverageCallback
            {
                OnCoverageDidComplete = (response) =>
                {
                    System.Console.WriteLine("OnCoverageDidComplete");
                    System.Console.WriteLine(response.IsAvailable);
                    var CvResult = new CoverageResult
                    {
                        IsAvailable = response.IsAvailable
                    };
                    tcs.SetResult(CvResult);
                },
                OnCoverageDidError = (error) =>
                {
                    System.Console.WriteLine("OnCoverageDidError");
                    System.Console.WriteLine(error.ErrorMessage);
                    var CvResult = new CoverageResult
                    {
                        IsAvailable = false,
                        ErrorMessage = error.ErrorMessage
                    };

                    tcs.SetResult(CvResult);
                }
            };
            coverageService.RegisterCallbackWithCoverageCallback(coverageCallback);
            coverageService.CheckCoverage();

            return tcs.Task;
            

        }

        public void Dispose()
        {
            // do nothing
        }

        public void InitIPConfiguration()
        {
            IPConfiguration.Shared.CoverageServiceEndpoint = IPConstants.CoverageServiceEndpoint;
            IPConfiguration.Shared.AuthServiceEndpoint = IPConstants.AuthServiceEndpoint;
            IPConfiguration.Shared.ClientId = IPConstants.ClientId;
            IPConfiguration.Shared.RedirectUri = IPConstants.RedirectUri;
        }
        
        Task<AuthorizationResult> IIPService.DoAuthorization(string login_hint)
        {
            var tcs = new TaskCompletionSource<AuthorizationResult>();
            var authorizationService = new IPificationSDK.AuthorizationService();
            var authCallback = new IPAuthorizationCallback
            {
                OnAuthDidComplete = (response) =>
                {
                    System.Console.WriteLine("OnAuthDidComplete");
                    System.Console.WriteLine(response.Code);
                    var AuthResult = new AuthorizationResult();
                    if (response.Code != null)
                    {
                        AuthResult.IsSuccess = true;
                    }
                    else
                    {
                        AuthResult.IsSuccess = false;
                    }
                    AuthResult.Code = response.Code;
                    tcs.SetResult(AuthResult);
                },
                OnAuthDidError = (error) =>
                {
                    System.Console.WriteLine("OnAuthDidError");
                    var AuthResult = new AuthorizationResult
                    {
                        IsSuccess = false,
                        ErrorMessage = error.ErrorMessage
                    };
                    tcs.SetResult(AuthResult);
                }
            };
            authorizationService.RegisterCallbackWithAuthCallback(authCallback);
            var authRe = new IPificationSDK.Builder();
            authRe.AddQueryParamWithKey("login_hint", login_hint);
            var req = authRe.Build;

            authorizationService.DoAuthorization(req);

            return tcs.Task;
        }
    }

}