//
//  WebViewController.swift
//  IPificationSDK-Demo
//
//  Created by Nguyen Huu Tinh on 8/2/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation
import WebKit
import UIKit
class WebViewController: UIViewController, WKNavigationDelegate {
    var callback : GetCodeDelegate?
    @IBOutlet weak var Activity: UIActivityIndicatorView!
    @IBOutlet weak var debug: UITextView!
    @IBOutlet weak var webView: WKWebView!
    var url: String!
    var subDomain: Bool = false
    
    var cookies: [String]?
    var xRoute: String?
    override func viewDidLoad() {
        webView.cleanAllCookies()
        webView.navigationDelegate = self
        self.navigationController?.isNavigationBarHidden = false
        var printdata = "get cookie for url: \(url ?? "nil") \n"
        if(cookies != nil && cookies!.count > 0){
            
            let savedrequest = cookies![0].components(separatedBy: "; ")
            printdata +=  "\(cookies!.count) " + "\n"
            if(savedrequest.count > 0){
                printdata += savedrequest[0] + "\n"
                
                let cookie = HTTPCookie(properties: [
                    .domain: self.subDomain ? "mc-idgw.beeline.ru" : "beeline.ru",
                    .path: "/oidc",
                    .name: "savedrequest",
                    .value: savedrequest[0].components(separatedBy: "=").count  > 1 ? savedrequest[0].components(separatedBy: "=")[1] : "",
                    .secure: "TRUE",
                    
    //                .expires: NSDate(timeIntervalSinceNow: 1612760400)
                ])!
                webView.configuration.websiteDataStore.httpCookieStore.setCookie(cookie)
            }
            
        }
        
        if(cookies != nil && cookies!.count > 1){
            let sessionId = cookies![1].components(separatedBy: "; ")
            if(sessionId.count > 0){
                printdata += sessionId[0] + "\n"
                print("aaa",printdata)
                let cookie2 = HTTPCookie(properties: [
                    .domain: self.subDomain ? "mc-idgw.beeline.ru" : "beeline.ru",
                    .path: "/oidc",
                    .name: "SESSIONID",
                    .value: sessionId[0].components(separatedBy: "=").count  > 1 ? sessionId[0].components(separatedBy: "=")[1] : "",
                    .secure: "TRUE",
        //            .expires: NSDate(timeIntervalSinceNow: 1612760400000)
                ])!
                webView.configuration.websiteDataStore.httpCookieStore.setCookie(cookie2)
            }
            
        }
        self.debug.text = printdata
//        webView.isOpaque = false

        webView.addObserver(self, forKeyPath: "URL", options: [.new,.old], context: nil)
        webView.addObserver(self, forKeyPath: #keyPath(WKWebView.estimatedProgress), options: .new, context: nil)
        var customRequest = URLRequest(url: URL(string: self.url)!)
        customRequest.setValue("ms-bee1", forHTTPHeaderField: "X-proxy")
        customRequest.setValue("nosniff", forHTTPHeaderField: "X-Content-Type-Options")
        if(xRoute != nil && xRoute != ""){
            self.debug.text = "set xRoute: \(xRoute ?? "")"
            customRequest.setValue(xRoute, forHTTPHeaderField: "X-Route")
        }else{
            self.debug.text = "no xRoute"
        }
        if url != "" {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { // Change `2.0` to the desired number of seconds.
                self.webView.load(customRequest)
            }

            
        }
        // add activity
        
        self.Activity.startAnimating()
        self.Activity.hidesWhenStopped = true

    }
    

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        DispatchQueue.main.async {
            self.debug.text = "load failed: \(error.localizedDescription)"
            self.Activity.stopAnimating()
        }
        
    }
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        DispatchQueue.main.async {
            self.debug.text = "*** didFailProvisionalNavigation \(error.localizedDescription) \n"
        }
    }
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        DispatchQueue.main.async {
            self.debug.text = "*** didStartProvisionalNavigation \n"
        }
    }
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        DispatchQueue.main.async {
            self.debug.text = "*** decidePolicyFor navigationAction \(navigationAction.request.url?.absoluteString)\n"
        }
        
        decisionHandler(.allow)

    }
    func webView(_ webView: WKWebView, decidePolicyFor navigationResponse: WKNavigationResponse,
                 decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {

        if let response = navigationResponse.response as? HTTPURLResponse {
            DispatchQueue.main.async {
                self.debug.text = "*** decidePolicyFor navigationResponse \(response.statusCode)\n"
            }
        }
        decisionHandler(.allow)
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        
        if keyPath == "estimatedProgress" {
            DispatchQueue.main.async {
                self.debug.text = "*** loading \(Float(self.webView.estimatedProgress) * 100)%\n"
            }
        }else if let key = change?[NSKeyValueChangeKey.newKey] {
            if(key is URL){
                let url = key as! URL
                print("***")
                
                DispatchQueue.main.async {
                    self.debug.text = "*** observeValue \(url.absoluteString) \n"
                }
                if(url.absoluteString.starts(with: "https://test.ipification")){
                    self.callback?.getCode(url.absoluteString)
                    dismiss(animated: false, completion: nil)
                    DispatchQueue.main.async {
                        self.debug.text = "*** final result \(url.absoluteString) \n"
                    }
                    
                }
            }else{
                DispatchQueue.main.async {
                    self.debug.text = "... \(key) is not URL"
                }
                
            }
                
                
            
        }
    }
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        DispatchQueue.main.async {
            self.debug.text = "loadFinish"
            self.Activity.stopAnimating()
        }
        
        self.webView.configuration.websiteDataStore.httpCookieStore.getAllCookies { cookies in
            for cookie in cookies {
                print(cookie)
            }
        }
    }
}

extension WKWebView {

    func cleanAllCookies() {
        HTTPCookieStorage.shared.removeCookies(since: Date.distantPast)
        print("All cookies deleted")

        WKWebsiteDataStore.default().fetchDataRecords(ofTypes: WKWebsiteDataStore.allWebsiteDataTypes()) { records in
            records.forEach { record in
                WKWebsiteDataStore.default().removeData(ofTypes: record.dataTypes, for: [record], completionHandler: {})
                print("Cookie ::: \(record) deleted")
            }
        }
    }

    func refreshCookies() {
        self.configuration.processPool = WKProcessPool()
    }
}
