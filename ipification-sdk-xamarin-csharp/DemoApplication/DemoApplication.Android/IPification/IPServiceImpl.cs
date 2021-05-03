using System.Threading.Tasks;
using Android.Content;
using Android.Util;
using Com.Ipification.Mobile.Sdk.Android;
using Com.Ipification.Mobile.Sdk.Android.Request;
using Xamarin.Forms;


[assembly: Dependency(typeof(DemoApplication.Droid.IPServiceImpl))]
namespace DemoApplication.Droid
{

    public class IPServiceImpl : IIPService
    {
        public IPServiceImpl()
        {
            InitConfiguration();
        }
        readonly Context context = Android.App.Application.Context;


        public Task<CoverageResult> CheckCoverage()
        {
            var tcs = new TaskCompletionSource<CoverageResult>();
            
            var service = new CellularService(context);
            var coverageCallback = new IPCoverageCallback
            {
                OnCoverageDidComplete = (response) =>
                {
                    //System.Console.WriteLine("OnCoverageDidComplete");
                    Log.Info("OnCoverageDidComplete", "" + response.IsAvailable);
                    //System.Console.WriteLine(response.IsAvailable);
                    var CvResult = new CoverageResult
                    {
                        IsAvailable = response.IsAvailable
                    };
                    tcs.SetResult(CvResult);

                },
                OnCoverageDidError = (error) =>
                {
                    Log.Info("OnCoverageDidError", error.ErrorMessage);
                    var CvResult = new CoverageResult
                    {
                        IsAvailable = false,
                        ErrorMessage = error.ErrorMessage,
                        ErrorCode = error.Error_code
                    };
                    tcs.SetResult(CvResult);
                }
            };

            service.CheckCoverage(coverageCallback);
            
            return tcs.Task;
        }

        private void InitConfiguration()
        {
            IPConfiguration.Instance.CoverageEndpoint = Android.Net.Uri.Parse(IPConstants.CoverageServiceEndpoint);
            IPConfiguration.Instance.AuthorizationEndpoint = Android.Net.Uri.Parse(IPConstants.AuthServiceEndpoint);
            IPConfiguration.Instance.ClientId = IPConstants.ClientId;
            IPConfiguration.Instance.RedirectUri = Android.Net.Uri.Parse(IPConstants.RedirectUri);
        }

        Task<AuthorizationResult> IIPService.DoAuthorization(string login_hint)
        {
            var tcs = new TaskCompletionSource<AuthorizationResult>();

            var service = new CellularService(context);
            var authCallback = new IPAuthorizationCallback
            {
                OnAuthDidComplete = (response) =>
                {

                    Log.Info("OnAuthDidComplete", "OnAuthDidComplete");
                    Log.Info("OnAuthDidComplete", "code: " + response.Code);
                    //System.Console.WriteLine(response.Code);
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
                    //System.Console.WriteLine("OnAuthDidError");
                    Log.Info("OnAuthDidError", "OnAuthDidError " + error.Error_code);
                    var AuthResult = new AuthorizationResult
                    {
                        IsSuccess = false,
                        ErrorMessage = error.ErrorMessage,
                        ErrorCode = error.Error_code
                    };
                    tcs.SetResult(AuthResult);
                }
            };
            var authRequestBuilder = new AuthRequest.Builder();
            authRequestBuilder.AddQueryParam("login_hint", login_hint);
            
            var auth = authRequestBuilder.Build();
            service.PerformAuth(auth, authCallback);

            return tcs.Task;
        }

        public void Dispose()
        {
            CellularService.Instance.UnregisterNetwork(context);
        }
    }

}
