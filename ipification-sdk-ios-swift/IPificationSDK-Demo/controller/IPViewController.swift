//
//  ViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 3/6/2020.
//  Copyright © 2020 IPification. All rights reserved.
//

import UIKit
import IPificationSDK


class IPViewController: BaseViewController {
    
    
    @IBOutlet weak var verifyBtn: UIButton!
    
    @IBOutlet weak var lblAuthorizationResult: UILabel!
    @IBOutlet weak var lblCoverageResult: UILabel!
    
    @IBOutlet weak var phoneInputTextField: UITextField!
    @IBOutlet weak var privacyPolicisLbl: UILabel!
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var logTextLbl: UITextView!
    
    var logText: String  = ""

    var errorMessage: String?
    var tokenInfo: TokenInfo?
    var index = 0

    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        if #available(iOS 13.0, *) {
            phoneInputTextField.overrideUserInterfaceStyle = .light
        } else {
            // Fallback on earlier versions
        }
        verifyBtn.layer.cornerRadius = 5
    }
    
    override func viewWillAppear(_ animated: Bool) {
        APIManager.sharedInstance.initStateAndRegister()
    }
    
    @IBAction func doAuthentication(_ sender: Any) {
        showLoadingViewAutoEnd()
        self.phoneInputTextField.endEditing(true)

        doIPIMAuthorization()
//        checkCoverageAPI()
    }
    
    // call IPification Authorization API with IM Flow
    func doIPIMAuthorization() {
        IPConfiguration.sharedInstance.debug = true
        var phone = phoneInputTextField.text!
        if(phone == ""){
            logText = "\n phone number error : \(phone)! \n"
            printLog();
            return
        }
        
        phone = phone.replacingOccurrences(of: "+", with: "")
        self.phoneInputTextField.endEditing(true)
        logText += "\n 1. start authorization with phone : \(phone) \n"
        printLog();

        
        let authorizationService =  AuthorizationService()
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed", error.localizedDescription)
            self.logText += "\nAuth Result : Error: \(error.localizedDescription)! \n"
            self.printLog();
            self.hideLoadingView()
        }
        
        authorizationService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.getPlainResponse() )
            DispatchQueue.main.async {
                if(response.getCode() != nil){
                    self.logText += "\n Auth Result: " + "code:  \(response.getCode()!) - state: \(response.getState()!)" + "\n"
                    self.callExchangeToken(code: response.getCode()!)
                    self.printLog()
                }else{
                    self.logText += "\n" + " Auth Result: getCode failed" + "\n"
                    self.printLog()
                }
                self.hideLoadingView()
            }
        }
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setScope(value: "openid ip:phone_verify")
        authorizationRequest.addQueryParam(key: "login_hint", value: phoneInputTextField.text!)
        authorizationRequest.addQueryParam(key: "channel", value: "ip wa viber telegram")
        authorizationRequest.setState(value: APIManager.sharedInstance.state)

        let authRequest = authorizationRequest.build()
        authorizationService.startAuthorization(viewController: self, authRequest)

        
        
    }
    
    func checkCoverageAPI() {
        logText += "\n 1. Check Coverage \n"
        printLog();
        
        let coverageService = CoverageService()
        coverageService.callbackFailed = { (error) -> Void in
            print("CoverageService callbackFailed ", error.localizedDescription)
            self.logText += "\nCoverageService Result : Error: \(error.localizedDescription)! \n"
            self.printLog();
        }

        coverageService.callbackSuccess = { (response) -> Void in
            print("CoverageService ", response.isAvailable(), response.getOperatorCode() ?? "" )
            self.logText += "\nCoverageService Result: \(response.isAvailable()) - Code: \( response.getOperatorCode() ?? "") \n"
            self.printLog()
            if(response.isAvailable()){
                // call Authorization API
                self.doIPAuthenticationAPI()
            } else{
                // TODO: TELCO is not supported
            }
        }
        coverageService.checkCoverage()
    }
    
    // call IPification Authorization API Only (No IM Auth Flow)
    func doIPAuthenticationAPI(){
        var phone = phoneInputTextField.text!
        if(phone == ""){
            logText = "\n phone number error : \(phone)! \n"
            printLog();
            return
        }
        
        phone = phone.replacingOccurrences(of: "+", with: "")
        self.phoneInputTextField.endEditing(true)
        logText += "\n 2. Start Authorization with phone : \(phone) \n"
        printLog();

        
        let authorizationService =  AuthorizationService()
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed", error.localizedDescription)
            self.logText += "\nAuth Result : Error: \(error.localizedDescription)! \n"
            self.printLog();
            self.hideLoadingView()
        }
        
        authorizationService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.getPlainResponse() )
            DispatchQueue.main.async {
                
                if(response.getCode() != nil){
                    self.logText += "\n Auth Result: " + "code:  \(response.getCode()!) - state: \(response.getState()!)" + "\n"
                    self.callExchangeToken(code: response.getCode()!)
                    self.printLog()
                }else{
                    self.logText += "\n" + " Auth Result: getCode failed" + "\n"
                    self.printLog()
                }
                self.hideLoadingView()
            }
        }
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setScope(value: "openid ip:phone_verify")
        authorizationRequest.addQueryParam(key: "login_hint", value: phoneInputTextField.text!)

        let authRequest = authorizationRequest.build()
        authorizationService.startAuthorization(viewController: self, authRequest)

    }
    
    // TODO: do this at your backend side
    private func callExchangeToken(code: String){
        DispatchQueue.main.async {
            self.showLoadingView()
            self.logText += "\n3. Do exchange Token with Code \(code)\n"
            self.printLog();
                    
        }
        APIManager.sharedInstance.callTokenExchange(code: code, success: { (data) in
            do {
                let json = try JSONSerialization.jsonObject(with: data) as! Dictionary<String, AnyObject>
                let tokenInfo = Util.parseAccessToken(accessToken: json["access_token"] as? String)
                if(tokenInfo == nil){
                    self.doErrorPage("", tokenInfo: nil)
                }else{
                    self.doSuccessPage(tokenInfo!)
                }
            } catch {
                print("error")
            }
           
           
       }) { (error) in
           DispatchQueue.main.async {
               self.logText += "\n" + "error exchange token \(error)" + "\n"
               self.printLog()
               self.hideLoadingView()
               self.doErrorPage(error, tokenInfo: nil)
           }
           
       }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openSuccessPage" {
            if let nextViewController = segue.destination as? SuccessViewController {
                nextViewController.tokenInfo = tokenInfo!
            }
        }
        if segue.identifier == "openFailPage" {
            if let nextViewController = segue.destination as? FailViewController {
                if(tokenInfo != nil){
                    nextViewController.tokenInfo = tokenInfo!
                }
                nextViewController.errorMessage = errorMessage

            }
        }
    }
    
    
    
    func printLog(_ text: String? = nil){
        DispatchQueue.main.async {
            self.logTextLbl.text = text ?? self.logText
        }
    }
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
    
    @IBAction func shareOnlyText(_ sender: UIButton) {
        let text = logTextLbl.text
        let textShare = [ text ]
        let activityViewController = UIActivityViewController(activityItems: textShare as [Any] , applicationActivities: nil)
        activityViewController.popoverPresentationController?.sourceView = self.view
        self.present(activityViewController, animated: true, completion: nil)
    }
    
    
    @IBAction func policyView(_ sender: Any) {
        guard let url = URL(string: "https://ipification.com/legal") else { return }
        UIApplication.shared.open(url)

    }
   
}


extension IPViewController{
    func doErrorPage(_ errorMessage: String?, tokenInfo : TokenInfo?){
        self.errorMessage = errorMessage
        self.tokenInfo = tokenInfo
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openFailPage", sender: nil)
        }
    }
    func doSuccessPage(_ tokenInfo: TokenInfo){
        self.tokenInfo = tokenInfo
        DispatchQueue.main.async {
            self.hideLoadingView()
        }
       
        if(tokenInfo.isVerifedPhone == true || tokenInfo.phoneNumber != nil){
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openSuccessPage", sender: nil)
            }
        }else{
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openFailPage", sender: nil)
            }
        }
    }
    
}
