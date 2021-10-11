using System;
namespace DemoApplication
{
    public class CoverageResult
    {
        public bool IsAvailable { get; set; }
        public string OperatorCode { get; set; }

        public string ErrorMessage { get; set; }
        public string ErrorCode { get; set; }
    }
    public class AuthorizationResult
    {
        public bool IsSuccess { get; set; }
        public string Code { get; set; }
        public string State { get; set; }
        public string FullResponse { get; set; }

        public string ErrorCode { get; set; }
        public string ErrorMessage { get; set; }
    }
}
