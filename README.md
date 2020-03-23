
#  Android Detect Store/terminal SDK

This document describes how integrators can integrate Detect Store/Terminal SDK which powers the following functions in the integrator’s application: 
* Detect Store.
* Detect Terminal.
* Detect Store’s Terminals (Manual check in).


## Requirements
* SDK supports min API level 21.
* SDK requires Java 8.
* Bluetooth LTE Hardware
##  Required Permissions

* Location Permission

# Integration
### SDK installation using maven
* You should compile the project using Java 8 or a higher version. Below is the sample code to add Java 8 support, the code should be added to the project level gradle file.
```groovy {
    compileOptions{
      sourceCompatibility JavaVersion.VERSION_1_8
      targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

* You need to include the following dependencies to the application level gradle file 
```groovy 
    mavenCentral()     
    maven { 
    		url https://repo.beamuae.app
    }

```
* You need to include the following dependencies to the application level gradle file 
```groovy
  implementation ('app.beamuae:physicalstore:1.1.10:release@aar'){
    transitive = true
  }

```

# SDK Overview

## Initialize and Start Sdk
PhysicalStoreCredentialProvider is an interface that initializes the SDK for a specific partner with an authentication token. You need to provide the SDK with authentication token and partner id. The sample code below demonstrates how to initialize the SDK.


```kotlin
      abstract fun initialize(
        context: Context, 
        server: PhysicalStoreServer,
        physicalStoreCredentialProvider: PhysicalStoreCredentialProvidr, 
        sdkInitializeListener: PhysicalStoreSDKStartListener)

```

```kotlin
 PhysicalStoreSDK.instance.initialize(this, PhysicalStoreServer.STAGING, object : PhysicalStoreCredentialProvider {
            override fun getCredentials(credentialsListener: PhysicalStoreCredentialsListener) {
                credentialsListener.onCredentialReceived(
                    PhysicalStoreCredentials(partnerId = "<<PARTNER_ID>>", token = "Bearer <<TOKEN>>")
                )
            }

        },object : PhysicalStoreSDKStartListener {
                override fun onSdkStarted() {
                  
                }

            })
```

<b>PhysicalStoreServer:</b> is an enum object you can use to specify target environment of SDK; possible values are as shown in the sample code below. 

```kotlin
  enum class PhysicalStoreServer(val host: String) {
      STAGING,
      PROD
  }
```

## PhysicalStoreCredentialProvider
This listener has one function called <b>onCredentialReceived()</b>. This function should be implemented on the class which has capability of receive authentication token and partner id. This function has one parameter called You can call this function after a async call to backend to get token information. Before every request to Beam Services. This function is going to call by SDK in order to get credentials for request.

```swift
override fun getCredentials(credentialsListener: PhysicalStoreCredentialsListener) {
                credentialsListener.onCredentialReceived(
                    PhysicalStoreCredentials(partnerId = "<<PARTNER_ID>>", token = "Bearer <<TOKEN>>")
                )
            }
```


# Physical Store/Terminal SDK Overview 
All SDK functions can be accessed from PhysicalStoreSdk object which is an abstract class you will use to access Physical Store SDK functionalities. The sections below will describe the SDK functionalities details.

### PhysicalStoreCallBack <<T>T>
* An interface used for collecting the Async operations results.

### PhysicalStoreCredentials
* PhysicalStoreCredentials object holds all information related to the partner’s authenticated user, the list below describes each field of the PhysicalStoreCredentials object:

* <b>partnerId</b>: partner key at beam side (example: LOYALTY).
* <b>userId</b>: optional id of the partner’s user.
* <b>token</b>: Auth0 token of the logged in user.

```kotlin
data class PhysicalStoreCredentials(
    var partnerId: String,
    var userId: String? = null,
    var token: String
)
```

### Store
* Store object holds all information related to the detected store, the list below describes each field of the store object:

* <b>currency</b>: 3 chars represents The currency accepted at that store (example: AED).
* <b>storeId</b>: store alias and represents unique identifier for the store on beam system.
* <b>storeGroupId</b>: group alias and unique identifier for the store group on beam system.
* <b>storeDisplayName</b>: The display of the store with respect to MAF loyalty.
* <b>experienceType</b>: The specific experience available at that store and it is an enum of (contactless, dine-in, fuel_station, etc..).


```kotlin
data class Store(
    var currency: String,
    var storeGroupId: String,
    var storeId: String,
    var storeDisplayName: String,
    var experienceType: ExperienceType,
    internal var terminals: List<Terminal>


)
```

### Terminal
* Terminal object holds all information related to the detected terminal, the list below describes each field of the Terminal object:



* <b>terminalId</b>: terminal alias and represents unique identifier for the store on beam system.
* <b>terminalDisplayName</b>: The display name of the terminal.

* <b>store</b>:  Is an object that includes store details mentioned earlier (currency, storeId, etc)




```kotlin
data class Terminal(
    var terminalId: String,
    var terminalDisplayName: String,
    var store: Store
)
```


### PhysicalStoreError

PhysicalStoreError object returns all errors that will occur while using the PhysicalStoreSDK. The object contains the following fields:

* <b>errorCode</b>:  is a numerical value that serves as indicator to a specific fault.
* <b>errorDescription</b>: is a descriptive message describing a specific errorCode.


```kotlin
enum class PhysicalStoreError constructor(private var message: String?) {
    ACTIVE_SESSION_EXISISTS("There is already an active detect session"),
       UNKNOWN("Unknown Error Occurred"),
       NO_STORE_FOUND("No Store Found"),
       NO_TERMINAL_FOUND("No Terminal Found"),
       UNAUTHORIZED("Unauthorized"),
       LOCATION_PERMISSION_NOT_GRANTED("Location permission not granted"),
       INVALID_STORE("The store is invalid"),
       INVALID_TERMINAL("The terminal is invalid")
```

# Operations
### Detect Store
Use detectStore () to retrieve the store related to your location.

```kotlin
PhysicalStoreSDK.instance.detectStore(object : PhysicalStoreCallBack<Store> {
            override fun onSuccess(result: Store) {
                showSnackBar("You Are At ${result.storeDisplayName}")
            }

            override fun onFailed(error: PhysicalStoreError) {
                showSnackBar(error.getMessage()!!)
            }


        })
```


```kotlin
    abstract fun detectStore(listener: PhysicalStoreCallBack<Store>)
```

### Detect Terminal
In order to start the payment’s process the user needs to initiate the pay process and tap, inhere the integrator needs to call detectTerminal(). In which the user would retrieve the store along one or more terminals

```kotlin
PhysicalStoreSDK.instance.detectTerminal(object : PhysicalStoreCallBack<Terminal> {
            override fun onSuccess(result: Terminal) {
                showSnackBar("You Are paying at terminal ${result.terminalId}")
            }

            override fun onFailed(error: PhysicalStoreError) {
                showSnackBar(error.getMessage()!!)
            }


        })
```

```kotlin
    abstract fun detectTerminal(listener: PhysicalStoreCallBack<Terminal>)

```

If detect terminal returns <b>NO_TERMINAL_FOUND</b> the app needs to view all terminals to the user in order select the right terminal, he/she is trying to pay at. 

Hence, this process is called manual check-in and below method needs to be addressed before initiating the payment.


### Detect Store Terminals
In order to start the payment through manual check-in process the user needs to select the right terminal to pay at, inhere the integrator needs to call getStoreTerminals(). In which the app would retrieve the store along available terminals so that the user select the terminal for the manual check-in.


```kotlin
PhysicalStoreSDK.instance.getStoreTerminals(object : PhysicalStoreCallBack<List<Terminal>> {
            override fun onSuccess(result: List<Terminal>) {
              
            }

            override fun onFailed(error: PhysicalStoreError) {
              
                showSnackBar(error.getMessage()!!)
            }

        })
```

```kotlin
abstract fun getStoreTerminals(callback: PhysicalStoreCallBack<List<Terminal>>)
```


<font color=red>Note</font>: Manual check-in is a fall back mechanism when the terminal the user is trying to pay at is down


### Notes:
* The timeout for detecting store or terminal is 30 Seconds. 
* SDK Requires real time Location Permission. There is no need to add permission to manifest file but Real Time Location Permission should be handled before to use any action on SDK.
* SDK Uses bluetooth_lte for detecting stores. Because of that devices should have bluetooth_lte hardware for that. Devices does not have bluetooth_lte, is not going to see the application on store.
  ```xml
  <uses-feature
            android:name="android.hardware.bluetooth_le"
            android:required="true"/>
  ```

## Version
* ..1
