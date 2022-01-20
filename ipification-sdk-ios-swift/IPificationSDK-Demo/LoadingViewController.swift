//
//  LoadingViewController.swift
//  IPificationSDK-Demo
//
//  Created by Nguyen Huu Tinh on 16/12/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation
import UIKit
class LoadingViewController: UIViewController {
    var spinner = UIActivityIndicatorView(style: UIActivityIndicatorView.Style.large)

    override func loadView() {
        view = UIView()
        view.backgroundColor = UIColor(white: 0, alpha: 0.3)
       
//        view.sizeToFit()
        spinner.translatesAutoresizingMaskIntoConstraints = false
        spinner.startAnimating()
        view.addSubview(spinner)

        spinner.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        spinner.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
    }
}
