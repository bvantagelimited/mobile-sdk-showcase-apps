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
        readonly Context context = Android.App.Application.Context;
        public IPServiceImpl()
        {
            InitConfiguration();
        }

        private void InitConfiguration()
        {
            IPConfiguration.Instance.CoverageEndpoint = Android.Net.Uri.Parse(IPConstants.CoverageServiceEndpoint);
            IPConfiguration.Instance.AuthorizationEndpoint = Android.Net.Uri.Parse(IPConstants.AuthServiceEndpoint);
            IPConfiguration.Instance.ClientId = IPConstants.ClientId;
            IPConfiguration.Instance.RedirectUri = Android.Net.Uri.Parse(IPConstants.RedirectUri);
        }

        public Task<CoverageResult> CheckCoverage()
        {
            var tcs = new TaskCompletionSource<CoverageResult>();
            var service = new CellularService(context);
            var coverageCallback = new IPCoverageCallback
            {
                OnCoverageDidComplete = (response) =>
                {
                    Log.Info("IPServiceImpl", "coverage result: " + response.IsAvailable);
                    var CvResult = new CoverageResult
                    {
                        IsAvailable = response.IsAvailable,
                        OperatorCode = response.OperatorCode
                    };
                    tcs.SetResult(CvResult);

                },
                OnCoverageDidError = (error) =>
                {
                    Log.Info("IPServiceImpl", "coverage error:" + error.ErrorMessage);
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

        Task<AuthorizationResult> IIPService.DoAuthorization(string login_hint)
        {
            var tcs = new TaskCompletionSource<AuthorizationResult>();
            var service = new CellularService(context);
            var authCallback = new IPAuthorizationCallback
            {
                OnAuthDidComplete = (response) =>
                {

                    Log.Info("IPServiceImpl", "OnAuthDidComplete");
                    Log.Info("IPServiceImpl", "code: " + response.Code);
                    var AuthResult = new AuthorizationResult();
                    if (response.Code != null)
                    {
                        AuthResult.IsSuccess = true;
                        AuthResult.Code = response.Code;
                        AuthResult.State = response.State;
                        AuthResult.FullResponse = response.ResponseData;
                    }
                    else
                    {
                        AuthResult.IsSuccess = false;
                    }

                    tcs.SetResult(AuthResult);
                },
                OnAuthDidError = (error) =>
                {

                    Log.Info("IPServiceImpl", "OnAuthDidError " + error.Error_code);
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
            authRequestBuilder.SetScope("openid ip:phone_verify ip:mobile_id");
            //authRequestBuilder.SetState("your_state");
            //authRequestBuilder.AddQueryParam("your_param", "your_value");

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
