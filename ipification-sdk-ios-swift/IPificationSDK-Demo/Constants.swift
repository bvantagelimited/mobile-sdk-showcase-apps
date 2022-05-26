//
//  Constant.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 21/1/2022.
//  Copyright © 2022 IPification. All rights reserved.
//

import Foundation
struct Constants {
    static let HOST = "https://stage.ipification.com"
   
    static let CHECK_COVERAGE_URL = "\(HOST)/auth/realms/ipification/coverage"
    static let AUTH_URL = "\(HOST)/auth/realms/ipification/protocol/openid-connect/auth"
    static let EXCHANGE_TOKEN_URL = "\(HOST)/auth/realms/ipification/protocol/openid-connect/token"
    
    static let REGISTER_DEVICE_URL = "https://cases.ipification.com/merchant-service/register-device"
    
    
    static let CLIENT_ID = "6f2026a683bc439ebb414a03f9012f27"
    static let REDIRECT_URI = "com.ipification.demoapp://oauth2redirect/"
    static let CLIENT_SECRET = "4bc14abb-fd00-4fd7-b274-88205f2f11cb"
}
