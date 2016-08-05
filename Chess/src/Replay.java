import java.util.ArrayList;


public class Replay {
    public ArrayList<String> fens;
    String currentFen;
    int currentFenNumber;
    boolean inProgress;
    
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
        if (currentFenNumber == fens.size() - 1){
            inProgress = false;
        } else {
            inProgress = true;
        }
    }
}
