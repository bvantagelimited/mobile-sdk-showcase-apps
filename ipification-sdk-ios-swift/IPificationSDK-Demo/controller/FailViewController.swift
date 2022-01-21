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

    var tokenInfo : TokenInfo = TokenInfo()

    var errorMessage: String? = nil
    
    
    override func viewDidLoad() {
        print(errorMessage, tokenInfo)
        lblMainInfo.text = errorMessage ?? (tokenInfo.isVerifedPhone != nil ? "Phone Verified: \(tokenInfo.isVerifedPhone!) - Input Phone Number: \(tokenInfo.loginHint ?? tokenInfo.phoneNumber ?? "")" : "")

        APIManager.sharedInstance.resetState()
        
    }
    @IBAction func sendLog(_ sender: Any) {
//        dismiss(animated: true, completion: nil)
    }
}
