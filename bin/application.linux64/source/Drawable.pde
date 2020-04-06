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
  
  final void draw(){
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
  abstract void overridableDraw();
  
  /* 
  Function used to implement things to do before the element is displayed
  */
  void update(){}
  
  /*
  Function called when the element has been clicked
  */
  boolean onClick(){
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
  boolean isClicked(float x, float y){
    // The hit box is considered as a rectangle
    return (isClickable && (x > this.x.getValue() && x < this.x.getValue() + this.w.getValue() && y > this.y.getValue() && y < this.y.getValue() + this.h.getValue()));
  }
  
  void setColor(Color c){
    this.c = c;
  }
  
  void setImg(String fileName){
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
  void start(){
    toDraw = true;
    for(Map.Entry entry : attributes.entrySet()){
      attributes.get(entry.getKey()).startAnimation();
    }
    for(Drawable child : children){
      child.start();
    }
  }
  
  void setAngle(AttributeFloat angle){
    this.angle = angle;
    updateListAttribute();
  }
  
  void setRotationPoint(AttributeFloat rotationX, AttributeFloat rotationY){
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    updateListAttribute();
  }
  
  void setClickAction(ClickAction ca){
    this.clickAction = ca;
  }
  
  /*
  Add a new child element to this
  @param child the element that has to become a new child of this
  */
  void addChild(Drawable child){
    if(child != null)
    children.add(child);
  }
  
  /*
  Return if the click event has been consumed or not. And therefore it tells if other drawable element can be considered as clicked on or not.
  @param x x-coordinate
  @param y y-coordinate
  @return True the click event has been consumed, False otherwise
  */
  boolean click(float x, float y){
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
