//
//  FabricTwitterKit.m
//  FabricTwitterKit
//
//  Created by Trevor Porter on 8/1/16.
//  Copyright © 2016 Trevor Porter. All rights reserved.
//

#import "FabricTwitterKit.h"
#import "RCTBridgeModule.h"
#import "RCTEventDispatcher.h"
#import "RCTBridge.h"
#import <TwitterKit/TwitterKit.h>

@implementation FabricTwitterKit
@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(login:(RCTResponseSenderBlock)callback)
{
    [[Twitter sharedInstance] logInWithCompletion:^(TWTRSession *session, NSError *error) {
        if (session) {
            NSDictionary *body = @{@"authToken": session.authToken,
                                   @"authTokenSecret": session.authTokenSecret,
                                   @"userID":session.userID,
                                   @"userName":session.userName};
            callback(@[[NSNull null], body]);
        } else {
            NSLog(@"error: %@", [error localizedDescription]);
            callback(@[[error localizedDescription]]);
        }
    }];
}

RCT_EXPORT_METHOD(composeTweet:(NSDictionary *)options :(RCTResponseSenderBlock)callback) {

    NSString *body = options[@"body"];

    TWTRComposer *composer = [[TWTRComposer alloc] init];

    if (body) {
        [composer setText:body];
    }

    UIViewController *rootView = [UIApplication sharedApplication].keyWindow.rootViewController;
    [composer showFromViewController:rootView completion:^(TWTRComposerResult result) {

        bool completed = NO, cancelled = NO, error = NO;

        if (result == TWTRComposerResultCancelled) {
            cancelled = YES;
        }
        else {
            completed = YES;
        }

        callback(@[@(completed), @(cancelled), @(error)]);

    }];
}

RCT_EXPORT_METHOD(logOut)
{
    TWTRSessionStore *store = [[Twitter sharedInstance] sessionStore];
    NSString *userID = store.session.userID;

    [store logOutUserID:userID];
}


- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

@end
