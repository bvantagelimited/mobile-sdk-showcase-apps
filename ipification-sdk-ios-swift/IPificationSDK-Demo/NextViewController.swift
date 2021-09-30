//
//  NextViewController.swift
//  IPificationSDK-Demo
//
//  Created by Nguyen Huu Tinh on 5/4/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation

import UIKit
class NextViewController: UIViewController {
    @IBOutlet weak var lblVerifiedValue: UILabel!
    @IBOutlet weak var lblCodeValue: UILabel!
    @IBOutlet weak var lblMobileIDValue: UILabel!
    @IBOutlet weak var lblSubValue: UILabel!
    
    var code : String?
    var mobileID: String?
    var sub : String?
    var isVerifedPhone: String? = "false"
    
    
    
    override func viewDidLoad() {
        lblCodeValue.text = code
        lblSubValue.text = sub
        lblMobileIDValue.text = mobileID
        
        lblVerifiedValue.text = "\(isVerifedPhone ?? "false")"
        
    }
    @IBAction func close(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
}
