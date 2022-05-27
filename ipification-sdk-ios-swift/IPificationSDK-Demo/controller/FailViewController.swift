//
//  NextViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 5/4/2021.
//  Copyright © 2021 IPification. All rights reserved.
//

import Foundation
import IPificationSDK
import UIKit
class FailViewController: UIViewController {
    
    
    @IBOutlet weak var lblVerifiedValue: UILabel!
    @IBOutlet weak var lblMainInfo: UILabel!
    @IBOutlet weak var lblDetailInfo: UILabel!

    var responseData : String?
    
    
    override func viewDidLoad() {
        
        if(responseData != nil){
            print("FailViewController - responseData", responseData!)
            lblMainInfo.text = responseData!
        }
    }
    
    
    @IBAction func sendLog(_ sender: Any) {
    }
}
