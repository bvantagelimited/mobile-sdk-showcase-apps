using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DemoApplication.Animations;
using DemoApplication.ExchangeToken;
using JWT;
using JWT.Serializers;
using Refit;
using Xamarin.Forms;

namespace DemoApplication.LoginPages
{
    public partial class LoginPage2 : ContentPage
    {
        public LoginPage2()
        {
            InitializeComponent();
        }
        protected override void OnAppearing()
        {
            base.OnAppearing();
            Task.Run(async () =>
            {
                await ViewAnimations.FadeAnimY(MainStack);

            });
        }
        protected void Back(object s, EventArgs e)
        {
            Navigation.PopAsync();
        }
        protected void Login(object s, EventArgs e)
        {
            CoverageResultLbl.Text = "";
            AuthResultLbl.Text = "";
            PhoneVerifiedLbl.Text = "";
            SubLbl.Text = "";
            CheckCoverage();

        }

        private async void CheckCoverage()
        {
            var coverageResult = await DependencyService.Get<IIPService>().CheckCoverage();
            CoverageResultLbl.Text = "Coverage Result: " + coverageResult.IsAvailable;
            if (coverageResult.IsAvailable)
            {
                
                DoAuth();
            }
            else
            {
                CoverageResultLbl.Text = "Coverage Result: " + coverageResult.IsAvailable + " - errormessage: " + coverageResult.ErrorMessage + " - errorcode: " + coverageResult.ErrorCode;
            }

        }
        private async void DoAuth()
        {
            var loginHint = PhoneInput.Text;
            var authorizationResult = await DependencyService.Get<IIPService>().DoAuthorization(loginHint);

            if (authorizationResult.IsSuccess)
            {
                AuthResultLbl.Text = "Authorization Result: " + authorizationResult.Code;
                DoExchangeToken(authorizationResult.Code);

            }
            else
            {
                AuthResultLbl.Text = "Authorization Result: " + authorizationResult.Code + " - errormessage: " + authorizationResult.ErrorMessage + " - errorcode: " + authorizationResult.ErrorCode;
            }

        }
        private async void DoExchangeToken(string code)
        {
            try
            {
                var nsAPI = RestService.For<IExchangeTokenApi>("https://stage.ipification.com");
                var request = new Dictionary<string, string>
            {
                { "client_id" , IPConstants.ClientId },
                { "grant_type", "authorization_code" },
                { "client_secret" , "your_client_secret" },
                { "redirect_uri" , IPConstants.RedirectUri },
                { "code", code }
            };
                var req = await nsAPI.PostToken(request);

                if (req.IsSuccessStatusCode)
                {
                    var token = req.Content.access_token;
                    var serializer = new JsonNetSerializer();
                    var urlEncoder = new JwtBase64UrlEncoder();
                    var decoder = new JwtDecoder(serializer, urlEncoder);
                    var payload = decoder.DecodeToObject<IDictionary<string, string>>(token);

                    if (payload.ContainsKey("phone_number_verified"))
                    {
                        var exp = payload["phone_number_verified"];
                        PhoneVerifiedLbl.Text = "phone_number_verified: " + exp;

                    }
                    else
                    {
                        PhoneVerifiedLbl.Text = "phone_number_verified: false";
                    }
                    var sub = payload["sub"];
                    SubLbl.Text = "sub: " + sub;
                    var mobileID = payload["mobile_id"];
                    MobileIDLbl.Text = "mobileID: " + mobileID;
                }
                else
                {
                    PhoneVerifiedLbl.Text = "Token error: " + req.Error.Content;
                }
            } catch (ApiException ex)
            {
                PhoneVerifiedLbl.Text = ex.Message;
            } catch (Exception ex)
            {
                PhoneVerifiedLbl.Text = ex.Message;
            }
        }
        public void Dispose()
        {
            
            Dispose(true);
        }
        protected virtual void Dispose(bool disposing)
        {
            DependencyService.Get<IIPService>().Dispose();
        }
    }


}
