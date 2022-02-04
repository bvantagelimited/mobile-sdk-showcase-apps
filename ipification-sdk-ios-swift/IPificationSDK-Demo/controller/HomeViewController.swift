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
    var logText = ""
    
    var errorMessage : String?
    
    var tokenInfo : TokenInfo?
    

    @IBOutlet weak var imVerifyBtn: UIButton!
    @IBOutlet weak var phoneVerifyBtn: UIButton!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpView()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        print("viewWillAppear")
        //FCM push token
        Messaging.messaging().token { token, error in
          if let error = error {
            print("Error fetching FCM registration token: \(error)")
            IPConfiguration.sharedInstance.log += "Error fetching FCM registration token: \(error) \n"
          } else if let token = token {
              print("FCM registration token: \(token)")
              IPConfiguration.sharedInstance.log += "FCM registration token: \(token) \n"
              APIManager.sharedInstance.deviceToken = token
              APIManager.sharedInstance.initStateAndRegister()
              
          }
        }
    }
    
    private func setUpView(){
        navigationItem.title = "IPification"
        imVerifyBtn.layer.cornerRadius = 5
        phoneVerifyBtn.layer.cornerRadius = 5
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
        showLoadingViewAutoEnd()
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "H:mm:ss.SSSS"
        let time = df.string(from: d)
      
        logText += "1. start authorization at time \(time) with state: \(APIManager.sharedInstance.state)\n"
        printLog();


        let authorizationService =  AuthorizationService()
//        authorizationService.debug = true
        
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed \(error.localizedDescription)")
            self.logText += "[Auth Request] Result - \(time): Error: \(error.localizedDescription)! \n"
            self.printLog();
            self.hideLoadingView()
            self.doErrorPage(error.localizedDescription, tokenInfo: nil)
        }
        
        authorizationService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.getPlainResponse() )
            
            DispatchQueue.main.async {
                self.hideLoadingView()
                self.callExchangeToken(code: response.getCode()!)
            }
            
        }
        IPificationLocale.sharedInstance.updateScreen(titleBar:"IPification", title:"Phone Number Verify",  description:"Please tap on the preferred messaging app then follow our instruction on the screen", whatsappBtnText:"Quick Login via Whatsapp", viberBtnText : "Quick Login via Viber", telegramBtnText : "Quick Login via Telegram", doneBtnText:"Done")
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setState(value: APIManager.sharedInstance.state)
        authorizationRequest.setScope(value: "openid ip:phone")
        authorizationRequest.addQueryParam(key: "channel", value: "wa viber telegram")
        
        let authRequest = authorizationRequest.build()
      
        authorizationService.startIMAuthorization(viewController: self, authRequest)

        
        
    }
    
    private func printLog(){
        IPConfiguration.sharedInstance.log += logText
        logText = ""
    }
}


extension HomeViewController{
    
    private func callExchangeToken(code: String) {
        print("callExchangeToken")
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        
        self.logText += "2. Do exchange Token with code: \(code)\n"
        self.printLog();
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
               print(error)
               self.logText += "\n" + "error exchange token \(error)" + "\n"
               self.printLog()
               self.hideLoadingView()
               self.doErrorPage(error, tokenInfo: nil)
           }
           
       }
        
    }
    
    private func doErrorPage(_ errorMessage: String?, tokenInfo : TokenInfo?){
        self.tokenInfo = tokenInfo
        print(errorMessage ?? "")
        self.errorMessage = errorMessage
        logText += "[show error page] failed \(errorMessage) \n"
        printLog()
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openFailPage", sender: nil)
        }
    }
    
    private func doSuccessPage(_ tokenInfo: TokenInfo){
        self.tokenInfo = tokenInfo
        DispatchQueue.main.async {
            self.hideLoadingView()
        }
        
        if(tokenInfo.isVerifedPhone == true || tokenInfo.phoneNumber != nil){
            logText += "[show success page] Success with phoneNumber \(tokenInfo.phoneNumber) isVerifedPhone: \(tokenInfo.isVerifedPhone) \n"
            printLog()
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openSuccessPage", sender: nil)
            }
        }else{
            logText += "[show success page] failed \(tokenInfo.phoneNumber) \n"
            printLog()
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openFailPage", sender: nil)
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
}
