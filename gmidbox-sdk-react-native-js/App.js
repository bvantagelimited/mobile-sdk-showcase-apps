import { NavigationContainer } from "@react-navigation/native";
import React, { useEffect } from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import GMIDBOXScreen from "./GMIDBOXScreen";

const Stack = createNativeStackNavigator();

const App = () => {
  
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="GMIDBOX DEMO" component={GMIDBOXScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
