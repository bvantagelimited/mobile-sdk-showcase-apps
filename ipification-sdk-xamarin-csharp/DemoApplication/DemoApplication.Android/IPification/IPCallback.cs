using Com.Ipification.Mobile.Sdk.Android.Callback;
using Com.Ipification.Mobile.Sdk.Android.Exception;
using Com.Ipification.Mobile.Sdk.Android.Response;
namespace DemoApplication.Droid
{
    // Coverage

    public delegate void OnCoverageDidCompleteDelegate(CoverageResponse response);
    public delegate void OnCoverageDidErrorDelegate(CellularException error);

    public class IPCoverageCallback : Java.Lang.Object,  ICellularCallback
    {
        public OnCoverageDidCompleteDelegate OnCoverageDidComplete { get; set; }
        public OnCoverageDidErrorDelegate OnCoverageDidError { get; set; }


        void ICellularCallback.OnError(CellularException error)
        {
            this.OnCoverageDidError?.Invoke(error);
        }

        void ICellularCallback.OnSuccess(Java.Lang.Object response)
        {
            this.OnCoverageDidComplete?.Invoke((CoverageResponse)response);
        }
    }

    // Authorization

    public delegate void OnAuthDidCompleteDelegate(AuthResponse response);
    public delegate void OnAuthDidErrorDelegate(CellularException error);

    public class IPAuthorizationCallback : Java.Lang.Object, ICellularCallback
    {

        public OnAuthDidCompleteDelegate OnAuthDidComplete { get; set; }
        public OnAuthDidErrorDelegate OnAuthDidError { get; set; }

        public IPAuthorizationCallback()
        {
        }

        void ICellularCallback.OnError(CellularException error)
        {
            this.OnAuthDidError?.Invoke(error);
        }

        void ICellularCallback.OnSuccess(Java.Lang.Object response)
        {
            this.OnAuthDidComplete?.Invoke((AuthResponse)response);
        }

       
    }
}
