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
    
    public void advance(){
        if (currentFenNumber < fens.size() - 1){
            currentFenNumber++;
            updateToReflectFenNumber();
        }
    }
    
    public void retreat(){
        if (currentFenNumber > 0){
            currentFenNumber--;
            updateToReflectFenNumber();
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
