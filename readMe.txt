The ToyGIS library is designed to work with vector and raster images of various nature, (cartography, design images, scanned images)

The project (for IntelliJ IDEA 2021) includes several examples of using the library. Vector maps for the cities of Moscow and Pskov (maps obtained from MP format), a fragment of the Tver region (streets and roads), Drawing obtained from dxf format, an application showing an example of combining a raster layer and a vector one on the example of a map of Rostov Veliky, with a demonstration of a plug-in for entering graphic information , as well as a demo program that combines a map of several railway stations and stops with tabular information about these objects, such as purpose, area, year of construction, wear percentage, etc. All vector information in Russian.

To build the demo applications, you need Java(32-bit only >=1.6) IntelliJ IDEA (versions >= IntelliJ IDEA 2021.2 (Community Edition)).
You can use ant (version > 1.6) for this you need to register jdk.home.1.6 and idea.home in the waitpnt2.properties file, run the assembly bld.cmd

To run applications, you must specify the path in the files *Demo to Java (only 32-bit version >=1.6), you need to download information to display by link https://cloud.mail.ru/public/Rpfi/5PPhHhrSB and unpack the archive into the ToyGIS folder. All information after that should be in the ToyGIS/MAPDIR folder.

Applications can be run from the development environment, the default interface is English. (-lngru parameter to switch to Russian at startup)

Brief description of the application functionality:

Image viewing functionality is standard - shift zoom in zoom out with the mouse and keyboard, selection of areas of interest Ctrl+Rigth Mosue Button. A search plug-in is included in each project. In order to view an object - double-clicking the right mouse button in the object area - the plugin dialog will open with a list of the object that fell into the mouse click area. You can simply search for an object by name, there is a corresponding icon on the toolbar.
To view all the attributes of an object, double-click the left mouse button over it. A dialog will appear to view all the attributes of the objects in the mouse area. You can also manage the set of layers to display. The set of styles for each layer can also be changed.

The application for displaying railway objects visualizes tabular information along with cartographic information, allows you to perform simple operations for selecting objects by a criterion, by falling into an area of interest, and also demonstrates the possibility of visualizing the quantitative parameter of objects over a map.