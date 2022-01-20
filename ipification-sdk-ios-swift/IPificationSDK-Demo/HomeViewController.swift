//
//  HomeViewController.swift
//  IPificationSDK-Demo
//
//  Created by Nguyen Huu Tinh on 16/12/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation
import UIKit
import IPificationSDK
import JWTDecode
class HomeViewController : BaseViewController{
    
    var code : String?
    var errorMessage : String?
    var mobileID: String?
    var sub : String?
    var isVerifedPhone: Bool = false
    var phoneNumber: String?
    var logText = ""
    @IBOutlet weak var imVerifyBtn: UIButton!
    
    @IBOutlet weak var phoneVerifyBtn: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setUpView()
    }
    
    func setUpView(){
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
    
    func startIMAuthorization() {
        showLoadingViewAutoEnd()
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "H:mm:ss.SSSS"
        let time = df.string(from: d)
      
        logText += "\n 2. start authorization at time \(time)\n"
        printLog();


        let authorizationService =  AuthorizationService()
        authorizationService.debug = true
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed \(error.localizedDescription)")
            self.logText += "\nAuth Result - \(time): Error: \(error.localizedDescription)! \n"
            self.printLog();
            self.hideLoadingView()
            self.doErrorPage(error.localizedDescription)
        }
        authorizationService.callbackLog = { (response) -> Void in
            print("authlog", response)
        }
        authorizationService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.getPlainResponse() )
            
            DispatchQueue.main.async {
//                    let d = Date()
//                    let df = DateFormatter()
//                    df.dateFormat = "H:mm:ss.SSSS"
//                    let time = df.string(from: d)
                self.hideLoadingView()
                self.callExchangeToken(code: response.getCode()!)
            }
            
        }
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setState(value: NetworkManager.sharedInstance.state)
        authorizationRequest.setScope(value: "openid ip:phone")
        authorizationRequest.addQueryParam(key: "channel", value: "wa viber telegram")
        
        let authRequest = authorizationRequest.build()
        authorizationService.locale = IPificationLocale(title:"Phone Number Verify", topTitle:"IPification", description:"Please tap on the preferred messaging app then follow our instruction on the screen", whatsappBtnText:"Quick Login via Whatsapp", viberBtnText : "Quick Login via Viber", telegramBtnText : "Quick Login via Telegram", doneBtnText:"Done")
        authorizationService.startIMAuthorization(viewController: self, authRequest)

        
        
    }
    
    func printLog(){
        
    }
}


extension HomeViewController{
  
    func callCheckState(state: String){
        NetworkManager.sharedInstance.checkState(state: state, success: { (data) in
            do {
                print("hello")
                let json = try JSONSerialization.jsonObject(with: data) as! Dictionary<String, AnyObject>
                self.doSuccessPage(json["access_token"] as? String)
            } catch {
                print("error")
                self.doErrorPage(error.localizedDescription)
            }
        }) {(error) in
            DispatchQueue.main.async {
                self.logText += "\n" + "error exchange token \(error)" + "\n"
                self.printLog()
                self.hideLoadingView()
                self.doErrorPage(error)
            }
        }
    }
    
    func callExchangeToken(code: String) {
        print("callExchangeToken")
        DispatchQueue.main.async {
            self.showLoadingView()
        }
        
        self.logText += "\n3. Do exchange Token with Code \(code)\n"
        self.printLog();
        NetworkManager.sharedInstance.callTokenExchange(code: code, success: { (data) in
            do {
                let json = try JSONSerialization.jsonObject(with: data) as! Dictionary<String, AnyObject>
                print(json)
                self.doSuccessPage(json["access_token"] as? String)
            } catch {
                print("error")
            }
           
           
       }) { (error) in
           DispatchQueue.main.async {
               print(error)
               self.logText += "\n" + "error exchange token \(error)" + "\n"
               self.printLog()
               self.hideLoadingView()
               self.doErrorPage(error)
           }
           
       }
        
    }
    
    func doErrorPage(_ errorMessage: String?){
        print(errorMessage)
        self.errorMessage = errorMessage
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openFailPage", sender: nil)
        }
    }
    func doSuccessPage(_ accesToken: String?){
        DispatchQueue.main.async {
            self.hideLoadingView()
        }
        if(accesToken == nil){
            DispatchQueue.main.async {
                self.logText += "\n" + "accessToken nil" + "\n"
                self.printLog()
            }
           
            return
        }
        do{
            let jwt = try decode(jwt: accesToken!)
            let phoneVerifiedClaim = jwt.claim(name: "phone_number_verified")
            if let phoneVerifiedValue = phoneVerifiedClaim.string {
                
                isVerifedPhone = phoneVerifiedValue == "true"
            }
            
            let logintHintClaim = jwt.claim(name: "phone_number")
            if let phoneNumberValue = logintHintClaim.string {
                phoneNumber = phoneNumberValue
            }
            let mobileIDClaim = jwt.claim(name: "mobile_id")
            if let mobileIDValue = mobileIDClaim.string {
                mobileID = mobileIDValue
            }
            let subClaim = jwt.claim(name: "sub")
            if let subValue = subClaim.string {
                sub = subValue
            }
//            self.logText += "\n" + "accesToken \(phoneClaim.string)" + "\n"
//            self.printLog()
            self.logText += "\n" + "accesToken: \(accesToken!)" + "\n"
            self.printLog()
            if(phoneNumber != nil){
                DispatchQueue.main.async {
                    self.performSegue(withIdentifier: "openSuccessPage", sender: nil)
                }
            }else{
                DispatchQueue.main.async {
                    self.performSegue(withIdentifier: "openFailPage", sender: nil)
                }
            }
        }catch{
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openFailPage", sender: nil)
            }
        }
        
        
        
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openSuccessPage" {
            print(isVerifedPhone)
            if let nextViewController = segue.destination as? SuccessViewController {
                nextViewController.mobileID = mobileID
                nextViewController.sub = sub
                nextViewController.isVerifedPhone = isVerifedPhone
                nextViewController.phoneNumber = phoneNumber

            }
        }
        if segue.identifier == "openFailPage" {
            if let nextViewController = segue.destination as? FailViewController {
                nextViewController.mobileID = mobileID
                nextViewController.sub = sub
                nextViewController.isVerifedPhone = isVerifedPhone
                nextViewController.phoneNumber = phoneNumber
                nextViewController.errorMessage = errorMessage

            }
        }
    }
}
