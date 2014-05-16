#imageIoMetadata Project


This project contains the sample code associated with the following blog posts at SilverBayTech.com:

- [Part 1 - http://www.silverbaytech.com/2014/05/19/iiometadata-tutorial-part-1-background/](http://www.silverbaytech.com/2014/05/19/iiometadata-tutorial-part-1-background/)
- Part 2
- Part 3
- Part 4

## Notes on JAI ImageIO

The `jai_ImageIO` folder contains several implementations of the JAI ImageIO code.  This code contains additional image plug-ins (including the one for TIFF) that are not part of the standard Java 7 distribution.

JAI ImageIO contains native code.  The TIFF file format will work without the native code in many situations - the native code is apparently for efficiency's sake for certain compressed forms of TIFF.  As such, the default Eclipse project includes the `noNativeCode` JAR files on its build path.

You have three options:

1. You can leave things as they are, and they will probably work.
2. You can change the build path to a native implementation appropriate for your environment.  If you do this, you may need to provide JVM parameters on your launch configuration so that Java will find the native code.
3. You can remove these JAR files from your build path.  In this case, only the built-in image formats will be "seen."

