using System;
using System.Threading.Tasks;

namespace DemoApplication
{
    public interface IIPService
    {
        Task<CoverageResult> CheckCoverage();
        Task<AuthorizationResult> DoAuthorization(string login_hint);
        void Dispose();
    }
}
