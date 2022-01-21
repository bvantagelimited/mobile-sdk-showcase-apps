//
//  Util.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 21/1/2022.
//  Copyright © 2022 IPification. All rights reserved.
//

import Foundation
import JWTDecode

class Util{
    
    static func parseAccessToken(accessToken: String?) -> TokenInfo?{
        if(accessToken == nil){
            return nil
        }
        do{
            var tokenInfo = TokenInfo()
          
            let jwt = try decode(jwt: accessToken!)
            let logintHintClaim = jwt.claim(name: "login_hint")
    //            print(logintHintClaim.string)
            if let phoneNumberValue = logintHintClaim.string {
                tokenInfo.loginHint = phoneNumberValue
            }
            let phoneNumClaim = jwt.claim(name: "phone_number")
            if let phoneNumValue = phoneNumClaim.string {
                tokenInfo.phoneNumber = phoneNumValue
            }
            let phoneVerifiedClaim = jwt.claim(name: "phone_number_verified")
            if let phoneVerifiedValue = phoneVerifiedClaim.string {
                print("phoneVerifiedValue",phoneVerifiedValue)
                tokenInfo.isVerifedPhone = phoneVerifiedValue == "true"
            }
            let mobileIDClaim = jwt.claim(name: "mobile_id")
            if let mobileIDValue = mobileIDClaim.string {
                tokenInfo.mobileID = mobileIDValue
            }
            let subClaim = jwt.claim(name: "sub")
            if let subValue = subClaim.string {
                tokenInfo.sub = subValue
            }
            return tokenInfo
        } catch{
            return nil
        }
        
    }
}
