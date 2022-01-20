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

    
    var loginHint : String?
    var mobileID: String?
    var sub : String?
    var isVerifedPhone: Bool?
    var phoneNumber: String? = nil
    var errorMessage: String? = nil
    
    
    override func viewDidLoad() {
        lblMainInfo.text = "Phone Verified: \(isVerifedPhone!) - Input Phone Number: \(loginHint ?? phoneNumber ?? "")"
//        let subTxt = sub != nil ? " | Sub: \(sub!)" : ""
//        let mobileIdTxt = mobileID != nil ? " | MobileID: \(mobileID!)" : ""
//        let loginHintTxt = loginHint != nil ? " | LoginHint: \(loginHint!)" : ""
//        let phoneNumberTxt = phoneNumber != nil ? " | PhoneNumber: \(phoneNumber!)" : ""
//        let phoneVerifed = isVerifedPhone != nil ? "Phone Verified: \(isVerifedPhone!)" : ""
//        lblDetailInfo.text = "\(phoneVerifed)\(mobileIdTxt)\(subTxt)\(phoneNumberTxt)\(loginHintTxt) \(errorMessage ?? "")"
        
        IPConfiguration.sharedInstance.authState = nil
        
    }
    @IBAction func sendLog(_ sender: Any) {
//        dismiss(animated: true, completion: nil)
    }
}
