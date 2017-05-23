# browserstack-local-java

[![Build Status](https://travis-ci.org/browserstack/browserstack-local-java.svg?branch=master)](https://travis-ci.org/browserstack/browserstack-local-java)

Java bindings for BrowserStack Local.

## Installation

Add this dependency to your project's POM:
```xml
<dependency>
    <groupId>com.browserstack</groupId>
    <artifactId>browserstack-local-java</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Example

```java
import com.browserstack.local.Local;

# creates an instance of Local
Local bsLocal = new Local();

# replace <browserstack-accesskey> with your key. You can also set an environment variable - "BROWSERSTACK_ACCESS_KEY".
HashMap<String, String> bsLocalArgs = new HashMap<String, String>();
bsLocalArgs.put("key", "<browserstack-accesskey>");

# starts the Local instance with the required arguments
bsLocal.start(bsLocalArgs);

# check if BrowserStack local instance is running
System.out.println(bsLocal.isRunning());

#stop the Local instance
bsLocal.stop();
```

## Arguments

Apart from the key, all other BrowserStack Local modifiers are optional. For the full list of modifiers, refer [BrowserStack Local modifiers](https://www.browserstack.com/local-testing#modifiers). For examples, refer below -

#### Verbose Logging
To enable verbose logging -
```java
bsLocalArgs.put("v", "true");
```

#### Folder Testing
To test local folder rather internal server, provide path to folder as value of this option -
```java
bsLocalArgs.put("f", "/my/awesome/folder");
```

#### Force Start
To kill other running Browserstack Local instances -
```java
bsLocalArgs.put("force", "true");
```

#### Only Automate
To disable local testing for Live and Screenshots, and enable only Automate -
```java
bsLocalArgs.put("onlyAutomate", "true");
```

#### Force Local
To route all traffic via local(your) machine -
```java
bsLocalArgs.put("forcelocal", "true");
```

#### Proxy
To use a proxy for local testing -

* proxyHost: Hostname/IP of proxy, remaining proxy options are ignored if this option is absent
* proxyPort: Port for the proxy, defaults to 3128 when -proxyHost is used
* proxyUser: Username for connecting to proxy (Basic Auth Only)
* proxyPass: Password for USERNAME, will be ignored if USERNAME is empty or not specified

```java
bsLocalArgs.put("proxyHost", "127.0.0.1");
bsLocalArgs.put("proxyPort", "8000");
bsLocalArgs.put("proxyUser", "user");
bsLocalArgs.put("proxyPass", "password");
```

#### Local Identifier
If doing simultaneous multiple local testing connections, set this uniquely for different processes -
```java
bsLocalArgs.put("localIdentifier", "randomstring");
```

## Additional Arguments

#### Binary Path

By default, BrowserStack local wrappers try downloading and executing the latest version of BrowserStack binary in ~/.browserstack or the present working directory or the tmp folder by order. But you can override these by passing the -binarypath argument.
Path to specify local Binary path -
```java
bsLocalArgs.put("binarypath", "/browserstack/BrowserStackLocal");
```

#### Logfile
To save the logs to the file while running with the '-v' argument, you can specify the path of the file. By default the logs are saved in the local.log file in the present woring directory.
To specify the path to file where the logs will be saved -
```java
bsLocalArgs.put("v", "true");
bsLocalArgs.put("logfile", "/browserstack/logs.txt");
```

## Contribute

### Compile Instructions

To compile the package, `mvn compile`.

To run the test suite run, `mvn test`.

### Reporting bugs

You can submit bug reports either in the Github issue tracker.

Before submitting an issue please check if there is already an existing issue. If there is, please add any additional information give it a "+1" in the comments.

When submitting an issue please describe the issue clearly, including how to reproduce the bug, which situations it appears in, what you expect to happen, what actually happens, and what platform (operating system and version) you are using.

### Pull Requests

We love pull requests! We are very happy to work with you to get your changes merged in, however, please keep the following in mind.

* Adhere to the coding conventions you see in the surrounding code.
* Include tests, and make sure all tests pass.
* Before submitting a pull-request, clean up the git history by going over your commits and squashing together minor changes and fixes into the corresponding commits. You can do this using the interactive rebase command.
