//
//  ViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 3/6/2020.
//  Copyright © 2020 IPification. All rights reserved.
//

import UIKit
import IPificationSDK


class PhoneVerifyViewController: BaseViewController {
    
    
    @IBOutlet weak var verifyBtn: UIButton!
    
    @IBOutlet weak var lblAuthorizationResult: UILabel!
    @IBOutlet weak var lblCoverageResult: UILabel!
    
    @IBOutlet weak var phoneInputTextField: UITextField!
    @IBOutlet weak var privacyPolicisLbl: UILabel!
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var logTextLbl: UITextView!
    

    
    var successResponse: String? = ""
    var errorResponse: String? = ""

    
    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 13.0, *) {
            phoneInputTextField.overrideUserInterfaceStyle = .light
        } else {
            // Fallback on earlier versions
        }
        verifyBtn.layer.cornerRadius = 5
    }

    
    @IBAction func doAuthentication(_ sender: Any) {
        self.phoneInputTextField.endEditing(true)
        checkCoverageAPI()
    }
    
    
    
    func checkCoverageAPI() {
        
        let coverageService = CoverageService()
        coverageService.callbackFailed = { (error) -> Void in
            print("CoverageService callbackFailed ", error.localizedDescription)
        }

        coverageService.callbackSuccess = { (response) -> Void in
            if(response.isAvailable()){
                // call Authorization API
                self.doIPAuthenticationAPI()
            } else{
                // TODO: TELCO is not supported
            }
            print("CoverageService ", response.isAvailable(), response.getOperatorCode() ?? "" )
        }
        coverageService.checkCoverage()
    }
    
    // call IPification Authorization API Only (Not enable IM Auth)
    func doIPAuthenticationAPI(){
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        let phone = phoneInputTextField.text!
        if(phone == ""){
            print("phone number error : \(phone)! \n")
            return
        }
        
        self.phoneInputTextField.endEditing(true)
        
        let authorizationService =  AuthorizationService()
        
        authorizationService.callbackSuccess = { (response) -> Void in
            DispatchQueue.main.async {
                self.hideLoadingView()
            }
            
            if(response.getCode() != nil){
                print("callbackSuccess with code: ", response.getCode()!)
                self.callExchangeToken(code: response.getCode()!)
            }else{
                print("auth failed", response.getPlainResponse())
            }
            
        }
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed", error.localizedDescription)
            DispatchQueue.main.async {
                self.hideLoadingView()
            }
        }
        
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setScope(value: "openid ip:phone_verify")
        authorizationRequest.addQueryParam(key: "login_hint", value: phoneInputTextField.text!)

        let authRequest = authorizationRequest.build()
        authorizationService.startAuthorization(authRequest)

    }
    
    // call IPification Authorization API with IM Flow
    func doIPIMAuthorization() {
        IPConfiguration.sharedInstance.debug = true
        var phone = phoneInputTextField.text!
        if(phone == ""){
            print("phone number error : \(phone)!")
            return
        }
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        phone = phone.replacingOccurrences(of: "+", with: "")
        self.phoneInputTextField.endEditing(true)
       
        
        let authorizationService =  AuthorizationService()
        
        
        authorizationService.callbackSuccess = { (response) -> Void in
            DispatchQueue.main.async {
                self.hideLoadingView()
            }
            if(response.getCode() != nil){
                print("callbackSuccess with code: ", response.getCode()!)
                self.callExchangeToken(code: response.getCode()!)
            }else{
                print("auth failed", response.getPlainResponse())
            }
            
        }
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed ", error.localizedDescription)
            DispatchQueue.main.async {
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
    
    // TODO: do this at your backend side
    private func callExchangeToken(code: String){
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        APIManager.sharedInstance.callTokenExchange(code: code, success: { (data) in
            do {
                let response = String(decoding: data, as: UTF8.self)
                
                let json = try JSONSerialization.jsonObject(with: data) as! Dictionary<String, AnyObject>
                let phoneNumberVerified = json["phone_number_verified"] as? String
                let phoneNumber = json["phone_number"] as? String
                
                if(phoneNumberVerified != nil && phoneNumberVerified != "false" || phoneNumber != nil){
                    self.doSuccessPage(response)
                }else{
                    self.doErrorPage(response)
                }
                DispatchQueue.main.async {
                    self.hideLoadingView()
                }
            } catch {
                print("error")
                self.doErrorPage(error.localizedDescription)
                DispatchQueue.main.async {
                    self.hideLoadingView()
                }
            }
           
           
       }) { (error) in
           DispatchQueue.main.async {
               self.hideLoadingView()
               self.doErrorPage(error)
           }
           
       }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openSuccessPage" {
            if let nextViewController = segue.destination as? SuccessViewController {
                nextViewController.responseData = successResponse!
            }
        }
        if segue.identifier == "openFailPage" {
            if let nextViewController = segue.destination as? FailViewController {
                nextViewController.responseData = errorResponse!
            }
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


extension PhoneVerifyViewController{
    private func doErrorPage(_ errorMessage: String?){
        self.errorResponse = errorMessage
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openFailPage", sender: nil)
        }
    }
    
    private func doSuccessPage(_ response: String){
        self.successResponse = response
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openSuccessPage", sender: nil)
        }
    }
    
}
