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
  abstract void start();
  
  // @return true if the animation is ended or not, otherwise false
  abstract boolean isEnded();
  
  // Update this object to start from the end value and end at the start value
  abstract void reverse();
  
  // Update the value of the current value
  abstract void update();
  
  // @return the current value of this animation if it is running. Otherwise the start value.
  float getCurrentValue(){
    if(running){
      // Add update() here to have the latest of latest possible value
      this.update();
      return currentValue;
    } else {
      return getStartValue();
    }
  }
  
  // @return the start value of this animation
  abstract float getStartValue();
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
  
  void start(){
    running = true;
    startTime = millis();
    currentValue = startValue;
  }
  
  void update(){
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
    
  void reverse(){
      float temp = startValue;
      startValue = endValue;
      endValue = temp;
  }
  
  boolean isEnded(){
    return !boomerang && !loop && millis() - startTime > duration;
  }
  
  float getStartValue(){
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
  
  void start(){
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
  
  void update(){
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
  
  void reverse(){
    // reverse List of Animation
    ArrayList<AnimationFloat> temp = new ArrayList<AnimationFloat>();
    for(int i = 1; i <= animations.size(); i ++){
      AnimationFloat a = animations.get(animations.size()-i);
      a.reverse();
      temp.add(a);
    }
    animations = temp;
  }
  
  boolean isEnded(){
    return !boomerang && !loop && currentAnimationIndex >= animations.size()-1 && currentAnimation.isEnded();
  }
  
  float getStartValue(){
    return animations.get(0).getStartValue();
  }
}
