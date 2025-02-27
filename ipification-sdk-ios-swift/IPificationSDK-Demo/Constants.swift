//
//  Constant.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 21/1/2022.
//  Copyright © 2022 IPification. All rights reserved.
//

import Foundation
struct Constants {
    
    
    static let HOST = "https://api.stage.ipification.com"
    // should be call (S2S) from your backend service
    static let EXCHANGE_TOKEN_URL = "\(HOST)/auth/realms/ipification/protocol/openid-connect/token"
    static let USER_INFO_URL = "\(HOST)/auth/realms/ipification/protocol/openid-connect/userinfo"

    
    
    static let CLIENT_ID = ""
    static let REDIRECT_URI = ""
    // put here just for demo
    static let CLIENT_SECRET = ""
    
    static let enableFCM = true
    
    static let REGISTER_DEVICE_URL = "https://sample.com/api/register-device"
    
}
