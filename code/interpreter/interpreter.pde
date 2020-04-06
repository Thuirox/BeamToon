import ddf.minim.*; //<>//
import java.util.Map;
Minim minim;
AudioPlayer song;

String pathBTML;
String prefixPath;
float floatWidth = (float) 1280;
float floatHeight = (float) 720;

float scaleX = 1;
float scaleY = 1;

String slideName = "0";
String newSlideName = null;
HashMap<String, Slide> slides = new HashMap<String, Slide>();
Slide currentSlide = null;

XML xml;

void setup(){
  if(args!=null){
    pathBTML = args[0];
    File f = new File(pathBTML);
    prefixPath = f.getParent();
  } else {
    selectInput("Select a BTML file to process:", "fileSelected"); // This function is asynchronous
    while(pathBTML == null){
      delay(10);
    }
  }
  readXML(pathBTML);
  surface.setResizable(true);
  minim = new Minim(this);
  surface.setSize((int)floatWidth, (int)floatHeight);
  
  frameRate(60);
}

/*
Callback of "selectInput" function. It means that this function is call when the user has selected a file or close de selection window.
@param selection file selected by the user
*/
void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    prefixPath = selection.getParent();
    println(prefixPath);
    pathBTML = selection.getAbsolutePath();
  }
}

void draw(){
  scaleX = width/floatWidth;
  scaleY = height/floatHeight;
  scale(scaleX, scaleY);
  try{
    if(newSlideName != null){
      if(currentSlide != null){
        currentSlide.stop();
      }
      currentSlide = slides.get(newSlideName);
      currentSlide.start();
      
      slideName = newSlideName;
      newSlideName = null;
    }
  } catch(IndexOutOfBoundsException e){
    println(e.getMessage());
  }
  currentSlide.draw();
}

void mousePressed(){
  currentSlide.click(mouseX/scaleX, mouseY/scaleY);
}

/*
Read the file, then setup the program with the params and finally create add the Slide object in the global variable "slides".
@param path path of the file containing the code btml
*/
void readXML(String path){
  xml = resolveAlias(path);
  XML meta = xml.getChild("meta");
  floatWidth = meta.getFloat("width", 1280);
  floatHeight = meta.getFloat("height", 720);
  newSlideName = meta.getString("firstSlide", "0");
  
  XML[] slidesXML = xml.getChildren("slide");
  Slide tempSlide;
  
  for(int i=0; i < slidesXML.length; i++){
    tempSlide = parseSlide(slidesXML[i]);
    slides.put(tempSlide.getName(), tempSlide);
  }
}

/*
Give the path to an existing file, based on an absolute or a relative one.
If no existing file have been found, return the parameter.
@param path an absolute or a relative path of the file.
@return the path to the existing file, if it exists. Otherwise the parameter.
*/
String getPath(String path){
  if((new File(path)).exists()){
    return path;
  } else if((new File(prefixPath+"/"+path)).exists()){
    return prefixPath+"/"+path;
  }
  return path;
}
