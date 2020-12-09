import { NgModule } from "@angular/core";
import { Routes } from "@angular/router";
import { NativeScriptRouterModule } from "@nativescript/angular";

import { Functions } from "./item/functions.component";

const routes: Routes = [
    { path: "", redirectTo: "/function", pathMatch: "full" },
    { path: "function", component: Functions }
];

@NgModule({
    imports: [NativeScriptRouterModule.forRoot(routes)],
    exports: [NativeScriptRouterModule]
})
export class AppRoutingModule { }
