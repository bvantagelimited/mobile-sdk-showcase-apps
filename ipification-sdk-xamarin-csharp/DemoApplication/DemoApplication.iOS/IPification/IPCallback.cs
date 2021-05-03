using System;
using IPificationSDK;
namespace DemoApplication.iOS
{

    // Check Coverage

    public delegate void OnCoverageDidCompleteDelegate(CoverageResponse response);
    public delegate void OnCoverageDidErrorDelegate(CellularException error);

    public class IPCoverageCallback : Foundation.NSObject, ICoverageCallback
    {
       
        public OnCoverageDidCompleteDelegate OnCoverageDidComplete { get; set; }
        public OnCoverageDidErrorDelegate OnCoverageDidError { get; set; }

        public void OnErrorWithError(CellularException error)
        {
            this.OnCoverageDidError?.Invoke(error);
        }

        public void OnSuccessWithResponse(CoverageResponse response)
        {
            this.OnCoverageDidComplete?.Invoke(response);
        }
    }

    // Authentication

    public delegate void OnAuthDidCompleteDelegate(AuthorizationResponse response);
    public delegate void OnAuthDidErrorDelegate(CellularException error);

    public class IPAuthorizationCallback : Foundation.NSObject, IAuthCallback
    {

        public OnAuthDidCompleteDelegate OnAuthDidComplete { get; set; }
        public OnAuthDidErrorDelegate OnAuthDidError { get; set; }

        public void OnErrorWithError(CellularException error)
        {
            this.OnAuthDidError?.Invoke(error);
        }


        public void OnSuccessWithResponse(AuthorizationResponse response)
        {
            this.OnAuthDidComplete?.Invoke(response);
        }
    }
}
