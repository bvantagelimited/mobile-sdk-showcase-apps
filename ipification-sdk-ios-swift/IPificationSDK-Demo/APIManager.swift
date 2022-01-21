//
//  IMNetwork.swift
//  IPificationSDK
//
//  Created by IPification on 16/12/2021.
//  Copyright © 2021 IPification. All rights reserved.
//

import Foundation
import UIKit
import IPificationSDK

@objc class APIManager: NSObject {
   
    public static let sharedInstance = APIManager()
    public var deviceToken = ""
    
    public var state : String = IPConfiguration.sharedInstance.generateState()
    
    private override init() {
        super.init()
    }
    func resetState()
    {
        state = IPConfiguration.sharedInstance.generateState()
    }
    func registerDevice()
    {
        print(String(format: "%@  |   %@", deviceToken, state))
        let url = URL(string: String(format: Constants.REGISTER_DEVICE_URL))!
        
        
        let json: [String: Any] = ["device_token": deviceToken, "device_id": state, "device_type":"ios"]

        let jsonData = try? JSONSerialization.data(withJSONObject: json)

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("\(String(describing: jsonData?.count))", forHTTPHeaderField: "Content-Length")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData
        
        let session = URLSession(configuration: URLSessionConfiguration.default, delegate: self, delegateQueue: nil)
        let loadDataTask = session.dataTask(with: request) { (data, response, error) in
            if let err = error {
                print("registerDevice error", err)
                return
            }
            if let httpResponse = response as? HTTPURLResponse {
                if(httpResponse.statusCode != 200){
                    print("registerDevice error", httpResponse.statusCode)
                }
                do{
                    let json = try JSONSerialization.jsonObject(with: data!) as! Dictionary<String, AnyObject>
                    print("json", json)
                } catch {
                    print("error")
                }
            } else{
                print("registerDevice error")
            }
            
        }
        loadDataTask.resume()
    }
    
    // TODO: do this at your backend side
    func callTokenExchange(code: String, success successBlock: @escaping (_ result: Data) -> Void, failure failureBlock : ((String) -> ())!)
    {
        var requestBodyComponents = URLComponents()
        requestBodyComponents.queryItems = [
            URLQueryItem(name: "grant_type", value: "authorization_code"),
            URLQueryItem(name: "client_id", value: IPConfiguration.sharedInstance.CLIENT_ID),
            URLQueryItem(name: "client_secret", value: Constants.CLIENT_SECRET),
            URLQueryItem(name: "redirect_uri", value: IPConfiguration.sharedInstance.REDIRECT_URI),
            URLQueryItem(name: "code", value: code)
        ]
        
        var request = URLRequest(url: URL(string: Constants.EXCHANGE_TOKEN_URL)!)
        request.httpMethod = "POST"
        request.httpBody = requestBodyComponents.query?.data(using: .utf8)
        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
            if(error != nil){
                print(error?.localizedDescription)
                failureBlock(error?.localizedDescription ?? "")
                return
            }
            if(data != nil){
                if let httpResponse = response as? HTTPURLResponse {
                    if(httpResponse.statusCode == 200){
                        successBlock(data!)
                    }else{
                        do {
                            let json = try JSONSerialization.jsonObject(with: data!) as! Dictionary<String, AnyObject>
//                            print("error", json["error_description"])
                            let errorMessage = json["error_description"]
                            failureBlock(errorMessage as! String)
                        } catch {
                            print("error")
                        }
                    }
                }
                
            }else{
                failureBlock("data is empty")
            }
           
        })

        task.resume()
    }
}
extension APIManager: URLSessionDelegate, URLSessionTaskDelegate {
    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse, newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) {
        // Stops the redirection, and returns (internally) the response body.
        print(request.url!.absoluteString)
        if(request.url?.absoluteString.starts(with: IPConfiguration.sharedInstance.REDIRECT_URI) == true){
            completionHandler(nil)
        }else{
            completionHandler(request)
        }
       
    }
}
