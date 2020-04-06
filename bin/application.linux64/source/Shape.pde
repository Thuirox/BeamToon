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
  
  void start(){
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
  
  void overridableDraw(){
    for(Point point : points){
      shape.setVertex(point.getId(), point.getX(), point.getY());
    }
    
    shape.setFill(this.c.getValue());
    shape(shape, -rotationX.getValue(), -rotationY.getValue());
  }
}
