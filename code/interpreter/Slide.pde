/*
An instance of this class represent a slide.
Those compose a BeamToon project and are composed by other elements that inherite from the Drawable class.
*/

class Slide extends Rectangle{
  private String name;
  private String musicFileName;
  private int startTime;
  private int timeOut;
  private String nextSlideName = null;
  private boolean stopSong;
  
  Slide(String name, Color c, boolean toReDraw, int timeOut, String nextSlideName, String musicFileName, Boolean stopSong){
    super(new AttributeFloat(0), new AttributeFloat(0), new AttributeFloat(width), new AttributeFloat(height));
    this.toRedraw = toReDraw;
    this.c = c;
    this.name = name;
    this.timeOut = timeOut;
    this.nextSlideName = nextSlideName;
    this.musicFileName = musicFileName;
    this.stopSong = stopSong;
  }
  
  void update(){
    if(timeOut <  millis() - startTime && nextSlideName != null){
      newSlideName = nextSlideName;
    }
  }
  
  void overridableDraw(){
    // w and h are revalued here to fit the window in case of resize
    this.w.setValue(floatWidth);
    this.h.setValue(floatHeight);
    super.overridableDraw();
    
    textSize(100);
    fill(125);
    textAlign(CENTER);
    text(this.name, floatWidth/2, floatHeight/2);
  }
  
  /*
  This function prepare the slide to display it.
  */
  void start(){
    super.start();
    startTime = millis();
    if(this.musicFileName != null){
      song = minim.loadFile(getPath(this.musicFileName));
      song.setGain(-10);
      song.play();
    }
  }
  
  /*
  This function stop what is supposed to be when we don't want to display the slide anymore
  */
  void stop(){
    if(song != null && stopSong){
      song.pause();
      song = null;
    }
  }
  
  public String getName(){
    return this.name;
  }
}
