      // 15092021 
      // Check Coverage Function
      // before 1.6.1: 
      var isAvailable = await IpSdk.checkCoverage;

      // from 1.6.1
      var coverageResponse = await IpSdk.checkCoverage;
      var isAvailable = coverageResponse.isAvailable;
      var operatorCode = coverageResponse.operatorCode;

      print("isAvailable $isAvailable");
      print("operatorCode $operatorCode");


      // Do Authentication Function
      // before 1.6.1: 
      var authCode = await IpSdk.doAuthentication(loginHint: inputPhoneNumber);

      // from 1.6.1
      var authResponse = await IpSdk.doAuthentication(loginHint: inputPhoneNumber);
      var authCode = authResponse.code;
      print(authCode);
      print(authResponse.state);