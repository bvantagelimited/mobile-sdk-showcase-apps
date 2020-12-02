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
    
    //    func onSuccess(response: CellularResponse) {
    //        print("onsuccess", response.getData())
    //
    //    }
    //
    //    func onError(error: CellularException) {
    //        print("onError" , error.localizedDescription)
    //        sdkResult.text = error.localizedDescription
    //    }
    
    @IBAction func onCellularRequest(_ sender: Any) {
        inputField.resignFirstResponder()
        if(inputField.text == nil ||  inputField.text == ""){
            return
        }
        cellularResult.text = "Connecting..."
        let url = URL(string: inputField.text!)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
            guard let data = data else {
                DispatchQueue.main.async {
                    self.cellularResult.text = error?.localizedDescription ?? "Something wrong"
                }
                return
                
            }
            DispatchQueue.main.async {
                self.cellularResult.text = String(data: data, encoding: .utf8) ?? "error. cannot parse response data"
            }
        }
        
        
    }
    
    
    var cellularRequest: CellularRequest? = nil
    
    func onSDKConnect(){
        inputField.resignFirstResponder()
        if(inputField.text == nil || inputField.text == ""){
            print("inputField is empty")
            return
        }
        print(inputField.text ?? "???")
        let endPoint = URL(string: inputField.text!)
        let builder = CellularRequest.Builder(endpoint: endPoint!)
        
        builder.setConnectTimeout(value: 10000) //ms
        builder.setReadTimeout(value: 10000) // ms
        builder.addQueryParam(key: "format", value: "json")
        
        let cellularRequest = builder.build()
        let cellularService = CellularService()
        
        cellularService.callbackFailed = { (error) -> Void in
            print("callbackFailed")
            print(error.localizedDescription)
            DispatchQueue.main.async {
                self.sdkResult.text = error.localizedDescription
            }
            
        }
        cellularService.callbackSuccess = { (response) -> Void in
            print("callbackSuccess")
            if(response.getData() is String){
                DispatchQueue.main.async {
                    self.sdkResult.text = response.getData() as? String
                }
                
            }else if(response.getData() is [String: Any]){
                do {
                    
                    let data1 = try JSONSerialization.data(withJSONObject: response.getData(), options: JSONSerialization.WritingOptions.fragmentsAllowed) // first of all convert json to the data
                    let convertedString = String(data: data1, encoding: String.Encoding.utf8)
                    DispatchQueue.main.async {
                        self.sdkResult.text = convertedString
                    }
                    
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        do{
            try cellularService.performRequest(request: cellularRequest)
        }catch{
            print("Unexpected error: \(error).")
        }
    }
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: "dismissKeyboard")
        
        //Uncomment the line below if you want the tap not not interfere and cancel other interactions.
        //tap.cancelsTouchesInView = false
        //            inputField.text = "https://testpreload.bhtelecom.ba"
        view.addGestureRecognizer(tap)
    }
    
    @IBAction func wifiConnect(_ sender: Any) {
        if(inputField.text == nil || inputField.text == ""){
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
    
    @IBAction func onSDKConnect(_ sender: Any) {
        if(inputField.text == nil || inputField.text == ""){
            return
        }
        sdkResult.text = "connecting..."
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.onSDKConnect()
        }
    }
}

