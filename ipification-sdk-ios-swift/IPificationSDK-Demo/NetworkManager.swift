//
//  IMNetwork.swift
//  IPificationSDK
//
//  Created by Nguyen Huu Tinh on 16/12/2021.
//  Copyright © 2021 Nguyen Huu Tinh. All rights reserved.
//

import Foundation
import UIKit
import IPificationSDK

@objc class NetworkManager: NSObject {
    let CLIENT_SECRET = "4bc14abb-fd00-4fd7-b274-88205f2f11cb"
    let REGISTER_DEVICE_URL = "https://cases.ipification.com/merchant-service/register-device"
    let EXCHANGE_TOKEN_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
    public static let sharedInstance = NetworkManager()
    public var deviceToken = ""
    
    public var state = ""
    
    private override init() {
        super.init()
    }
    func registerDevice()
        {
            print(String(format: "%@/%@", deviceToken, state))
            let url = URL(string: String(format: REGISTER_DEVICE_URL))!
            let session = URLSession(configuration: URLSessionConfiguration.default, delegate: self, delegateQueue: nil)
            let json: [String: Any] = ["device_token": deviceToken, "device_id": state, "device_type":"ios"]

            let jsonData = try? JSONSerialization.data(withJSONObject: json)

            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("\(String(describing: jsonData?.count))", forHTTPHeaderField: "Content-Length")
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            // insert json data to the request
            request.httpBody = jsonData
            
            let loadDataTask = session.dataTask(with: request) { (data, response, error) in
                if let err = error {
                    print("registerDevice error", err)
                    return
                }
                if let httpResponse = response as? HTTPURLResponse {
                    if(httpResponse.statusCode != 200){
                        print("registerDevice error", httpResponse.statusCode)
                    }
                }else{
                    print("registerDevice error")
                }
                
            }
            loadDataTask.resume()
        }
    
    // TODO: do this at your backend side
    func callTokenExchange(code: String, success successBlock: @escaping (_ result: Data) -> Void, failure failureBlock : ((String) -> ())!){
        
        var requestBodyComponents = URLComponents()
        requestBodyComponents.queryItems = [
            URLQueryItem(name: "grant_type", value: "authorization_code"),
            URLQueryItem(name: "client_id", value: IPConfiguration.sharedInstance.CLIENT_ID),
            URLQueryItem(name: "client_secret", value: CLIENT_SECRET),
            URLQueryItem(name: "redirect_uri", value: IPConfiguration.sharedInstance.REDIRECT_URI),
            URLQueryItem(name: "code", value: code)
        ]
        
        var request = URLRequest(url: URL(string: EXCHANGE_TOKEN_URL)!)
        request.httpMethod = "POST"
        request.httpBody = requestBodyComponents.query?.data(using: .utf8)

        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
            if(error != nil){
                print(error)
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
    //api call to check state to get user_info
    func checkState(state: String, success successBlock: @escaping (_ result: Data) -> Void, failure failureBlock : ((String) -> ())!){
        var request = URLRequest(url: URL(string: "https://cases.ipification.com/merchant-service/user-info?state=\(state)")!)
        request.httpMethod = "GET"


        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
            if(error != nil){
                failureBlock(error?.localizedDescription ?? "")
                return
            }
            if(data != nil){
                successBlock(data!)
            }else{
                failureBlock("data is empty")
            }
           
        })

        task.resume()
    }
    
}
extension NetworkManager: URLSessionDelegate, URLSessionTaskDelegate {
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
