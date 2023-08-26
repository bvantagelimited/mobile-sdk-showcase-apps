//
//  ViewController.swift
//  GMIDBox iOS SDK Demo
//
//  Created by IPification on 11/4/2020.
//  Copyright © 2020 IPification. All rights reserved.
//


import GMiDBOXSDK

import UIKit
class ViewController: UIViewController {
    
    @IBOutlet weak var inputField: UITextField!
    @IBOutlet weak var sdkResult: UILabel!
    @IBOutlet weak var wifiResult: UILabel!
    @IBOutlet weak var cellularResult: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: "dismissKeyboard")
        view.addGestureRecognizer(tap)
        
        // set url
        inputField.text = "https://api.ipify.org"
    }
    
    @IBAction func onSDKConnect(_ sender: Any) {
        sdkResult.text = "Connecting..."
        self.makeSDKRequest()
    }
    func makeSDKRequest(){
        inputField.resignFirstResponder()
        if(inputField.text == nil || inputField.text == ""){
            print("url is empty")
            return
        }
//        print(inputField.text ?? "???")
//        let requestBuilder = CellularRequest.Builder()
//        requestBuilder.setConnectTimeout(value: 1000) //ms
//        requestBuilder.setReadTimeout(value: 1000) // ms
//        requestBuilder.addQueryParam(key: "format", value: "json")

        let requestUrl = inputField.text!
        CellularServices.sharedInstance.requestTo(url: requestUrl, successCallback: { response in
                print(response.statusCode)
                if(response.getResponseData() is String){
                        DispatchQueue.main.async {
                            self.sdkResult.text = response.getResponseData() as? String
                            self.sdkResult.textColor = UIColor.green
                        }
        
                } else if(response.getResponseData() is [String: Any]){
                        do {
                            let data1 = try JSONSerialization.data(withJSONObject: response.getResponseData(), options: JSONSerialization.WritingOptions.fragmentsAllowed) // first of all convert json to the data
                            let convertedString = String(data: data1, encoding: String.Encoding.utf8)
                            DispatchQueue.main.async {
                                self.sdkResult.text = convertedString
                                self.sdkResult.textColor = UIColor.green
                            }
                        } catch let myJSONError {
                            print(myJSONError)
                        }
                }
            },
            failureCallback: { exception in
                print("Request failed: \(exception)")
                DispatchQueue.main.async {
                    self.sdkResult.text = "\(exception.errorCode) - \(exception.errorMessage)"
                    self.sdkResult.textColor = UIColor.red
                }
            })
        
    }
    
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    @IBAction func onActiveConnect(_ sender: Any) {
        inputField.resignFirstResponder()
        if(inputField.text == nil || inputField.text == ""){
            print("url is empty")
            return
        }
        wifiResult.text = "Connecting..."
        let url = URL(string: inputField.text! )!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
            guard let data = data else { DispatchQueue.main.async {
                self.wifiResult.text = error?.localizedDescription ?? "Something wrong"
                }
                return
                
            }
            DispatchQueue.main.async {
                self.wifiResult.text = String(data: data, encoding: .utf8) ?? "error. cannot parse response data"
                
            }
        }
        
    }
    
}

