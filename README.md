## Welcome to Looking Glass

[Looking Glass](https://lookingglass.wustl.edu) is a programming environment
for ages 10 and up. With Looking Glass, you can create and share animated
stories, simple games, and even virtual pets.

Looking Glass is developed by the Looking Glass Team and Research Group at
[Washington University in St. Louis](http://www.wustl.edu).

## Getting Started

1. Install Java SE Development Kit 8

  Looking Glass is known to have problems with OpenJDK, we recommend using the
  official JDK/JRE from Oracle. The minumum required JRE/JDK version is 8u60.
  http://www.oracle.com/technetwork/java/javase/downloads/

2. Developing with Looking Glass

  Looking Glass is built using [Maven](https://maven.apache.org/). You can build
  Looking Glass using an IDE or at the command line.

  * Command Line - Install Maven

    This option is not well supported on Windows.

    1. Install [Maven](https://maven.apache.org/).

    2. Compile Looking Glass

      `mvn compile`

    3. Launch Looking Glass

      `./lookingglass.sh`

  * IDE - Install Eclipse IDE for Java Developers

    Supported on all platforms (Windows, Mac OS X, GNU/Linux).

    1. Install [Eclipse IDE for Java Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/mars1).

    2. Install the required plugins.

      Within Eclipse, **File** > **Import...**, **Install** > **Install Software
      Items from File**, Then import the **eclipse-install.p2f** file.

    3. Import all projects in the repository into eclipse.

      This process is made easier if you first turn off automatic building
      (**Project** > **Build Automatically**). Then select **File** >
      **Import...**, **General** > **Existing Projects into Workspace**.
      Import all of the projects into your workspace.

    4. Synchronize with Maven

      Select all projects within the Java Perspective and right click,
      then select **Maven** > **Update Project...**. Select **OK**.

    5. Compile with Maven (first time only)

      On the first build, Maven has to download a lot of resources for Looking
      Glass and sometimes Eclipse has trouble with this. To bypass this problem
      on your first build, from the menu, select **Run** >
      **Run Configurations...**, **Maven Build** > **Maven compile**, **Run**.
      Once the first build is done, turn back on automatic building
      (**Project** > **Build Automatically**).

    6. Launch Looking Glass

      From the menu, select **Run** >  **Run Configurations...**,
      **Java Application** > **Looking Glass - Production**, **Run**.

## Packaging Looking Glass

  Below you will find how to build a release package for Windows, Mac and Linux.
  All builds require that you download and install the latest version of the
  JDK. If you plan to build both 64-bit and 32-bit releases you will need to
  install at a minimum a 32-bit and 64-bit JRE.

  You may build a release in Eclipse or at the command line. To build in Eclipse
  install the prerequisites below and then select a Release launcher from the
  run configurations. (**Run** > **Run Configurations...**, **Maven Build** >
  **Release - RELEASE_PLATFORM**). To build at the command line, install the
  prerequisites instructions below and then run:
  
  `mvn clean package -Ddistribution.package=RELEASE_PLATFORM`.

  * Where *RELEASE_PLATFORM* is one of the following:
    * linux-amd64-deb
    * linux-amd64-rpm
    * linux-amd64-tar
    * linux-i586-deb
    * linux-i586-rpm
    * linux-i586-tar
    * mac-dmg
    * win32-exe
    * win32-zip
    * win64-exe
    * win64-zip

  The packaged released is located in *distribution/target*.

  Prerequisites for Building a Release:

  * Windows

    1. Install the latest JRE (32-bit, 64-bit or both).
    2. Install [Inno Setup Unicode QuickStart Pack](http://www.jrsoftware.org/isdl.php).

  * Mac OS X

    1. Install the latest JDK (64-bit only).

  * Linux

    For RPM package, you must build on RPM supported platform like Redhat or
    Fedora. For DEB package or TAR.GZ you may build on any Linux platform.

    1. Download the latest JRE TAR.GZ and place it in your *Downloads* folder.
       (32-bit, 64-bit, or both).

## Differences between Open Source and Proprietary (Official) Looking Glass

The open source version of Looking Glass does not contain *The Sims 2* assets.
This means that projects created with the proprietary version of Looking Glass
that contain Sims characters or models cannot be opened in the open source version.
However, as long as no Sims assets are used in a project, that project can be
opened in both the open source and proprietary versions.

## FAQ

1. How can I help localize/translate Looking Glass?

  Help us localize/translate Looking Glass by visiting our Zanata site: https://translate.zanata.org/zanata/project/view/lookingglass

2. Do you accept contributions?

  Email the Looking Glass Team if you are interested in contributing to our
  research project. lookingglasshelp@seas.wustl.edu

3. Is the Looking Glass Community (website) source code also available?

  We are working to make the source code for our community website available
  at some time in the future.

4. My project that was created in the Official Looking Glass won't open in the
  open source version that I compiled here. Why won't it open?

  Your project probably contains Sims assets. You will need to remove them in
  order to open your project in the open source version.

5. Can I create my own 3D models?

  Looking Glass is based on [Alice 3](http://www.alice.org), which uses it's own
  proprietary model format. If you are interested in creating your own 3D models
  that you can use in Looking Glass, you should contact the Alice 3 team.
