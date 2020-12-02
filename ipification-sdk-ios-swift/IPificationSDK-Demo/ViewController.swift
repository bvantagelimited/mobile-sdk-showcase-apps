//
//  ViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 3/6/2020.
//  Copyright © 2020 IPification. All rights reserved.
//

import UIKit
import  IPificationSDK
class ViewController: UIViewController {
    
    @IBOutlet weak var lblAuthorizationResult: UILabel!
    @IBOutlet weak var lblCoverageResult: UILabel!
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }
    
    @IBAction func doCoverage(_ sender: Any) {
        do {
            
            let coverageService =  CoverageService()
            coverageService.callbackFailed = { (error) -> Void in
                print("callbackFailed")
                print(error.localizedDescription)
                
            }
            coverageService.callbackSuccess = { (response) -> Void in
                print("callbackSuccess", response.isAvailable())
                DispatchQueue.main.async {
                    self.lblCoverageResult.text = "available: \(response.isAvailable())"
                    
                }
            }
            try coverageService.checkCoverage()
            
        }
        catch{
            print("Unexpected error: \(error).")
            
        }
        
        
    }
    
    @IBAction func doAuthorization(_ sender: Any) {
        do {
            
            let authorizationService =  AuthorizationService()
            authorizationService.callbackFailed = { (error) -> Void in
                print("callbackFailed")
                print(error.localizedDescription)
                
            }
            authorizationService.callbackSuccess = { (response) -> Void in
                print("callbackSuccess", response.getCode())
                DispatchQueue.main.async {
                    self.lblAuthorizationResult.text = "authorization code: " + (response.getCode() ?? "")
                    
                }
                
            }
            let authorizationRequest =  AuthorizationRequest.Builder()
            authorizationRequest.addQueryParam(key: "login_hint", value: "381692023534")
            try authorizationService.doAuthorization(authorizationRequest.build())
            
        }
        catch{
            print("Unexpected error: \(error).")
            
        }
    }
}

