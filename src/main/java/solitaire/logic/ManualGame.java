package solitaire.logic;

import solitaire.model.BoardType;

public class ManualGame extends Game {

    public ManualGame(int size, BoardType type){
        super(size, type);
    }
    @Override
    public void playTurn(){
        //Manuel game do nothing
    }
}

