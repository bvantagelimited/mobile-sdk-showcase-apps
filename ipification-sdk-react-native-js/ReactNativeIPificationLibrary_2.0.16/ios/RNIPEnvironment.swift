import Foundation
import IPificationSDK
import UIKit
@objc(RNIPEnvironment) class RNIPEnvironment: NSObject {
    @objc func getName() {
    }
    @objc static func requiresMainQueueSetup() -> Bool {return false}

    @objc
    func constantsToExport() -> [AnyHashable : Any]! {
        return ["SANDBOX":"sandbox", "PRODUCTION": "production"]
    }
}
