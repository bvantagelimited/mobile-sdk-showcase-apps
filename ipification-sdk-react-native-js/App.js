import { NavigationContainer } from '@react-navigation/native';
import React, {useEffect} from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import PhoneVerifyScreen from './PhoneVerifyScreen';
import HomeScreen from './Home';
import ResultScreen from './Result';
import messaging from '@react-native-firebase/messaging';
import { Platform } from "react-native";
import Constants from './Constants';
import { SafeAreaView, StyleSheet, View, NativeModules, ToastAndroid } from 'react-native';
const { RNIPNotification} = NativeModules;


const Stack = createNativeStackNavigator();

const App = () => {
  useEffect(() => {
    initNotification()
    receiveNotificationFromQuitState();
    receiveBackgroundNotification();
    backgroundThread();
    //iOS
    requestUserPermission()
    return () => {
      
    }
  }, [])
  // do at your backend server
  registerDevice = async (device_id, device_token) => {
    console.log("1. register FCM device token to your backend server ");


    var details = {
      device_id: device_id,
      device_token: device_token,
      device_type: Platform.OS,
    };
    

    
    fetch(Constants.REGISTER_DEVICE_TOKEN_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(details),
    })
      .then((response) => response.json())
      .then((responseJson) => {
        console.log("REGISTER_DEVICE_TOKEN_URL response", responseJson);
      })
      .catch((error) => {
        console.error(error);
      });
  };
  async function requestUserPermission() {
    const authorizationStatus = await messaging().requestPermission();
  
    if (authorizationStatus) {
      console.log('Permission status:', authorizationStatus);
    }
  }
  async function initNotification() {
    // Register the device with FCM
    await messaging().registerDeviceForRemoteMessages();
  
    // Get the token
    const token = await messaging().getToken();
    console.log(token)
    var state = Constants.getRandomStateValues();
    Constants.CURRENT_STATE = state;
    await registerDevice(state, token);
    // Save the token
    // await postToApi('/users/1234/tokens', { token });
  }
  const receiveNotificationFromQuitState = () => {
    messaging()
    .getInitialNotification()
    .then(async (remoteMessage) => {
      // if (remoteMessage) {
      //  showToast(
      //  'getInitialNotification:' +
      //  'Notification caused app to open from quit state',
      //  );
      // }
     });
  }
  const receiveBackgroundNotification = () => {
    messaging().onNotificationOpenedApp(async (remoteMessage) => {
    //  if (remoteMessage) {
    //   showToast(
    //   'onNotificationOpenedApp: ' +
    //   'Notification caused app to open from background state',
    //   );
    //  }
    });
  }
  const backgroundThread = () => {  
    //It's called when the app is in the background or terminated
      messaging().setBackgroundMessageHandler(
       async (remoteMessage) => {
        //  showToast("Background notification" +  JSON.stringify(remoteMessage));
         display(remoteMessage);
       });
     }
    const showToast = (message) => {
      if (Platform.OS == 'ios') {
        alert(message);
      } else {
        ToastAndroid.show(message, ToastAndroid.SHORT);
      }
  };

  const display = (remoteMessage) =>{
    RNIPNotification.showNotification(remoteMessage.body.title,  "mipmap", "ic_notification")
  }
  const createChannel = ()=>{
    // PushNotification.createChannel(
    //   {
    //     channelId: "first_app", // (required)
    //     channelName: "first_app", // (required)
    //     channelDescription: "A channel to categorise your notifications", // (optional) default: undefined.
    //     playSound: false, // (optional) default: true
    //     soundName: "default", // (optional) See `soundName` parameter of `localNotification` function
    //     importance: 4, // (optional) default: 4. Int value of the Android notification importance
    //     vibrate: true, // (optional) default: true. Creates the default vibration patten if true.
    //   },
    //   (created) => console.log(`createChannel returned '${created}'`) // (optional) callback returns whether the channel was created, false means it already existed.
    // );
  }

  // const showLocalNotification = ({title, body, id, number}, messageId) =>
    // PushNotification.localNotification({
    //   channelId: 'first_app', // (required) channelId, if the channel doesn't exist, notification will not trigger.
    //   ticker: 'Hey', // (optional)
    //   showWhen: true, // (optional) default: true
    //   autoCancel: true, // (optional) default: true
    //   largeIcon: 'ic_launcher', // (optional) default: "ic_launcher". Use "" for no large icon.
    //   // largeIconUrl: 'https://www.example.tld/picture.jpg', // (optional) default: undefined
    //   smallIcon: 'ic_launcher', // (optional) default: "ic_notification" with fallback for "ic_launcher". Use "" for default small icon.
    //   // bigText: '', // (optional) default: "message" prop
    //   // subText: '', // (optional) default: none
    //   // bigPictureUrl: 'https://www.example.tld/picture.jpg', // (optional) default: undefined
    //   bigLargeIcon: 'ic_launcher', // (optional) default: undefined
    //   // bigLargeIconUrl: 'https://www.example.tld/bigicon.jpg', // (optional) default: undefined
    //   // color: 'red', // (optional) default: system default
    //   vibrate: true, // (optional) default: true
    //   vibration: 300, // vibration length in milliseconds, ignored if vibrate=false, default: 1000
    //   tag: 'some_tag', // (optional) add tag to message
    //   group: 'group', // (optional) add group to message
    //   groupSummary: false, // (optional) set this notification to be the group summary for a group of notifications, default: false
    //   ongoing: false, // (optional) set whether this is an "ongoing" notification
    //   priority: 'high', // (optional) set notification priority, default: high
    //   visibility: 'private', // (optional) set notification visibility, default: private
    //   ignoreInForeground: false, // (optional) if true, the notification will not be visible when the app is in the foreground (useful for parity with how iOS notifications appear). should be used in combine with `com.dieam.reactnativepushnotification.notification_foreground` setting
    //   shortcutId: 'shortcut-id', // (optional) If this notification is duplicative of a Launcher shortcut, sets the id of the shortcut, in case the Launcher wants to hide the shortcut, default undefined
    //   onlyAlertOnce: true, // (optional) alert will open only once with sound and notify, default: false

    //   when: null, // (optional) Add a timestamp (Unix timestamp value in milliseconds) pertaining to the notification (usually the time the event occurred). For apps targeting Build.VERSION_CODES.N and above, this time is not shown anymore by default and must be opted into by using `showWhen`, default: null.
    //   usesChronometer: false, // (optional) Show the `when` field as a stopwatch. Instead of presenting `when` as a timestamp, the notification will show an automatically updating display of the minutes and seconds since when. Useful when showing an elapsed time (like an ongoing phone call), default: false.
    //   timeoutAfter: null, // (optional) Specifies a duration in milliseconds after which this notification should be canceled, if it is not already canceled, default: null

    //   messageId: `${number}:${id}`, // (optional) added as `message_id` to intent extras so opening push notification can find data stored by @react-native-firebase/messaging module.

    //   // actions: ['Yes', 'No'], // (Android only) See the doc for notification actions to know more
    //   invokeApp: true, // (optional) This enable click on actions to bring back the application to foreground or stay in background, default: true
    //   // repeatType: 'day', // (optional) Repeating interval. Check 'Repeating Notifications' section for more info.
    //   /* iOS only properties */
    //   category: '', // (optional) default: empty string
    //   // subtitle: 'My Notification Subtitle', // (optional) smaller title below notification title

    //   /* iOS and Android properties */
    //   id, // (optional) Valid unique 32 bit integer specified as string. default: Autogenerated Unique ID
    //   title: title, // (optional)
    //   message: body, // (required)
    //   // picture: 'https://www.example.tld/picture.jpg', // (optional) Display an picture with the notification, alias of `bigPictureUrl` for Android. default: undefined
    //   userInfo: {}, // (optional) default: {} (using null throws a JSON value '<null>' error)
    //   playSound: true, // (optional) default: true
    //   importance: Importance.HIGH,
    //   soundName: 'notification.mp3', // (optional) Sound to play when the notification is shown. Value of 'default' plays the default sound. It can be set to a custom sound such as 'android.resource://com.xyz/raw/my_sound'. It will look for the 'my_sound' audio file in 'res/raw' directory and play it. default: 'default' (default sound is played)
    //   number, // (optional) Valid 32 bit integer specified as string. default: none (Cannot be zero)
    // });
  
  return (
    <NavigationContainer>
     <Stack.Navigator>
      <Stack.Screen name="Home Screen" component={HomeScreen} />

        <Stack.Screen
          name="PhoneVerifyScreen"
          component={PhoneVerifyScreen}
          options={{ title: 'Phone Verify Screen' }}
        />
        <Stack.Screen
          name="ResultScreen"
          component={ResultScreen}
          options={{ title: 'Result Screen' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
