DroidBox
========
by Terje Norderhaug

Virtual development environment/sandbox for building Android apps in Clojure

Follow these steps and you're good to go with a Clojure Android app development sandbox on VirtualBox.

## Prerequisites

Install [VirtualBox 4.3.x](https://www.virtualbox.org/wiki/Downloads) or later.

[Download Ubuntu 14.04 VDI](https://s3.amazonaws.com/vmfest-images/ubuntu-14.04.vdi.gz)

The instructions below assumes OSX. See the [vmfest][vmfest] README for platform-specific requirements.

## Create VM Instance

1. Use the VirtualBox application to create a new instance called "DroidBox".
2. Set the Type to Linux with Ubuntu (64) as version. 
3. The minimum memory size should be set to 1024.
4. Select 'use an existing virtual drive', choosing the previously downloaded Ubuntu VDI file. 
5. Keep file size at 8.00 GB, VDI, Dynamically Allocated.
6. Complete by clicking the Create button.
7. Start the new instance.

## Connecting to the Instance

Log in to the instance in the virtual terminal or using ssh (username/password "vmfest").

Get the IP address using:

    ip addr show eth0

## Configure Name Server

Look up the name server for the host computer, for example by running this in a terminal shell:

    cat /etc/resolv.conf

Next configure the name server in the virtual terminal with the same cofiguration, e.g.:

    sudo cat > /etc/resolvconf/resolv.conf.d/tail
    nameserver 192.168.1.254

Restart the name server to activate:

    sudo /etc/init.d/resolvconf restart

## Activate APT

This is required for later use of ``add-apt-repository``:

    sudo apt-get install python-software-properties; sudo apt-get update
    sudo apt-get install software-properties-common; sudo apt-get update

Troubleshooting:
May cause problems if python-software-properties is installed after software-properties-common.

## Install Java

### For Ubuntu

    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java7-installer
    sudo apt-get update  % possibly not required

### For Debian (alternative)

    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" > sudo /etc/apt/sources.list.d/webupd8team-java.list
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" >> sudo /etc/apt/sources.list.d/webupd8team-java.list
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
    apt-get update
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    apt-get install oracle-java7-installer
    apt-get install oracle-java7-set-default  
    exit

If you have to redo these steps, remove this file first:

    rm /etc/apt/sources.list.d/webupd8team-java.list

### Wish this worked because then Pallet could do it:

    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java7-installer

Troubleshooting if there is a problem with accepting oracle license:
Make sure you've done the step above correctly. 

## Suppport 32bit

### For ubuntu14

    sudo apt-get install lib32stdc++6
    sudo apt-get install lib32z1

Troubleshooting for problem with 'aatp no such file':
Do the steps above to install 32 bit support. 

### For Ubuntu 13 (alternative):

    sudo apt-get install ia32-libs

## Install Emacs

Alternatively your editor of choice.

    sudo apt-get install emacs ; sudo apt-get update

## Install Unzip

    sudo apt-get install unzip ; sudo apt-get update

## Install Maven

    sudo apt-get install maven

## Get Leiningen

Get the latest leiningen:

    wget https://raw.github.com/technomancy/leiningen/stable/bin/lein
    sudo mv lein /usr/local/bin/lein
    sudo chmod +x /usr/local/bin/lein 
    /usr/local/bin/lein

### Alternatively get the older officially supported version:

    sudo apt-get install leiningen
    sudo apt-get upgrade leiningen
    sudo apt-get update

## Android SDK

Get the full bundle (although you may only need the sdk):

    wget http://dl.google.com/android/adt/adt-bundle-linux-x86_64-20140702.zip
    unzip adt-bundle-linux-x86_64-20140702.zip
    mv adt-bundle-linux-x86_64-20140702 /home/vmfest/android-sdk-linux

### Preferable but doesn't work:

    sudo apt-get install android-sdk  
    tools/android update sdk --no-ui

## Update Path

For convenience add the sdk tools to your path:

    cat >> ~/.profile
    export PATH=~/android-sdk-linux/sdk/tools/:~/android-sdk-linux/sdk/platform-tools/:$PATH

### References

http://androidtutorialforbeginners.com/install-android-sdk/
better:
http://www.webupd8.org/2012/08/install-adb-and-fastboot-android-tools.html
better:
http://www.upubuntu.com/2012/05/how-to-install-android-sdk-release-18.html
Download zip from here: https://developer.android.com/sdk/index.html

## Generate Key

Generate a key for releases: 

    mkdir ~/.android/
    keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048

Later you can use the path to the key in the profile.clj file.

## Android CLJ

    mkdir ~/.lein/
    emacs ~/.lein/profiles.clj

Change the content of ~/.lein/profiles.clj as follows:

    {:user {:plugins [ [lein-droid "0.2.3"] ] :android {:sdk-path "/home/vmfest/android-sdk-linux/sdk"}}}

The sdk path shoud be the same as downloaded previously.

Troubleshooting:
if the profiles.clj doesn't seem to take efect (so no droid task) consider installing it manually:

    lein plugin install lein-droid "0.2.3" 

## Final Steps

Save the image in VirtualBox.
Create a clone of this image for new instances to avoid repeating the steps each time.

To create your first app check out the tutorial at:
https://github.com/krisc/events/blob/master/tutorial.md
