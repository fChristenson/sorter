sorter
======

Groups files in folders using java 8.
The app assumes that the root folder has the files to be sorted and will not move files in subfolders.

# Usage #
1. Use the terminal and go to the folder with the .jar in it.
2. Run "java -jar sorter.jar <path to folder you want to sort>".
3. Go to the folder you sorted and you should see new folders with UUID names.

# Options #
You can add options such as "java -jar sorter.jar <path to folder you want to sort> 100" to group files by 100.
Any non negative number will work and by default the group size is set to 50.

If you prefer to sort by type you can use "java -jar sorter.jar <path to folder you want to sort> .jpg .gif .flv" to sort files by type.
