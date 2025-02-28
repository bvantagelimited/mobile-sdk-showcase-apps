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

    
    
    static let CLIENT_ID = "webclient3"
    static let REDIRECT_URI = "https://test-stage.ipification.com/auth/callback/pvn_ip"
    // put here just for demo
    static let CLIENT_SECRET = "99dd1d64-9f99-4748-beb6-9f86ff103cda"
    
    
    // IM
    static let enableFCM = true
    
    static let REGISTER_DEVICE_URL = "https://cases.ipification.com/merchant-service/register-device"
    
}
