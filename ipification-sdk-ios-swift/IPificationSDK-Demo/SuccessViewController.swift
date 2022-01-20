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

    
    var loginHint : String?
    var mobileID: String?
    var sub : String?
    var isVerifedPhone: Bool?
    var phoneNumber: String? = nil
    
    
    override func viewDidLoad() {
        var phoneVerifedTxt  = ""
        var phoneNumberTxt = ""
        if(phoneNumber != nil){
            lblMainInfo.text = "Phone Number :\(phoneNumber!)"
            phoneNumberTxt = phoneNumber != nil ? "Phone Number: \(phoneNumber!)" : ""
        }else if(isVerifedPhone == true){
            lblMainInfo.text = "Phone Verified :\(isVerifedPhone!) - Phone Number: \(loginHint ?? "")"
//            phoneVerifedTxt = isVerifedPhone != nil ? "Phone Verified: \(isVerifedPhone!)" : ""
        }
//        let subTxt = sub != nil ? " | Sub: \(sub!)" : ""
//        let mobileIdTxt = mobileID != nil ? " | MobileID: \(mobileID!)" : ""
        IPConfiguration.sharedInstance.authState = nil
       
        lblDetailInfo.text = ""
        
    }
    @IBAction func sendLog(_ sender: Any) {
//        dismiss(animated: true, completion: nil)
    }
}
