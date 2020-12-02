import Foundation
import IPificationSDK

@objc(RNCoverageService) class RNCoverageService: NSObject {
  @objc static func requiresMainQueueSetup() -> Bool {return true}
  
  
  @objc func checkCoverage(_ callbackSuccess:  @escaping RCTResponseSenderBlock,failed callbackError: @escaping RCTResponseSenderBlock) {

   do {
       let coverageService = CoverageService()
       coverageService.callbackFailed = { (error) -> Void in
//           print(error.localizedDescription)
            callbackError([NSNull(), error.localizedDescription])
       }
       coverageService.callbackSuccess = { (response) -> Void in
//           print("check coverage result: ", response.isAvailable())
            callbackSuccess([NSNull(), response.isAvailable()])
       }
       try coverageService.checkCoverage()
   } catch{
       print("Unexpected error: \(error).")
       callbackError([NSNull(), error.localizedDescription])
   }
  }
    
}
