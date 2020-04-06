/*
Abstract class used to implements class that hold a value and can modify it with an Animation object.
*/
abstract class Attribute{
  abstract void startAnimation();
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
  
  void startAnimation(){
    if(this.animation != null){
      this.animation.start();
    }
  }
  
  Animation getAnimation(){
    return animation;
  }
  
  void setAnimation(Animation animation){
    this.animation = animation;
  }
  
  float getValue(){
    if(animation == null){
      return value;
    } else {
      return animation.getCurrentValue();
    }
  }
  
  void setValue(float value){
    this.value = value;
  }
  
  /*
  @return true if this object has an animation, otherwise false
  */
  public boolean hasAnimation(){
    return animation != null;
  }
  
}
