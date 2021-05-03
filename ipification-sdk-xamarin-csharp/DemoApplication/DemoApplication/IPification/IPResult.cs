using System;
namespace DemoApplication
{
    public class CoverageResult
    {
        public bool IsAvailable { get; set; }
        public string ErrorMessage { get; set; }
        public string ErrorCode { get; set; }
    }
    public class AuthorizationResult
    { 
        public string ErrorCode { get; set; }
        public bool IsSuccess { get; set; }
        public string ErrorMessage { get; set; }
        public string Code { get; set; }
    }
}
