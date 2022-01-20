//
//  ViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 3/6/2020.
//  Copyright © 2020 IPification. All rights reserved.
//

import UIKit
import IPificationSDK
import JWTDecode

class IPViewController: BaseViewController {
    
    let CLIENT_SECRET = "4bc14abb-fd00-4fd7-b274-88205f2f11cb"
    
    @IBOutlet weak var verifyBtn: UIButton!
    
    @IBOutlet weak var lblAuthorizationResult: UILabel!
    @IBOutlet weak var lblCoverageResult: UILabel!
    
    @IBOutlet weak var phoneInputTextField: UITextField!
    @IBOutlet weak var privacyPolicisLbl: UILabel!
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var logTextLbl: UITextView!
    
    var logText: String  = ""
    var loginHint : String?
    var errorMessage: String?
    var mobileID: String?
    var sub : String?
    var isVerifedPhone: Bool?
    var phoneNumber: String?
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
    
    
    @IBAction func doAuthentication(_ sender: Any) {
        showLoadingViewAutoEnd()
//        callCheckIP()
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "H:mm:ss.SSS"
        let time = df.string(from: d)
//        logText = "\(IPConfiguration.sharedInstance.AuthConnectTimeout)"
        logText += "\n\n1. check Coverage at \(time)... \n"
//        print("start")
        index+=1
        printLog()
        self.phoneInputTextField.endEditing(true)
        doIPificationAuthorization()
    }
    
    
    func doIPificationAuthorization() {
        var phone = phoneInputTextField.text!
        if(phone == ""){
            logText = "\n phone number error : \(phone)! \n"
            printLog();
            return
        }
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "H:mm:ss.SSSS"
        let time = df.string(from: d)
        phone = phone.replacingOccurrences(of: "+", with: "")
        self.phoneInputTextField.endEditing(true)
        logText += "\n 2. start authorization with phone : \(phone)! at time \(time)\n"
        printLog();


        let authorizationService =  AuthorizationService()
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed", error.localizedDescription)
            self.logText += "\nAuth Result - \(time): Error: \(error.localizedDescription)! \n"
            self.printLog();
            self.hideLoadingView()
        }
        
        authorizationService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.getPlainResponse() )
            DispatchQueue.main.async {
                let d = Date()
                let df = DateFormatter()
                df.dateFormat = "H:mm:ss.SSSS"
                let time = df.string(from: d)
                if(response.getCode() != nil){
                    self.logText += "\n Auth Result - \(time): " + "code:  \(response.getCode()!) - state: \(response.getState()!)" + "\n"
                    self.callExchangeToken(code: response.getCode()!)
                    self.printLog()
                }else{
                    self.logText += "\n" + " Auth Result - \(time): getCode failed" + "\n"
                    self.printLog()
                }
                self.hideLoadingView()
            }
            
            

        }
        
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setScope(value: "openid ip:phone_verify")
        authorizationRequest.addQueryParam(key: "login_hint", value: phoneInputTextField.text!)
        authorizationRequest.addQueryParam(key: "channel", value: "ip wa viber telegram")
//        authorizationRequest.setState(value: "1234abcd")
//        authorizationRequest.addQueryParam(key: "sleep", value: "22000")
        let authRequest = authorizationRequest.build()
        authorizationService.startAuthorization(viewController: self, authRequest)

        
        
    }
    
    // TODO: do this at your backend side
    func callExchangeToken(code: String){
        DispatchQueue.main.async {
            self.showLoadingView()
            self.logText += "\n3. Do exchange Token with Code \(code)\n"
            self.printLog();
                    
        }
        NetworkManager.sharedInstance.callTokenExchange(code: code, success: { (data) in
            do {
                let json = try JSONSerialization.jsonObject(with: data) as! Dictionary<String, AnyObject>
                self.doSuccessPage(json["access_token"] as? String)
            } catch {
                print("error")
            }
           
           
       }) { (error) in
           DispatchQueue.main.async {
               self.logText += "\n" + "error exchange token \(error)" + "\n"
               self.printLog()
               self.hideLoadingView()
               self.doErrorPage(error)
           }
           
       }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openSuccessPage" {
            if let nextViewController = segue.destination as? SuccessViewController {
                nextViewController.mobileID = mobileID
                nextViewController.sub = sub
                nextViewController.isVerifedPhone = isVerifedPhone
                nextViewController.phoneNumber = phoneNumber
                nextViewController.loginHint = loginHint
            }
        }
        if segue.identifier == "openFailPage" {
            if let nextViewController = segue.destination as? FailViewController {
                nextViewController.mobileID = mobileID
                nextViewController.sub = sub
                nextViewController.isVerifedPhone = isVerifedPhone
                nextViewController.phoneNumber = phoneNumber
                nextViewController.loginHint = loginHint

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

extension String {
    var trimmingTrailingSpaces: String {
        if let range = rangeOfCharacter(from: .whitespacesAndNewlines, options: [.anchored, .backwards]) {
            return String(self[..<range.lowerBound]).trimmingTrailingSpaces
        }
        return self
    }
}

extension IPViewController{
    func doErrorPage(_ errorMessage: String?){
        self.errorMessage = errorMessage
        DispatchQueue.main.async {
            self.performSegue(withIdentifier: "openFailPage", sender: nil)
        }
    }
    func doSuccessPage(_ accesToken: String?){
        print(accesToken ?? "")
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
            let logintHintClaim = jwt.claim(name: "login_hint")
//            print(logintHintClaim.string)
            if let phoneNumberValue = logintHintClaim.string {
                loginHint = phoneNumberValue
            }
            let phoneNumClaim = jwt.claim(name: "phone_number")
            if let phoneNumValue = phoneNumClaim.string {
                phoneNumber = phoneNumValue
            }
            let phoneVerifiedClaim = jwt.claim(name: "phone_number_verified")
            if let phoneVerifiedValue = phoneVerifiedClaim.string {
                print("phoneVerifiedValue",phoneVerifiedValue)
                isVerifedPhone = phoneVerifiedValue == "true"
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
            if(isVerifedPhone == true || phoneNumber != nil){
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
    
}
