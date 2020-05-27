import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import java.util.Map; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class interpreter extends PApplet {



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

public void setup(){
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
public void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    prefixPath = selection.getParent();
    println(prefixPath);
    pathBTML = selection.getAbsolutePath();
  }
}

public void draw(){
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

public void mousePressed(){
  currentSlide.click(mouseX/scaleX, mouseY/scaleY);
}

/*
Read the file, then setup the program with the params and finally create add the Slide object in the global variable "slides".
@param path path of the file containing the code btml
*/
public void readXML(String path){
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
public String getPath(String path){
  if((new File(path)).exists()){
    return path;
  } else if((new File(prefixPath+"/"+path)).exists()){
    return prefixPath+"/"+path;
  }
  return path;
}
/*
Read an XML file and return its content with its alias references resolved and deleted.
And its import tags resolved too.
@precondition the file must be a well-formed BTML one.
@param path path of the XML file
@return the XML file content with its alias references resolved and deleted
*/
public XML resolveAlias(String path){
  XML root = loadXML(path);
  XML[] aliases = root.getChildren("alias");
  
  for(XML alias : aliases){
    root.removeChild(alias);
    
    XML[] children = root.getChildren();
    for(int j=0; j<children.length; j++){
      if(children[j].getName().charAt(0) == '#')continue;
      findAndReplace(children[j], alias);
    }
  }
  
  ArrayList<XML> aliasesArrayList = getAliasesImports(root.getChildren("import"));
  
  for(XML alias : aliasesArrayList){
    XML[] children = root.getChildren();
    for(int j=0; j<children.length; j++){
      if(children[j].getName().charAt(0) == '#')continue;
      findAndReplace(children[j], alias);
    }
  }
  return root;
}

/*
Resolve a certain alias in a certain element if the tag match.
And in the children of the element.
@param element the element that we want to try to replace by the alias
@param alias the alias that we are trying to resolve
*/
public void findAndReplace(XML element, XML alias){
  XML[] children = element.getChildren();
  
  // Try to resolve the alias in the children of the element
  for(int i=0; i<children.length; i++){
    if(children[i].getName().charAt(0) == '#')continue;
    findAndReplace(children[i], alias);
  }
  
  // Replace the element by the alias if they match
  if(element.getName() == alias.getChild(1).getName()){
    replace(element, alias);
  }
}

/*
Resolve the alias in the element
@param el the element that will be replaced by the alias
@param alias the alias that we are trying to resolve
*/
public void replace(XML el, XML alias){
  XML elementSyntax = alias.getChild(1);
  XML aliasSyntax = alias.getChild(3);
  
  el.setName(aliasSyntax.getName());
  
  recursiveReplace(el, el, elementSyntax, aliasSyntax);
}

/*
Auxiliary function doing the actual resolving of the alias in an element
@param element the element of the original XML object, without any alias replacement
@param elementToUpdate the element that has to have his attributes replaced by those of the alias
@param elementSyntax canvas of the element that has to be replaced. It is the first part of an alias in the BTML langage
@param aliasSyntax canvas of the element that will replace. It is the seconde part of an alias in the BTML langage
*/
public void recursiveReplace(XML element, XML elementToUpdate, XML elementSyntax, XML aliasSyntax){
  String[] aliasAttributes = aliasSyntax.listAttributes();

  for(int j=0; j < aliasAttributes.length; j++){
    elementToUpdate.setString(aliasAttributes[j], element.getString(aliasSyntax.getString(aliasAttributes[j]), elementSyntax.getString(aliasSyntax.getString(aliasAttributes[j]))));
  }
  
  XML[] children = aliasSyntax.getChildren();
  for(XML child: children){
    if(child.getName().charAt(0) == '#')continue;
    XML newChild = elementToUpdate.addChild(child.getName());
    recursiveReplace(element, newChild, elementSyntax, child);
  }
}

/*
Return the list of aliases that has been found in the files cited in the import
@param imports list of import tag as defined in the BTML langage
@return an ArrayList of aliases
*/
public ArrayList<XML> getAliasesImports(XML[] imports){
  ArrayList<XML> aliases = new ArrayList<XML>();
  
  getAliasesImportsAux(imports, new ArrayList<String>(), aliases);
  
  return aliases;
}

public void getAliasesImportsAux(XML[] imports, ArrayList<String> pathes, ArrayList<XML> aliases){
  String path;
  XML root;
  XML[] mainAliases;
  
  for(XML import_ : imports){
    path = import_.getString("path");
    
    if(pathes.contains(path))
    continue;
    
    pathes.add(path);
    
    root = loadXML(getPath(path));
    
    mainAliases = root.getChildren("alias");
    for(XML alias : mainAliases)
    aliases.add(alias);
    
    getAliasesImportsAux(root.getChildren("import"), pathes, aliases);
  }
}
/*
Abstract class used to implements class that will update a value through time.
*/
abstract class Animation{
  int startTime;
  boolean running = false;
  float currentValue;
  
  // determine if the animation must loop after the end
  boolean loop = false;
  
  // determine if the animation must reverse after the end
  boolean boomerang = false;
  
  // Start this animation
  public abstract void start();
  
  // @return true if the animation is ended or not, otherwise false
  public abstract boolean isEnded();
  
  // Update this object to start from the end value and end at the start value
  public abstract void reverse();
  
  // Update the value of the current value
  public abstract void update();
  
  // @return the current value of this animation if it is running. Otherwise the start value.
  public float getCurrentValue(){
    if(running){
      // Add update() here to have the latest of latest possible value
      this.update();
      return currentValue;
    } else {
      return getStartValue();
    }
  }
  
  // @return the start value of this animation
  public abstract float getStartValue();
}

/*
Implementation of abstract class Animation for a float value.
Starting at a certain value and reaching the other linearly after a certain amount of time.
*/
class AnimationFloat extends Animation{
  float startValue;
  int duration;// must be greater then 0
  float endValue;
  
  AnimationFloat(float startValue, float endValue, int duration){
    this.startValue = startValue;
    this.endValue = endValue;
    this.duration = duration;
  }
  
  AnimationFloat(float startValue, float endValue, int duration, boolean loop, boolean boomerang){
    this.startValue = startValue;
    this.endValue = endValue;
    this.duration = duration;
    this.loop = loop;
    this.boomerang = boomerang;
  }
  
  public void start(){
    running = true;
    startTime = millis();
    currentValue = startValue;
  }
  
  public void update(){
    float timeRatio = (millis()-startTime) / (float)duration;
    if(timeRatio < 1){
      currentValue = startValue + ((endValue - startValue)*timeRatio);
    } else {
      // Animation is finished
      if(boomerang){
        this.reverse();
        if(!loop){
          boomerang = false;
          this.start();
        }
      } else if (!loop){
        currentValue = endValue;
      }
      
      if(loop){
        this.start();
      }
    }
  }
    
  public void reverse(){
      float temp = startValue;
      startValue = endValue;
      endValue = temp;
  }
  
  public boolean isEnded(){
    return !boomerang && !loop && millis() - startTime > duration;
  }
  
  public float getStartValue(){
    return this.startValue;
  }
}

/*
Implementation of abstract class Animation for a float value.
Animation is following a list of values and durations.
Starting at the start value, then going to the second value in a certain amount of time being the first duration.
*/
class AnimationListFloat extends Animation{
  ArrayList<Float> values;
  ArrayList<Integer> durations;
  ArrayList<AnimationFloat> animations;
  
  AnimationFloat currentAnimation;
  int currentAnimationIndex;
  
  AnimationListFloat(ArrayList<Float> values, ArrayList<Integer> durations){
    if(values.size() <= durations.size()){throw new RuntimeException("fuck");}
    this.values = values;
    this.durations = durations;
    this.setAnimations();
  }
  
  AnimationListFloat(ArrayList<Float> values, ArrayList<Integer> durations, boolean loop, boolean boomerang){
    if(values.size() <= durations.size()){throw new RuntimeException("fuck");}
    this.values = values;
    this.durations = durations;
    this.loop = loop;
    this.boomerang = boomerang;
    this.setAnimations();
  }
  
  /*
  Create the animations ArrayList<AnimationFloat> from the parameters given in the constructor
  @precondition values.size = durations.size + 1
  */
  private void setAnimations(){
    animations = new ArrayList<AnimationFloat>();
    
    for(int i = 0; i < durations.size(); i++){
      animations.add(new AnimationFloat(values.get(i), values.get(i+1), durations.get(i)));
    }
  }
  
  public void start(){
    running = true;
    startTime = millis();
    currentAnimationIndex = -1;
    this.nextAnimation();
  }
  
  /*
  Setup the next animation
  */
  private void nextAnimation(){
    currentAnimationIndex++;
    currentAnimation = animations.get(currentAnimationIndex);
    currentAnimation.start();
    currentValue = currentAnimation.getCurrentValue();
  }
  
  public void update(){
    if(!currentAnimation.isEnded()){
      currentValue = currentAnimation.getCurrentValue();
      
    } else if(currentAnimationIndex < animations.size()-1){
      this.nextAnimation();
      
    } else {
      // Animation is finished
      if(boomerang){
        this.reverse();
        if(!loop){
          boomerang = false;
          this.start();
        }
      } else if(!loop){
        currentValue = currentAnimation.getCurrentValue();
      }
      if(loop){
        this.start();
      }
    }  
  }
  
  public void reverse(){
    // reverse List of Animation
    ArrayList<AnimationFloat> temp = new ArrayList<AnimationFloat>();
    for(int i = 1; i <= animations.size(); i ++){
      AnimationFloat a = animations.get(animations.size()-i);
      a.reverse();
      temp.add(a);
    }
    animations = temp;
  }
  
  public boolean isEnded(){
    return !boomerang && !loop && currentAnimationIndex >= animations.size()-1 && currentAnimation.isEnded();
  }
  
  public float getStartValue(){
    return animations.get(0).getStartValue();
  }
}
/*
Abstract class used to implements class that hold a value and can modify it with an Animation object.
*/
abstract class Attribute{
  public abstract void startAnimation();
}

/*
Implementation of Attribute abstract class for a float value
*/
class AttributeFloat extends Attribute{
  private float value;
  private Animation animation;
  
  /*
  @param value initial value
  @param animation object that will be used to update the value through time
  */
  AttributeFloat(float value, Animation animation){
    this.value = value;
    this.animation = animation;
  }
  
  /*
  @param value initial value
  */
  AttributeFloat(float value){
    this.value = value;
  }
  
  /*
  The value will be initialised as the start value of the animation
  @param animation object that will be used to update the value through time
  */
  AttributeFloat(Animation animation){
    this.animation = animation;
    this.value = animation.getStartValue();
  }
  
  public void startAnimation(){
    if(this.animation != null){
      this.animation.start();
    }
  }
  
  public Animation getAnimation(){
    return animation;
  }
  
  public void setAnimation(Animation animation){
    this.animation = animation;
  }
  
  public float getValue(){
    if(animation == null){
      return value;
    } else {
      return animation.getCurrentValue();
    }
  }
  
  public void setValue(float value){
    this.value = value;
  }
  
  /*
  @return true if this object has an animation, otherwise false
  */
  public boolean hasAnimation(){
    return animation != null;
  }
  
}
/*
Parse a specific XML element that is formed as a slide as defined in the BTML, and create the Slide object representing it.
@param slideXML the XML element with slide as tag
@return the Slide object representing the XML object
*/
public Slide parseSlide(XML slideXML){
  Slide slide = new Slide(slideXML.getString("name"), getColor(slideXML), PApplet.parseBoolean(slideXML.getString("toRedraw", "true")), slideXML.getInt("duration"), slideXML.getString("nextSlide", null), slideXML.getString("music"), PApplet.parseBoolean(slideXML.getString("stopSong", "true")));
  parseChildren(slideXML, slide);
  return slide;
}

/*
Parse a specific XML element that is formed as a shape as defined in the BTML, and create the Shape object representing it.
@param elemXML the XML element with shape as tag
@return the Shape object representing the XML object
*/
public Shape parseShape(XML elemXML){
  AttributeFloat x;
  AttributeFloat y;
  PShape s = createShape(PShape.PATH);
  s.beginShape();
  ArrayList<Point> points = new ArrayList<Point>();
  int pointId = 0;
  
  AttributeFloat[] bezierPointsX = new AttributeFloat[3];
  AttributeFloat[] bezierPointsY = new AttributeFloat[3];
  
  XML[] children = elemXML.getChildren();
  for(int i=0; i < children.length; i++){
    if(children[i].getName().charAt(0) != '#'){ // useless lines start with #
      
      if(children[i].getName() == "vertex"){
        x = getFloat(children[i], "x", 0);
        y = getFloat(children[i], "y", 0);
        
        if(x.hasAnimation() || y.hasAnimation()){
          points.add(new Point(x, y, pointId));
          pointId++;
        }
        
        s.vertex(x.getValue(), y.getValue());
      } else if (children[i].getName() == "bezierVertex"){
        for(int j=1; j <= 3; j++){
          bezierPointsX[j-1] = getFloat(children[i], "x"+j, 0);
          bezierPointsY[j-1] = getFloat(children[i], "y"+j, 0);
          
          if(bezierPointsX[j-1].hasAnimation() || bezierPointsY[j-1].hasAnimation()){
            points.add(new Point(bezierPointsX[j-1], bezierPointsY[j-1], pointId));
            pointId++;
          }
        }
        
        s.bezierVertex(bezierPointsX[0].getValue(), bezierPointsY[0].getValue(), bezierPointsX[1].getValue(), bezierPointsY[1].getValue(), bezierPointsX[2].getValue(), bezierPointsY[2].getValue());
      }
    }
  }
  
  s.endShape();
  return new Shape(getFloat(elemXML, "x", 0), getFloat(elemXML, "y", 0), getFloat(elemXML, "width", 0), getFloat(elemXML, "height", 0), PApplet.parseBoolean(elemXML.getString("toRedraw", "true")), points, s);
}

/*
Parse any XML element that is defined in the BTML as a drawable one: a Shape, a Rectangle or a Text
@param elemXML the XML element with one of the third tag
@return the Drawable object representing the XML object
*/
public Drawable parseElem(XML elemXML){
  String name = elemXML.getName();
  Drawable elem;
  switch(name){
    case "rectangle":
    elem = new Rectangle(getFloat(elemXML, "x", 0), getFloat(elemXML, "y", 0), getFloat(elemXML, "width", 0), getFloat(elemXML, "height", 0), PApplet.parseBoolean(elemXML.getString("toRedraw", "true")), elemXML.getString("path", null));
    elem.setColor(getColor(elemXML));
    break;
    
    case "text":
    elem = new TextDraw(getFloat(elemXML, "x", 0), getFloat(elemXML, "y", 0), getFloat(elemXML, "w", 0), getFloat(elemXML, "h", 0), elemXML.getInt("police", 16), elemXML.getString("text"));
    elem.setColor(getColor(elemXML));
    break;
    
    case "shape":
    elem = parseShape(elemXML);
    elem.setColor(getColor(elemXML));
    break;
    
    default:
    return null;
  }
  XML action = elemXML.getChild("clickActionGoTo");
  if(action != null){
    elem.setClickAction(new GoTo(action.getString("name")));
  }
  
  parseChildren(elemXML, elem);
  return elem;
}

/*
Parse the drawable children elements of a given element and add them directly to the Drawable object representing the given element.
@param parentXML XML element of the element that we want to add children
@param parent Drawable object of the element that we want to add children
*/
public void parseChildren(XML parentXML, Drawable parent){
  XML[] children = parentXML.getChildren();
  for(int i=0; i < children.length; i++){
    if(children[i].getName().charAt(0) != '#'){
      parent.addChild(parseElem(children[i]));
    }
  }
}

/*
Parse a specific XML element that is formed as a color as defined in the BTML, and create the Color object representing it.
@param elemXML the XML element with color as tag
@return the Color object representing the XML object
*/
public Color getColor(XML elemXML){
  XML c = elemXML.getChild("color");
  if(c == null){
    return new Color(50, 50, 50); // Default value
  } else {
    return new Color(getFloat(c, "red", 0), getFloat(c, "green", 0), getFloat(c, "blue", 0));
  }
}

/*
Return an AttributeFloat object representing the value of a given attribute name
@param elemXML the XML element in which we want to recuperate the float value
@param name name of the attribute we want to find
@param def default value of the AttributeFloat object if the attribute had not been found
@return the AttributeFloat object representing the attribute named "name" with its value or with def value if it had not been found
*/
public AttributeFloat getFloat(XML elemXML, String name, float def){
  XML attributeChild = elemXML.getChild(name);// it's the attribute, written as a child
  
  if(attributeChild == null){
    if(elemXML.getString(name) == ""){
      return new AttributeFloat(def);
    } else {
      return new AttributeFloat(elemXML.getFloat(name, def));
    }
  } else {
    Animation animation = getAnimation(attributeChild);
    if(animation != null)
      return new AttributeFloat(attributeChild.getFloat("value", animation.getStartValue()) , animation);
    
    return new AttributeFloat(attributeChild.getFloat("value"));
  }
}

/*
Parse a specific XML element that is formed as a Animation as defined in the BTML, and create the Animation object representing it.
@param elemXML the XML element with animation as tag
@return the Animation object representing the XML object
*/
public Animation getAnimation(XML elemXML){
  Animation animation;
  XML animationXML = elemXML.getChild("animation");
  
  if(animationXML == null){
    return null;
  } else {
    XML[] keyframes = animationXML.getChildren("keyframe");
    ArrayList<Integer> durations = new ArrayList<Integer>();
    ArrayList<Float> values = new ArrayList<Float>();
    values.add(animationXML.getFloat("startValue", 0));
    Integer lastTiming = 0;
    Integer currentTiming;
    for(int i=0; i < keyframes.length; i++){
      currentTiming = keyframes[i].getInt("timing");
      durations.add(currentTiming - lastTiming);
      lastTiming = currentTiming;
      
      values.add(keyframes[i].getFloat("value"));
    }
    animation = new AnimationListFloat(values, durations, PApplet.parseBoolean(animationXML.getString("infinite", "false")), PApplet.parseBoolean(animationXML.getString("boomerang", "false")));
  }
  return animation;
}
/*
Abstract class used to implement class that will hold a function to be executed.
Those classes are meant to be linked to a Drawable object. And therefore represent the action that is triggered when the visual object is clicked on.
Furthermore due to the fact that multiple Drawable can be stacked, each ClickAction determine if those of the others object has to be triggered to or not.
*/
abstract class ClickAction {
  boolean canClickThrough;
  
  ClickAction(){
    canClickThrough = false;
  }
  
  ClickAction(boolean cct){
    canClickThrough = cct;
  }
  
  /*
  Execute the action implemented in sub-classes.
  */
  abstract public void act();
  
  public boolean canClickThrough(){
    return canClickThrough;
  }
}

/*
This class is used to swap the current slide with another one.
*/
class GoTo extends ClickAction{
  String name;
  
  GoTo(String name, boolean cct){
    super(cct);
    this.name = name;
  }
  
  GoTo(String name){
    this.name = name;
  }
  
  public void act(){
    newSlideName = this.name;
  }
}
/*
Implementation of Attribute abstract class for a color value.
*/
class Color extends Attribute{
  AttributeFloat red;
  AttributeFloat green;
  AttributeFloat blue;
  
  Color(AttributeFloat red, AttributeFloat green, AttributeFloat blue){
    this.red = red;
    this.blue = blue;
    this.green = green;
  }
  
  Color(float red, float green, float blue){
    this.red = new AttributeFloat(red);
    this.blue = new AttributeFloat(blue);
    this.green = new AttributeFloat(green);
  }
  
  public void startAnimation(){
    red.startAnimation();
    green.startAnimation();
    blue.startAnimation();
  }
  
  public int getValue(){
    return color(red.getValue(), green.getValue(), blue.getValue());
  }
}
/*
Abstract class used to implement any element that has to be display during a BeamToon project.
*/
abstract class Drawable{
  PImage img;
  boolean isClickable = true;
  Color c;
  boolean toDraw = true;
  boolean toRedraw = true;
  boolean clickThrough = true;
  HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
  ArrayList<Drawable> children = new ArrayList<Drawable>();
  ClickAction clickAction;
  
  AttributeFloat x;
  AttributeFloat y;
  AttributeFloat h;
  AttributeFloat w;
  AttributeFloat rotationX;
  AttributeFloat rotationY;
  AttributeFloat angle;
  
  Drawable(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h){
    this.x = x;
    this.y = y;
    this.h = h;
    this.w = w;
    rotationX = new AttributeFloat(0);
    rotationY = new AttributeFloat(0);
    angle = new AttributeFloat(0);
    this.c = new Color(50, 50, 150); // Default color
    updateListAttribute();
  }
  
  Drawable(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h, AttributeFloat rotationX, AttributeFloat rotationY, AttributeFloat angle, Color c){
    this.x = x;
    this.y = y;
    this.h = h;
    this.w = w;
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    this.angle = angle;
    this.c = c;
    updateListAttribute();
  }
  
  private void updateListAttribute(){
    attributes.put("x", x);
    attributes.put("y", y);
    attributes.put("h", h);
    attributes.put("w", w);
    attributes.put("rotationX", rotationX);
    attributes.put("rotationY", rotationY);
    attributes.put("angle", angle);
    attributes.put("c", c);
  }
  
  public final void draw(){
    pushMatrix();
    translate(x.getValue()+rotationX.getValue(), y.getValue()+rotationY.getValue());
    rotate(angle.getValue());
    textAlign(LEFT);
    this.update();
    if(toDraw){
      this.overridableDraw();
    }
    if(!toRedraw){
      toDraw = false;
    }
    translate(-rotationX.getValue(), -rotationY.getValue());
    for(Drawable child : children){
      child.draw();
    }
    popMatrix();
  }
  
  /*
  Function overrided by child to implement how they will be displayed
  */
  public abstract void overridableDraw();
  
  /* 
  Function used to implement things to do before the element is displayed
  */
  public void update(){}
  
  /*
  Function called when the element has been clicked
  */
  public boolean onClick(){
    // return false, mean that no action has been accomplished and thus we can click throught this element
    if(clickAction != null){
      clickAction.act();
      return !clickAction.canClickThrough();
    }
    return !clickThrough;
  }
  
  /*
  Return if a coordinate is inside the element. Considering the hit box of the element as a rectangle starting from its bottom left corner.
  @param x x-coordinate
  @param y y-coordinate
  @return True if the coordinate couple is inside the hit box of the element or not, False otherwise
  */
  public boolean isClicked(float x, float y){
    // The hit box is considered as a rectangle
    return (isClickable && (x > this.x.getValue() && x < this.x.getValue() + this.w.getValue() && y > this.y.getValue() && y < this.y.getValue() + this.h.getValue()));
  }
  
  public void setColor(Color c){
    this.c = c;
  }
  
  public void setImg(String fileName){
    if(fileName != null){
      this.img = loadImage(fileName);
      if(this.img == null){ // If the path didn't work try to use it as relative to folder of the BTML file
        this.img = loadImage(prefixPath+"/"+fileName);
      }
    }
  }
  
  /*
  Function initializing the element when it has to be displayed
  */
  public void start(){
    toDraw = true;
    for(Map.Entry entry : attributes.entrySet()){
      attributes.get(entry.getKey()).startAnimation();
    }
    for(Drawable child : children){
      child.start();
    }
  }
  
  public void setAngle(AttributeFloat angle){
    this.angle = angle;
    updateListAttribute();
  }
  
  public void setRotationPoint(AttributeFloat rotationX, AttributeFloat rotationY){
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    updateListAttribute();
  }
  
  public void setClickAction(ClickAction ca){
    this.clickAction = ca;
  }
  
  /*
  Add a new child element to this
  @param child the element that has to become a new child of this
  */
  public void addChild(Drawable child){
    if(child != null)
    children.add(child);
  }
  
  /*
  Return if the click event has been consumed or not. And therefore it tells if other drawable element can be considered as clicked on or not.
  @param x x-coordinate
  @param y y-coordinate
  @return True the click event has been consumed, False otherwise
  */
  public boolean click(float x, float y){
    int id = children.size()-1;
    Drawable toTest;
    boolean val = false;
    
    while(!val && id > 0){ 
      toTest = children.get(id);
      val = toTest.click(x, y);
      id--;
    }
    
    if(!val && this.isClicked(x, y)){
      return this.onClick();
    }
    
    return val;
  }
}
/*
The instances of this class represent a point of a Shape element.
*/
class Point{
  private AttributeFloat x;
  private AttributeFloat y;
  private int id;
  
  public Point(AttributeFloat x, AttributeFloat y, int id){
    this.x = x;
    this.y = y;
    this.id = id;
  }
  
  public void start(){
    x.startAnimation();
    y.startAnimation();
  }
  
  public float getX(){
    return x.getValue();
  }
  
  public float getY(){
    return y.getValue();
  }
  
  public int getId(){
    return id;
  }
  
  public boolean isAnimated(){
    return x.hasAnimation() || y.hasAnimation();
  }
}
/*
An instance of this class represent a rectangle.
That can be an image sized by the rectangle.
*/
class Rectangle extends Drawable{
  Rectangle(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h){
    super(x, y, w, h);
    this.c = new Color(50, 50, 150);
  }
  
  Rectangle(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h, boolean toRedraw, String imgFileName){
    super(x, y, w, h);
    this.toRedraw = toRedraw;
    this.setImg(imgFileName);
    this.c = new Color(50, 50, 150);
  }
  
  public void overridableDraw(){
    fill(this.c.getValue());
    strokeWeight(0);
    if(this.img != null){
      image(this.img, -rotationX.getValue(), -rotationY.getValue(), w.getValue(), h.getValue());
    } else {
      rect(-rotationX.getValue(), -rotationY.getValue(), w.getValue(), h.getValue());
    }
  }
}
/*
An instance of this class represent any kind of shape.
*/
class Shape extends Drawable{
  ArrayList<Point> points = new ArrayList<Point>();
  PShape shape;
  
  Shape(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h, boolean toRedraw, ArrayList<Point> points, PShape shape){
    super(x, y, w, h);
    this.points = points;
    shape.setStrokeWeight(0);
    this.shape = shape;
    this.toRedraw = toRedraw;
  }
  
  public void start(){
    toDraw = true;
    for(Map.Entry entry : attributes.entrySet()){
      attributes.get(entry.getKey()).startAnimation();
    }
    for(Point point : points){
      point.start();
    }
    for(Drawable child : children){
      child.start();
    }
  }
  
  public void overridableDraw(){
    for(Point point : points){
      shape.setVertex(point.getId(), point.getX(), point.getY());
    }
    
    shape.setFill(this.c.getValue());
    shape(shape, -rotationX.getValue(), -rotationY.getValue());
  }
}
/*
An instance of this class represent a slide.
Those compose a BeamToon project and are composed by other elements that inherite from the Drawable class.
*/

class Slide extends Rectangle{
  private String name;
  private String musicFileName;
  private int startTime;
  private int timeOut;
  private String nextSlideName = null;
  private boolean stopSong;
  
  Slide(String name, Color c, boolean toReDraw, int timeOut, String nextSlideName, String musicFileName, Boolean stopSong){
    super(new AttributeFloat(0), new AttributeFloat(0), new AttributeFloat(width), new AttributeFloat(height));
    this.toRedraw = toReDraw;
    this.c = c;
    this.name = name;
    this.timeOut = timeOut;
    this.nextSlideName = nextSlideName;
    this.musicFileName = musicFileName;
    this.stopSong = stopSong;
  }
  
  public void update(){
    if(timeOut <  millis() - startTime && nextSlideName != null){
      newSlideName = nextSlideName;
    }
  }
  
  public void overridableDraw(){
    // w and h are revalued here to fit the window in case of resize
    this.w.setValue(floatWidth);
    this.h.setValue(floatHeight);
    super.overridableDraw();
    
    textSize(100);
    fill(125);
    textAlign(CENTER);
    text(this.name, floatWidth/2, floatHeight/2);
  }
  
  /*
  This function prepare the slide to display it.
  */
  public void start(){
    super.start();
    startTime = millis();
    if(this.musicFileName != null){
      song = minim.loadFile(getPath(this.musicFileName));
      song.setGain(-10);
      song.play();
    }
  }
  
  /*
  This function stop what is supposed to be when we don't want to display the slide anymore
  */
  public void stop(){
    if(song != null && stopSong){
      song.pause();
      song = null;
    }
  }
  
  public String getName(){
    return this.name;
  }
}
/*
An instance of this class is a text that can be visually represented.
*/
class TextDraw extends Drawable{
  String text;
  int policeSize;
  int alignment = LEFT;
  
  TextDraw(AttributeFloat x, AttributeFloat y, AttributeFloat w, AttributeFloat h, int policeSize, String text){
    super(x, y, w, h);
    this.c = new Color(150, 150, 150);
    this.text = text;
    this.policeSize = policeSize;
  }
  
  public void overridableDraw(){
    noStroke();
    textSize(policeSize);
    fill(this.c.getValue());
    strokeWeight(0);
    textAlign(alignment);
    // X/Y 5/5 needed cause the upper W is too wide and was bigger than the background
    if(w.getValue() == 0 && h.getValue() == 0) {
      text(this.text, -rotationX.getValue()+5, -rotationY.getValue()+5);
    } else {
      text(this.text, -rotationX.getValue()+5, -rotationY.getValue()+5, w.getValue(), h.getValue());
    }
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "interpreter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
