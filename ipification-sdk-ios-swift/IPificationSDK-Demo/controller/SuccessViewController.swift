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

    var tokenInfo : TokenInfo = TokenInfo()
    
    override func viewDidLoad() {
    
        if(tokenInfo.phoneNumber != nil){
            lblMainInfo.text = "Phone Number :\(tokenInfo.phoneNumber!)"
//            phoneNumberTxt = tokenInfo.phoneNumber != nil ? "Phone Number: \(tokenInfo.phoneNumber!)" : ""
        }else if(tokenInfo.isVerifedPhone == true){
            lblMainInfo.text = "Phone Verified : \(tokenInfo.isVerifedPhone!) - Phone Number: \(tokenInfo.loginHint ?? "")"
        }
        lblDetailInfo.text = ""
    }
    @IBAction func sendLog(_ sender: Any) {
    }
}
