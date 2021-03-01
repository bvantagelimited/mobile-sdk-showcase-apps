//
//  ViewController.swift
//  IPificationSDK-Demo
//
//  Created by IPification on 3/6/2020.
//  Copyright © 2020 IPification. All rights reserved.
//

import UIKit

class ResultViewController: UIViewController {
    var code : String? = "j4UGGJ"
    @IBOutlet weak var codeLbl: UILabel!
    @IBOutlet weak var getTokenButton: UILabel!
    
    @IBOutlet weak var accessTokenLbl: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        codeLbl.text = code
        if(code != nil){
           
        }
        
    }
    @IBAction func doExchangeToken(_ sender: Any) {
        self.tokenExchange(code: code ?? "")
    }
    func tokenExchange(code: String) {
        let Url = String(format: "https://mc-idgw.beeline.ru/oidc/mc-token")
            guard let serviceUrl = URL(string: Url) else { return }
            let loginString = String(format: "%@:%@", "IPification", "rXd3e#%Z?U{;HQ7N")
            let loginData = loginString.data(using: String.Encoding.utf8)!
            let base64LoginString = loginData.base64EncodedString()

            
            var request = URLRequest(url: serviceUrl)
            request.setValue("Basic \(base64LoginString)", forHTTPHeaderField: "Authorization")

            request.httpMethod = "POST"
        
        let myParams = "grant_type=authorization_code&redirect_uri=https://test.ipification.com/callback/beeline&code=\(self.code ?? "")"
            let postData = myParams.data(using: String.Encoding.utf8)
//            let postLength = String(format: "%d", postData!.count)
            
//            request.setValue(postLength, forHTTPHeaderField: "Content-Length")
            request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
            request.httpBody = postData
            request.timeoutInterval = 20
            let session = URLSession.shared
            session.dataTask(with: request) { (data, response, error) in
                if let response = response {
                    print(response)
                }
                if let data = data {
                    do {
                        let json = try JSONSerialization.jsonObject(with: data, options: [])
                        print("json", json)
                        let convertedString = String(data: data, encoding: String.Encoding.utf8) // the data will be converted to the string

                        DispatchQueue.main.async {
                            self.accessTokenLbl.text = convertedString
                        }
                        
//
                    } catch {
                        print(error)
                        DispatchQueue.main.async {
                            self.accessTokenLbl.text = "error \(error.localizedDescription)"
                        }
                    }
                }
            }.resume()
        }
    
   
   
}
