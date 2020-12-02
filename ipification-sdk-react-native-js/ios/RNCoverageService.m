#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(RNCoverageService,NSObject)
RCT_EXTERN_METHOD(checkCoverage: (RCTResponseSenderBlock)callbackSuccess failed: (RCTResponseSenderBlock)callbackError)
@end
