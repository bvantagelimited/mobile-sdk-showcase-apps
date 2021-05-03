using System;
using System.Threading.Tasks;

namespace DemoApplication
{
    public interface IIPService
    {
        Task<CoverageResult> CheckCoverage();
        Task<AuthorizationResult> DoAuthorization(String login_hint);
        void Dispose();
    }
}
