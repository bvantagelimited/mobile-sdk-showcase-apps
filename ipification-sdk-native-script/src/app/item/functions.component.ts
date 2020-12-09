import { Component, NgZone, OnInit } from "@angular/core";
import { Button, EventData } from "@nativescript/core";
// import { Ipification } from 'ipification';
import {IPification} from '@nativescript/ipification';
@Component({
    templateUrl: "./functions.component.html"
})
export class Functions implements OnInit {
   
    isBusy: boolean = false;

    isAvailable: String;
    code: String;
    constructor(private zone: NgZone) { }

    ngOnInit(): void {
        this.isAvailable = `supported: N/A`;
        this.code = `code: N/A`
    }
    setAvailable(result): void {
        this.zone.run(() => {
            this.isBusy = false;
            this.isAvailable = `support?: ${result}`;
        })
        
    }
    setCode(code): void {
        this.zone.run(() => {
            this.isBusy = false;
            this.code = `authorization code?: ${code}`;
        })
        
    }
    
    checkCoverage(args: EventData) {
        this.isBusy = true
        // let button = args.object as Button;
        let self = this
        var ip = new IPification()
        ip.checkCoverage().then((success) => {
            console.log('success: ', success);
            self.setAvailable(success.parseResponse())
        })
        .catch((error) => {
            console.log('error: ', error.getException().getMessage());
            self.setAvailable(error.getException().getMessage())
        });
    }

    doAuthorize(args: EventData){
        this.isBusy = true
        var self = this
        var ip = new IPification()
        ip.doAuthorization("381123456789").then((success) => {
            console.log('success: ', success);
            self.setCode(success.parseResponse())
        })
        .catch((error) => {
            console.log('error: ', error);
            self.setCode(error.getException().getMessage())
        });
    }
}
