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
  
  void startAnimation(){
    red.startAnimation();
    green.startAnimation();
    blue.startAnimation();
  }
  
  color getValue(){
    return color(red.getValue(), green.getValue(), blue.getValue());
  }
}
