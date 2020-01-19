# Image Loading Library

The library downloads an image from the Internet and shows it in ImageView.

## How to use

Create ImageLoadLibrary in you application:

    val imageLoadLibrary: ImageLoadLibrary = ImageLoadLibrary()
 
You can optionally set initialize cache:

    imageLoadLibrary.initCache(path)
If cache is not initialized, all images are downloaded always.   
 
 
Create ImageLoader:
 
    val imageLoader: ImageLoader = imageLoadLibrary.createImageLoader()
 
 Use the ImageLoader to bind view to image:
 
     imageLoader.into(yourImageView) 
     imageLoader.load("https://your_image_url")

You can optionally set placeholders for image, progress color and success/fail handlers:

     /* Should be set before load() */
     imageLoader.progressPlaceHolder = yourBitmapPlaceHolderForProgress
     imageLoader.errorPlaceHolder = yourBitmapPlaceHolderForError
     imageLoader.progressColor = yourProgressColor
     imageLoader.doOnFail = { /* Do on fail */ }
     imageLoader.doOnSuccess = { /* Do on success */}
  

## How to build and run

Put your local.properties to project root dir. 
Then execute 
  
    gradlew assembleDebug
    
or open the project in Android Studio and build. 

Run the sample application module "image_loading_library_example".     
 

## Implementation details

Used technologies/libraries: Kotlin, Coroutines, Dagger 2.

Architecture concept: Clean Architecture with splitting to layers (data, domain, UI). See the class diagram for details.  

Class diagram with classed description:

![](ClassDiagram.png) 
  
  