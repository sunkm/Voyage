# Specify compression level
-optimizationpasses 2
# Algorithm for confusion
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# Allow access to and modification of classes and class members with modifiers during optimization
-allowaccessmodification
# Rename file source to "Sourcefile" string
-renamesourcefileattribute SourceFile
# Keep line number
-keepattributes SourceFile,LineNumberTable
# Keep generics
-keepattributes Signature

# Add *one* of the following rules to your Proguard configuration file.
# Alternatively, you can annotate classes and class members with @androidx.annotation.Keep