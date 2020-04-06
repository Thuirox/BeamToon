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
  
  void overridableDraw(){
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
