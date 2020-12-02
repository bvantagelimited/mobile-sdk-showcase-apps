import Foundation
import IPificationSDK

@objc(RNAuthenticationService) class RNAuthenticationService: NSObject {
  @objc static func requiresMainQueueSetup() -> Bool {return true}
  
  
  @objc func doAuthorization(_ callbackSuccess:  @escaping RCTResponseSenderBlock,failed callbackError: @escaping RCTResponseSenderBlock) {

   do{
      let authorizationService = AuthorizationService()
       authorizationService.callbackFailed = { (error) -> Void in
          callbackError([NSNull(), error.localizedDescription])
       }
       authorizationService.callbackSuccess = { (response) -> Void in
          callbackSuccess([NSNull(), response.getCode()])
       }
       try authorizationService.doAuthorization()
   
   } catch{
       print("Unexpected error: \(error).")
       callbackError([NSNull(), error.localizedDescription])
   }
  }
  
  
  @objc func doAuthorizationWithParams(_ params: NSDictionary, success callbackSuccess:  @escaping RCTResponseSenderBlock,failed callbackError: @escaping RCTResponseSenderBlock) {

   do{
       guard let infoDictionary = params as? [String: Any] else {
          return
       }

       let authorizationService = AuthorizationService()
       var builder =  AuthorizationRequest.Builder()
       builder.addQueryParam(key: "login_hint", value: infoDictionary["login_hint"] as! String)
    

     
       authorizationService.callbackFailed = { (error) -> Void in
          callbackError([NSNull(), error.localizedDescription])
       }
       authorizationService.callbackSuccess = { (response) -> Void in
          callbackSuccess([NSNull(), response.getCode()])
       }
       try authorizationService.doAuthorization(builder.build())
   
   } catch{
       print("Unexpected error: \(error).")
       callbackError([NSNull(), error.localizedDescription])
   }
  }
    
}
