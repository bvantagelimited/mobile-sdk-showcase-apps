import { Constant } from './constant';
import { Injectable } from '@angular/core';
import { IPification, AuthResult , CoverageResult, TokenResult, AccessTokenInfo} from 'ipification-plugin';
import { Http, HttpResponse } from '@capacitor-community/http';


@Injectable({
  providedIn: 'root'
})
export class UserData {
  public myCallback: (name: string) => string;


  constructor(
  ) { }


  // login(phone_number: string) : Promise<any>{
  //   return this.checkCoverage(phone_number)
  // }
  async checkCoverage() : Promise<CoverageResult> {
    return await IPification.checkCoverage()
  }
  // async checkCoverage(phone_number: string) : Promise<any> {
  //   await IPification.checkCoverage()
  //   .then((result)=>{
  //     console.log(result)
  //     if(result.isAvailable){
  //         return this.doAuthorization(phone_number)
  //     }else
  //     {
  //       return Promise.reject(result)
  //     }

  //   })
  //   .catch((error)=>{
  //       return Promise.reject(error)
  //   })
  // }
  async doAuthorization(phone_number: string = ""): Promise<AuthResult>{
    let state = ""

    return await IPification.doAuthorization({login_hint: `${phone_number}`, scope: "openid ip:phone_verify", state: `${state}`})
  }

  //TODO Must do this in the server side
  async doPostTokenExchange(code: string): Promise<TokenResult>{
    return this.post(code)
  }
  async post(code: string) : Promise<any>{
    var clientInfo = await IPification.getClientInfo()

    const data = {
      'grant_type': 'authorization_code',
      'client_id': clientInfo.client_id,
      'client_secret': Constant.ClientSecret,
      'code': code,
      'redirect_uri': clientInfo.redirect_uri
    }
    const options = {
      url: Constant.TokenUrl,
      data: data,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    };
    return await Http.request({ ...options, method: 'POST' })
      .then(async response => {

        if (response.status === 200) {
          const data = await response.data;
          try {
            var json = JSON.stringify(data);
            // console.error("json",json)
            json = JSON.parse(json);
            return json
          } catch (e) {
            console.error("error", e)
          }
          return Promise.reject(JSON.stringify(response.data))

        }else{
          return Promise.reject(JSON.stringify(response.data))
        }

      })
      .catch(e => {
        return Promise.reject(e)

      })
  }
}
