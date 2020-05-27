/*
Parse a specific XML element that is formed as a slide as defined in the BTML, and create the Slide object representing it.
@param slideXML the XML element with slide as tag
@return the Slide object representing the XML object
*/
Slide parseSlide(XML slideXML){
  Slide slide = new Slide(slideXML.getString("name"), getColor(slideXML), boolean(slideXML.getString("toRedraw", "true")), slideXML.getInt("duration"), slideXML.getString("nextSlide", null), slideXML.getString("music"), boolean(slideXML.getString("stopSong", "true")));
  parseChildren(slideXML, slide);
  return slide;
}

/*
Parse a specific XML element that is formed as a shape as defined in the BTML, and create the Shape object representing it.
@param elemXML the XML element with shape as tag
@return the Shape object representing the XML object
*/
Shape parseShape(XML elemXML){
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
  return new Shape(getFloat(elemXML, "x", 0), getFloat(elemXML, "y", 0), getFloat(elemXML, "width", 0), getFloat(elemXML, "height", 0), boolean(elemXML.getString("toRedraw", "true")), points, s);
}

/*
Parse any XML element that is defined in the BTML as a drawable one: a Shape, a Rectangle or a Text
@param elemXML the XML element with one of the third tag
@return the Drawable object representing the XML object
*/
Drawable parseElem(XML elemXML){
  String name = elemXML.getName();
  Drawable elem;
  switch(name){
    case "rectangle":
    elem = new Rectangle(getFloat(elemXML, "x", 0), getFloat(elemXML, "y", 0), getFloat(elemXML, "width", 0), getFloat(elemXML, "height", 0), boolean(elemXML.getString("toRedraw", "true")), elemXML.getString("path", null));
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
void parseChildren(XML parentXML, Drawable parent){
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
Color getColor(XML elemXML){
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
AttributeFloat getFloat(XML elemXML, String name, float def){
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
Animation getAnimation(XML elemXML){
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
    animation = new AnimationListFloat(values, durations, boolean(animationXML.getString("infinite", "false")), boolean(animationXML.getString("boomerang", "false")));
  }
  return animation;
}
