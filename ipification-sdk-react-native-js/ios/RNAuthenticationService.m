#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(RNAuthenticationService,NSObject)
RCT_EXTERN_METHOD(doAuthorization: (RCTResponseSenderBlock)callbackSuccess failed: (RCTResponseSenderBlock)callbackError)
RCT_EXTERN_METHOD(doAuthorizationWithParams: (NSDictionary *)params success:(RCTResponseSenderBlock)callbackSuccess failed: (RCTResponseSenderBlock)callbackError)
@end
