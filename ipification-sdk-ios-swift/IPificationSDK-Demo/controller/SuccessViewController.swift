//
//  NextViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 5/4/2021.
//  Copyright © 2021 IPification. All rights reserved.
//

import Foundation

import UIKit
import IPificationSDK
class SuccessViewController: UIViewController {
    
    @IBOutlet weak var lblVerifiedValue: UILabel!
    @IBOutlet weak var lblMainInfo: UILabel!
    @IBOutlet weak var lblDetailInfo: UILabel!

    var responseData : String?
    
    override func viewDidLoad() {
       
        if(responseData != nil){
            print("SuccessViewController - responseData", responseData!)
            lblMainInfo.text = responseData
        }
        
        lblDetailInfo.text = ""
    }
    
    
    
    @IBAction func sendLog(_ sender: Any) {
    }
}
