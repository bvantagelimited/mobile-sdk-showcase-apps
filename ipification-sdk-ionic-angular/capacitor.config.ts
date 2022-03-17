import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.ipification.demo',
  appName: 'demo App',
  webDir: 'www',
  bundledWebRuntime: false,
  server: { "allowNavigation": [ "https://stage.ipification.com" ] }
};

export default config;
