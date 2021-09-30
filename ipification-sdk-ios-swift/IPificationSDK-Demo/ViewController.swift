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

class ViewController: UIViewController {
    
    @IBOutlet weak var lblAuthorizationResult: UILabel!
    @IBOutlet weak var lblCoverageResult: UILabel!
    
    @IBOutlet weak var phoneInputTextField: UITextField!
    @IBOutlet weak var privacyPolicisLbl: UILabel!
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var logTextLbl: UITextView!
    
    var logText: String  = ""
    var code : String?

    var mobileID: String?
    var sub : String?
    var isVerifedPhone: String?

    var index = 0

    
    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 13.0, *) {
            phoneInputTextField.overrideUserInterfaceStyle = .light
        } else {
            // Fallback on earlier versions
        }
        // Do any additional setup after loading the view.
    }
    
    
    @IBAction func doAuthentication(_ sender: Any) {
        showLoadingView()
//        callCheckIP()
        logText = "\n\n1. check Coverage ... \n"
//        print("start")
        index+=1
        printLog()
        self.phoneInputTextField.endEditing(true)
        
        

        let coverageService = CoverageService()
        coverageService.callbackFailed = { (error) -> Void in
            self.logText += "\n Coverage Result: Error: " + error.localizedDescription + "\n"
            self.printLog()
            self.hideLoadingView()
        }
        coverageService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess", response.isAvailable(), response.getOperatorCode() ?? "",  response.getError(), response.getPlainResponse())
            DispatchQueue.main.async {
                
                self.logText += "\n Coverage Result: " + "isAvailable: \(response.isAvailable()) - operator_code:\(response.getOperatorCode() ?? "")" + "\n"
                self.printLog()
                if(response.isAvailable()){
                    self.doAuthorization()
                }else{
                    self.logText += "\n Coverage Result: " + "not support telco. end" + "\n"
                    self.printLog()
                    self.hideLoadingView()
                }


            }
        }

        coverageService.checkCoverage()

        
       

    }
    func doAuthorization() {
        var phone = phoneInputTextField.text!
        if(phone == ""){
            logText = "\n phone number error : \(phone)! \n"
            printLog();
            return
        }
        phone = phone.replacingOccurrences(of: "+", with: "")
        self.phoneInputTextField.endEditing(true)
        logText += "\n 2. start authorization with phone : \(phone)! \n"
        printLog();


        let authorizationService =  AuthorizationService()
//        authorizationService.debug = debug
        authorizationService.callbackFailed = { (error) -> Void in
            print("callbackFailed")
            self.logText += "\nAuth Result: Error: \(error.localizedDescription)! \n"
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
                    self.logText += "\n" + "getCode failed" + "\n"
                    self.printLog()
                }

            }
            self.hideLoadingView()

        }
        let authorizationRequest =  AuthorizationRequest.Builder()
        authorizationRequest.setScope(value: "openid ip:phone_verify ip:mobile_id")
//        authorizationRequest.setState(value: "1234abccd")
        authorizationRequest.addQueryParam(key: "login_hint", value: phoneInputTextField.text!)
//        authorizationRequest.addQueryParam(key: "sleep", value: "12000")
//        authorizationService.setEnableCarrierHeaders(enable: enableCarrierHeaders)
        let authRequest = authorizationRequest.build()
        authorizationService.doAuthorization(authRequest)

        
        
    }
    
    
    func callExchangeToken(code: String){
        self.logText += "\n3. Do exchange Token with Code \(code)\n"
        self.printLog();
        self.code = code
        var requestBodyComponents = URLComponents()
        requestBodyComponents.queryItems = [URLQueryItem(name: "client_id", value: "your_client_id"), URLQueryItem(name: "grant_type", value: "authorization_code"), URLQueryItem(name: "client_secret", value: "your_client_secret"), URLQueryItem(name: "redirect_uri", value: "https://api.dev.ipification.com/api/v1/callback"), URLQueryItem(name: "code", value: code)]
        
        var request = URLRequest(url: URL(string: "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token")!)
        request.httpMethod = "POST"
        request.httpBody = requestBodyComponents.query?.data(using: .utf8)

        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
             
            do {
                if(error != nil){
                    DispatchQueue.main.async {
                        self.logText += "\n" + "error exchange token \(error!.localizedDescription)" + "\n"
                        self.printLog()
                    }
                }
                print(response!)
                
                let json = try JSONSerialization.jsonObject(with: data!) as! Dictionary<String, AnyObject>
                self.doNext(json["access_token"] as? String)
            } catch {
                print("error")
            }
        })

        task.resume()
    }
    
    func doNext(_ accesToken: String?){
        if(accesToken == nil){
            DispatchQueue.main.async {
                self.logText += "\n" + "accessToken nil" + "\n"
                self.printLog()
            }
           
            return
        }
        do{
            let jwt = try decode(jwt: accesToken!)
            let phoneClaim = jwt.claim(name: "phone_number_verified")
            if let phoneValue = phoneClaim.string {
                isVerifedPhone = phoneValue
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
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "openResult", sender: nil)
            }
        }catch{
            
        }
        
        
        
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openResult" {
            if let nextViewController = segue.destination as? NextViewController {
                nextViewController.code = code
                nextViewController.mobileID = mobileID
                nextViewController.sub = sub
                nextViewController.isVerifedPhone = isVerifedPhone
            }
        }
    }
    
    
    func showLoadingView(){
        DispatchQueue.main.async {
            self.loadingView.isHidden = false
        }
    }
    func hideLoadingView(){
        DispatchQueue.main.async {
            self.loadingView.isHidden = true
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

