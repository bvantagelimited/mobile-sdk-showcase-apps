import { Component, OnDestroy } from '@angular/core';
// import 'ipification-plugin'
import {
  IPification,
  AuthResult,
  CoverageResult,
  AccessTokenInfo,
} from 'ipification-plugin';
import { NgForm } from '@angular/forms';
import { UserData } from '../providers/user-data';
import jwtDecode, { JwtPayload } from 'jwt-decode';

@Component({
  selector: 'app-login',
  templateUrl: 'login.page.html',
  styleUrls: ['login.page.scss'],
})
export class LoginPage implements OnDestroy{
  loading = false;

  authResult: AuthResult;
  coverageResult: CoverageResult;
  tokenExchangeError: string;
  tokenExchangeResult: AccessTokenInfo;
  phoneNumber: string;
  coverageError: String;
  authError: String;
  constructor(public userData: UserData) {}
  onLogin(form: NgForm) {
    this.resetData();
    if (form.valid) {
      console.log('onlogin phone number: ', this.phoneNumber);
      this.loading = true;
      this.userData
        .checkCoverage()
        .then(result => {
          console.log('hello');
          this.coverageResult = result;
          if (result.isAvailable) {
            this.doAuthorization();
          } else {
            this.loading = false;
            this.coverageError = `${result.isAvailable} ` + `${result.error}`;
          }
        })
        .catch(error => {
          console.log('login error ', error);
          this.coverageError = error;
          this.loading = false;
        });
    } else {
    }
  }
  doAuthorization = () => {
    this.userData
      .doAuthorization(this.phoneNumber)
      .then(result => {
        console.log('authResult', result);
        this.authResult = result;
        if (result.code != null) {
          this.doTokenExchange(result.code);
        } else {
          this.authError = result.full_response;
          this.loading = false;
        }
      })
      .catch(error => {
        console.log('login error ', error);
        this.authError = error;
        this.loading = false;
      });
  };
  doTokenExchange = (code: string) => {
    this.userData
      .doPostTokenExchange(code)
      .then(result => {
        // console.log(result.access_token)
        var accessTokenInfo = jwtDecode<AccessTokenInfo>(result.access_token);
        this.tokenExchangeResult = accessTokenInfo;
        this.loading = false;
      })
      .catch(error => {
        console.log('login error ', error);
        this.tokenExchangeError = error;
        this.loading = false;
      });
  };
  resetData = () => {
    this.coverageResult = null;
    this.coverageError = '';

    this.authResult = null;
    this.authError = '';

    this.tokenExchangeResult = null;
    this.tokenExchangeError = '';
  };
  ngOnDestroy(){

  }
}
