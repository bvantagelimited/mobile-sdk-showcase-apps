//
//  BaseViewController.swift
//  IPificationSDK-Demo
//
//  Created by Nguyen Huu Tinh on 16/12/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation
import UIKit


class BaseViewController : UIViewController{
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    private var child : LoadingViewController? = nil
    
    func showLoadingViewAutoEnd() {
        child = LoadingViewController()
        
        // add the spinner view controller
        addChild(child!)
        child!.view.frame = view.frame
        view.addSubview(child!.view)
        child!.didMove(toParent: self)
//
        // wait two seconds to simulate some work happening
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            // then remove the spinner view controller
            self.child?.willMove(toParent: nil)
            self.child?.view.removeFromSuperview()
            self.child?.removeFromParent()
        }
    }
    func showLoadingView() {
        child = LoadingViewController()
        
        // add the spinner view controller
        addChild(child!)
        child!.view.frame = view.frame
        view.addSubview(child!.view)
        child!.didMove(toParent: self)
//
//        // wait two seconds to simulate some work happening
//        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
//            // then remove the spinner view controller
//            self.child?.willMove(toParent: nil)
//            self.child?.view.removeFromSuperview()
//            self.child?.removeFromParent()
//        }
    }
    func hideLoadingView(){
        child?.willMove(toParent: nil)
        child?.view.removeFromSuperview()
        child?.removeFromParent()
    }
}
