# react-native-tsc-printing

## Getting started

`$ npm install react-native-tsc-printing --save`

### Mostly automatic installation

`$ react-native link react-native-tsc-printing`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-tsc-printing` and add `TscPrinting.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libTscPrinting.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.tscprinting.TscPrintingPackage;` to the imports at the top of the file
  - Add `new TscPrintingPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-tsc-printing'
  	project(':react-native-tsc-printing').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-tsc-printing/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-tsc-printing')
  	```


## Usage
```javascript
import TscPrinting from 'react-native-tsc-printing';

// TODO: What to do with the module?
TscPrinting;
```
