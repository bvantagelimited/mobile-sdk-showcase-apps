//
//  HomeViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 16/12/2021.
//  Copyright © 2021 IPification. All rights reserved.
//

import Foundation
import UIKit
import IPificationSDK
import JWTDecode
import FirebaseMessaging
class HomeViewController : BaseViewController{
    
    @IBOutlet weak var imVerifyBtn: UIButton!
    @IBOutlet weak var phoneVerifyBtn: UIButton!
    
    var successResponse: String? = ""
    var errorResponse: String? = ""
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpView()
        
        if(Constants.enableFCM){
            //FCM push token
            Messaging.messaging().token { token, error in
              if let error = error {
                print("Error fetching FCM registration token: \(error)")
                IPConfiguration.sharedInstance.log += "Error fetching FCM registration token: \(error) \n"
              } else if let token = token {
                  print("FCM registration token: \(token)")
                  IPConfiguration.sharedInstance.log += "FCM registration token: \(token) \n"
                  APIManager.sharedInstance.deviceToken = token
                  
              }
            }
        }
        
        initIPification()
    }
    
    private func setUpView(){
        navigationItem.title = "IPification Demo App"
        imVerifyBtn.layer.cornerRadius = 5
        phoneVerifyBtn.layer.cornerRadius = 5
    }
    
    
    func initIPification(){
        IPConfiguration.sharedInstance.COVERAGE_URL = Constants.CHECK_COVERAGE_URL
        IPConfiguration.sharedInstance.AUTHORIZATION_URL = Constants.AUTH_URL
        IPConfiguration.sharedInstance.CLIENT_ID = Constants.CLIENT_ID
        IPConfiguration.sharedInstance.REDIRECT_URI = Constants.REDIRECT_URI
//        updateThemeAndLocale()
    }
    
    @IBAction func doIPificationVerify(_ sender: Any) {
        showLoadingViewAutoEnd()
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "showPhoneNumberVerify", sender: nil)
        }
    }
    
    @IBAction func doIMVerify(_ sender: Any) {
        startIMAuthorization()
    }
    
    @IBAction func doIMVerifyWithReply(_ sender: Any) {
    }
    
    private func startIMAuthorization() {
        if(Constants.enableFCM){
            APIManager.sharedInstance.initStateAndRegisterDevice()
        }
        

        IPConfiguration.sharedInstance.debug = true
        showLoadingView()
        

        let authorizationService =  AuthorizationService()
        authorizationService.callbackSuccess = { (response) -> Void in
            print("[Auth] Result - callbackSuccess", response.getPlainResponse() )
            
            DispatchQueue.main.async {
                self.hideLoadingView()
                self.callExchangeToken(code: response.getCode()!)
            }
            
        }
        authorizationService.callbackFailed = { (error) -> Void in
            print("[Auth] Result - callbackFailed \(error.localizedDescription)")
            
            DispatchQueue.main.async {
                self.hideLoadingView()
                self.doErrorPage(error.localizedDescription)
            }
            
        }
        authorizationService.callbackIMCanceled = { () -> Void in
            DispatchQueue.main.async {
                self.hideLoadingView()
            }
        }

        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setState(value: APIManager.sharedInstance.state)
        authorizationRequest.setScope(value: "openid ip:phone")
        authorizationRequest.addQueryParam(key: "channel", value: "wa viber telegram")
        
        let authRequest = authorizationRequest.build()
        authorizationService.startIMAuthorization(viewController: self, authRequest)

    }
    
   
    
    private func updateThemeAndLocale(){
        IPificationLocale.sharedInstance.updateScreen(titleBar:"IPification", title:"Phone Number Verify",  description:"Please tap on the preferred messaging app then follow our instruction on the screen", whatsappBtnText:"Quick Login via Whatsapp", viberBtnText : "Quick Login via Viber", telegramBtnText : "Quick Login via Telegram", cancelBtnText:"Done")
        IPificationTheme.sharedInstance.updateScreen(toolbarTitleColor: UIColor.black, cancelBtnColor: UIColor.systemBlue, titleColor: UIColor.black, descColor: UIColor.black, backgroundColor: UIColor.white)
                
    }
}


extension HomeViewController{
    
    private func callExchangeToken(code: String) {
        print("callExchangeToken")
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        
        APIManager.sharedInstance.callTokenExchange(code: code, success: { (data) in
            DispatchQueue.main.async {
                self.hideLoadingView()
            }
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
                
            } catch {
                print("error")
                self.doErrorPage(error.localizedDescription)
                DispatchQueue.main.async {
                    self.hideLoadingView()
                }
            }
           
           
       }) { (error) in
           DispatchQueue.main.async {
               print(error)
               self.hideLoadingView()
               self.doErrorPage(error)
           }
           
       }
        
    }
    
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
}
