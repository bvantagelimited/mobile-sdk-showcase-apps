class PhoneVerifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        binding.loginBtn.setOnClickListener {
            startIPificationFlow()
        }
    }

    private fun startIPificationFlow() {
        hideKeyboard()
        updateButton(isEnable = false) // make sure user cannot double click to send multi requests at the same time

        val phoneNumber = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

        IPificationServices.startCheckCoverage(
            phoneNumber,
            context = this,
            callback = object : CellularCallback<CoverageResponse> {
                override fun onError(error: CellularException) {
                    updateButton(isEnable = true)
                    handleIPError("CheckCoverage Error: " + error.getErrorMessage())
                }

                override fun onSuccess(response: CoverageResponse) {
                    if (response.isAvailable()) {
                        doIPAuth()
                    } else {
                        handleIPError("CheckCoverage return false")
                        updateButton(isEnable = true)
                    }
                }
            })

    }

    private fun doIPAuth() {
        val phoneNumber = "${binding.countryCodeEdt.text}${binding.phoneCodeEdt.text}"

        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setScope("openid ip:phone_verify")
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)

        IPificationServices.startAuthentication(
            this,
            authRequestBuilder.build(),
            object : IPificationCallback {
                override fun onSuccess(response: AuthResponse) {
                    //check auth_code
                    val code = response.getCode()
                    if (code != null) {
                        callTokenExchange(response.getCode()!!)
                        updateButton(isEnable = true)
                    } else {
                        binding.result.post {
                            updateButton(isEnable = true)
                            handleIPError(response.responseData)
                        }
                    }

                }

                override fun onError(error: IPificationError) {
                    binding.result.post {
                        updateButton(isEnable = true)
                        handleIPError(error.getErrorMessage())
                    }
                }
            })
    }

    private fun callTokenExchange(code: String) {

        APIManager.doPostToken(code, callback = object : TokenCallback {
            override fun onSuccess(response: String) {
                handleTokenExchangeSuccess(response)
            }

            override fun onError(error: String) {
                handleIPError(error)
            }
        })
    }

    private fun handleIPError(error: String) {
        // TODO start sms flow
    }
    private fun handleTokenExchangeSuccess(res: String){
        // TODO handle success auth. end IP flow
    }
}
