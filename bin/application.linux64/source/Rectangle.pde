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
  
  void overridableDraw(){
    fill(this.c.getValue());
    strokeWeight(0);
    if(this.img != null){
      image(this.img, -rotationX.getValue(), -rotationY.getValue(), w.getValue(), h.getValue());
    } else {
      rect(-rotationX.getValue(), -rotationY.getValue(), w.getValue(), h.getValue());
    }
  }
}
