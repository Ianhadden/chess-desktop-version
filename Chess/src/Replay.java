import java.util.ArrayList;


public class Replay {
    public ArrayList<String> fens;
    String currentFen;
    int currentFenNumber;
    boolean inProgress;
    boolean isDraw = false;
    
    public Replay(ArrayList<String> fens){
        this.fens = fens;
        currentFenNumber = 0;
        updateToReflectFenNumber();
    }
    
    public boolean advance(){
        if (currentFenNumber < fens.size() - 1){
            currentFenNumber++;
            updateToReflectFenNumber();
            return true;
        } else {
            return false;
        }
    }
    
    public boolean retreat(){
        if (currentFenNumber > 0){
            currentFenNumber--;
            updateToReflectFenNumber();
            return true;
        } else {
            return false;
        }
    }
    
    public void updateToReflectFenNumber(){
        currentFen = fens.get(currentFenNumber);
        if (!(currentFen.charAt(64) == ' ')){
            inProgress = false;
        } else {
            inProgress = true;
        }
        if (currentFen.charAt(64) == 'd'){
            isDraw = true;
        } else {
            isDraw = false;
        }
    }
    
    public Game startGameFromCurrentState(){
        ArrayList<String> fensUpToThisPoint = new ArrayList<String>();
        for (int i = 0; i <= currentFenNumber; i++){
            fensUpToThisPoint.add(fens.get(i));
        }
        return new Game(fensUpToThisPoint);
    }
}
