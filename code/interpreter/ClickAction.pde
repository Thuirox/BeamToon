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
