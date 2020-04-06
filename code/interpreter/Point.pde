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
