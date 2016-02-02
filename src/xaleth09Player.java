/*
 * Atropos AI
 * the Atropos game is not my creation nor my work, the only implementation
 * created by me (Roberto Garza) is this file (xaleth09Player) which is a 
 * MinMax AlphaBeta pruning heuristic algorithm to play the Atropos Game
 * 
 * Created By Roberto Garza
 */

import java.io.BufferedInputStream;
import java.io.IOException;

public class xaleth09Player {

	public static int maxDepth = 5;  //MINIMAX SEARCH DEPTH
  	public static int alpha = Integer.MIN_VALUE;
  	public static int beta = Integer.MAX_VALUE;
	
	//names for indices for ease of access
	public static final int RED = 1;
	public static final int BLUE = 2;
	public static final int GREEN = 3;
	public static final int COLOR = 0;
	public static final int X = 1;
	public static final int Y = 2;
	public static final int Z = 3;


	//parsed GameState and LastMove
	public static int[][] gameState;
	public static int lastM[] = new int[4];


	//Emulates the circles linked list and Circle objects used in Atropos API
	public static class Circles{
	Circle head;
	Circle tail;
	int size;
		public Circles(){
			head = null;
			tail = null;		
			size = 0;		
		}

		public void push(Circle nc){
			if(size == 0){
				head = nc;
				tail = head;			
			}else{
				tail.next = nc;
				tail = nc;
			}
			size++;
		}

		public void printCircs(){
			Circle tmp = head;
			while(tmp!=null){
				System.out.printf("("+tmp.clr+","+tmp.x+","+tmp.y+","+tmp.z+")");
				tmp = tmp.next;
				if(tmp != null){
					System.out.printf(" -> ");				
				}
			}
		}

	}

	public static class Circle{
		int clr; 
		int x;
		int y;
		int z;
		Circle next = null;

		public Circle(int ncol, int nx, int ny, int nz){
			clr = ncol;
			x = nx;
			y = ny;
			z = nz;
		}

		public Circle(){
		
		}

	}

	//Cloner as to not destroy original parsed input
	public static int[][] gClone(){
		int gameClone[][] = new int[gameState.length][gameState[0].length];
		for(int i = 0; i < gameState.length; i++){
			for(int j = 0; j < gameState[0].length; j++){
				gameClone[i][j] = gameState[i][j];
			}
		}
		return gameClone;
	}

	//fixes x and z of my circle to match game
	//i read game upsidedown relative to how Atropos API reads
	//the gamestate
	public static Circle reCalc(int [][] gMat, Circle cir){
		int tmp[] = gMat[cir.x];
		int end = 0;
	
		for(int i = tmp.length-1; i >= 0; i--){
			if(tmp[i] != -1){
				end = i;				
				break;
			}
		}

		cir.x = (gMat.length - 1) - cir.x;
		cir.z = end - cir.y;
		return cir;
	}

	//function for if i can play anywhere but all options are losses
	public static Circle getAnywhereLoss(int [][] gMat, int[] lastPlay){
		int[] nLastMove = new int[4];
		for(int i = 0; i < gMat.length; i++){
			for(int j = 0; j < gMat[0].length; j++){
				if(gMat[i][j] == 0){
						gMat[i][j] = 1;
						nLastMove[COLOR] = 1; nLastMove[X] = i; nLastMove[Y] = j; nLastMove[Z] = 0;
						if(isFinished(gMat, nLastMove)){
							return new Circle(1, i, j, 0);
						}
					gMat[i][j] = 0;
				}
			 }
		}

	return null;
	}

	//I know i'm gonna lose, choose losing circle
	public static Circle findLossCirc(int [][] gMat, int [] lastPlay){
		
		if(canPlayAnywhere(gMat, lastPlay)){ // can play anywhere but all choices are losses	
			return getAnywhereLoss(gMat, lastPlay);
		}
		Circle loss = null;
		int middleColor = lastPlay[COLOR];							//mid
		int leftUpColor = gMat[lastPlay[X]-1][lastPlay[Y]-1]; 		//topLeft
		int leftColor = gMat[lastPlay[X]][lastPlay[Y]-1];			//left
		int rightColor = gMat[lastPlay[X]][lastPlay[Y]+1];			//right
		int rightUpColor = gMat[lastPlay[X]-1][lastPlay[Y]];		//topRight

		//btmleft and btmright
		int leftDownColor, rightDownColor;
		if(lastPlay[X] == (gMat.length - 2)){
			leftDownColor = gMat[lastPlay[X]+1][lastPlay[Y]-1];
			rightDownColor = gMat[lastPlay[X]+1][lastPlay[Y]];
			if( leftDownColor == 0){
				loss = new Circle(1, lastPlay[X]+1, lastPlay[Y]-1, 0);
			}
			if(	rightDownColor == 0){
				loss = new Circle(1, lastPlay[X]+1, lastPlay[Y], 0);
			}
		}else{
			leftDownColor = gMat[lastPlay[X]+1][lastPlay[Y]];
			rightDownColor = gMat[lastPlay[X]+1][lastPlay[Y]+1];
			if( leftDownColor == 0){
				loss = new Circle(1, lastPlay[X]+1, lastPlay[Y], 0);
			}
			if(	rightDownColor == 0){
				loss = new Circle(1, lastPlay[X]+1, lastPlay[Y]+1, 0);
			}
		}

		if( leftUpColor == 0){
			loss = new Circle(1, lastPlay[X]-1, lastPlay[Y]-1, 0);
		}
		if( leftColor == 0){
			loss = new Circle(1, lastPlay[X], lastPlay[Y]-1, 0);
		}
		if( rightColor == 0){
			loss = new Circle(1, lastPlay[X], lastPlay[Y]+1, 0);
		}
		if( rightUpColor == 0){
			loss = new Circle(1, lastPlay[X]-1, lastPlay[Y], 0 );
		}


		return loss;
	}

	//pulled from Atropos API and used to figure out
	//possible moves i have that won't result in loss
	public static boolean isFinished(int[][] gMat, int[] lastPlay) {
    if (lastPlay == null) {
      return false;
    }
    int middleColor = lastPlay[COLOR];							//mid
    int leftUpColor = gMat[lastPlay[X]-1][lastPlay[Y]-1]; 		//topLeft
    int leftColor = gMat[lastPlay[X]][lastPlay[Y]-1];			//left
    int rightColor = gMat[lastPlay[X]][lastPlay[Y]+1];			//right
    int rightUpColor = gMat[lastPlay[X]-1][lastPlay[Y]];		//topRight

	//btmleft and btmright
	int leftDownColor, rightDownColor;
	if(lastPlay[X] == (gMat.length - 2)){
		leftDownColor = gMat[lastPlay[X]+1][lastPlay[Y]-1];
		rightDownColor = gMat[lastPlay[X]+1][lastPlay[Y]];
	}else{
		leftDownColor = gMat[lastPlay[X]+1][lastPlay[Y]];
		rightDownColor = gMat[lastPlay[X]+1][lastPlay[Y]+1];
	}

    return ((colorConflict(middleColor, leftUpColor) &&
              colorConflict(middleColor, leftColor) &&
              colorConflict(leftUpColor, leftColor)) ||
            (colorConflict(middleColor, leftColor) &&
              colorConflict(middleColor, leftDownColor) &&
              colorConflict(leftColor, leftDownColor)) ||
            (colorConflict(middleColor, leftDownColor) &&
              colorConflict(middleColor, rightDownColor) &&
              colorConflict(leftDownColor, rightDownColor)) ||
            (colorConflict(middleColor, rightDownColor) &&
              colorConflict(middleColor, rightColor) &&
              colorConflict(rightDownColor, rightColor)) ||
            (colorConflict(middleColor, rightColor) &&
              colorConflict(middleColor, rightUpColor) &&
              colorConflict(rightColor, rightUpColor)) ||
            (colorConflict(middleColor, rightUpColor) &&
              colorConflict(middleColor, leftUpColor) &&
              colorConflict(rightUpColor, leftUpColor)));
  }

  //Aux method for isFinished(), also found in Atropos API
  public static boolean colorConflict(int colorOne, int colorTwo) {
    return ((colorOne != 0) &&
            (colorTwo != 0) &&
            (colorOne != colorTwo));
  }

  //Adapted from Atropos API to find out if the next play or my current play
  //Can be made anywhere on the board
  private static boolean canPlayAnywhere(int[][] gMat, int[] lastMove) {
	    if (lastMove == null) {
	      return true;
	    }
			
	    int height = lastMove[X];
	    int leftDistance = lastMove[Y];

		//adjust btm left/ btm right
		int btmLeft, btmRight;
		if(height == (gMat.length - 2)){
			btmLeft = gMat[height+1][leftDistance-1];
			btmRight = gMat[height+1][leftDistance];
		}else{
			btmLeft = gMat[height+1][leftDistance];
			btmRight = gMat[height+1][leftDistance+1];
		}

	    return (btmRight != 0) &&								//btmright
	            (btmLeft != 0) && 								//btmleft
	            (gMat[height][leftDistance + 1] != 0) && 		//right
	            (gMat[height - 1][leftDistance] != 0) && 		//topright
	            (gMat[height - 1][leftDistance - 1] != 0) && 	//topleft
	            (gMat[height][leftDistance - 1] != 0);			//left
	  }
  

  //figure out which are my possible moves then send them to minMax() && eval()
							//possible moves being any move that does not result in loss
	//first checks if i canPlayAnywhere (due to last move == null or because of opp's last move
	//if canPlayAnywhere, loops through entire board looking for an uncolored circle
	//if uncolored, loops through colors seeing which are possible color plays at this location
	//behaves similarly for regular move checking topLeft, topRight, left, right, btmLeft, btmRight
	//in a similar fashion
  public static Circles findMoves(int[][]gMat, int[] lastMove){
		Circles playable = new Circles();
		int [] nLastMove = new int[4];
		//if canPlayAnywhere get all possible moves
		if(canPlayAnywhere(gMat, lastMove)){
			for(int i = 0; i < gMat.length; i++){
				for(int j = 0; j < gMat[0].length; j++){
					if(gMat[i][j] == 0){
						for(int c = 1; c <=3; c++){
							gMat[i][j] = c;
							nLastMove[COLOR] = c; nLastMove[X] = i; nLastMove[Y] = j; nLastMove[Z] = 0;
							if(!isFinished(gMat, nLastMove)){
								playable.push(new Circle(c, i, j, 0));
							}
						}
						gMat[i][j] = 0;
					}
				 }
			}
			return playable;
		}
	//otherwise get moves from adjacent circles
	//topleft
		if(gMat[lastMove[X]-1][lastMove[Y]-1] == 0){
			for(int i = 1; i <=3; i++){
				gMat[lastMove[X]-1][lastMove[Y]-1] = i;
				nLastMove[COLOR] = i; nLastMove[X] = lastMove[X]-1; nLastMove[Y] = lastMove[Y]-1; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, lastMove[X]-1, lastMove[Y]-1, 0));
				}
			}
			gMat[lastMove[X]-1][lastMove[Y]-1] = 0;
		}
	//topRight
		if(gMat[lastMove[X]-1][lastMove[Y]] == 0){
			for(int i = 1; i <=3; i++){
				gMat[lastMove[X]-1][lastMove[Y]] = i;
				nLastMove[COLOR] = i; nLastMove[X] = lastMove[X]-1; nLastMove[Y] = lastMove[Y]; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, lastMove[X]-1, lastMove[Y], 0));
				}
			}
			gMat[lastMove[X]-1][lastMove[Y]] = 0;
		}
	//left
		if(gMat[lastMove[X]][lastMove[Y]-1] == 0){
			for(int i = 1; i <=3; i++){
				gMat[lastMove[X]][lastMove[Y]-1] = i;
				nLastMove[COLOR] = i; nLastMove[X] = lastMove[X]; nLastMove[Y] = lastMove[Y]-1; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, lastMove[X], lastMove[Y]-1, 0));
				}
			}
			gMat[lastMove[X]][lastMove[Y]-1] = 0;
		}
	//right
		if(gMat[lastMove[X]][lastMove[Y]+1] == 0){
			for(int i = 1; i <=3; i++){
				gMat[lastMove[X]][lastMove[Y]+1] = i;
				nLastMove[COLOR] = i; nLastMove[X] = lastMove[X]; nLastMove[Y] = lastMove[Y]+1; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, lastMove[X], lastMove[Y]+1, 0));
				}
			}
			gMat[lastMove[X]][lastMove[Y]+1] = 0;
		}
	//botLeft && botRight
		int btmLeftX, btmLeftY, btmRightX, btmRightY;
		if(lastMove[X] == (gMat.length-2)){
			btmLeftX =  lastMove[X]+1; btmLeftY = lastMove[Y]-1;
			btmRightX = lastMove[X]+1; btmRightY = lastMove[Y];
		}else{
			btmLeftX =  lastMove[X]+1; btmLeftY = lastMove[Y];
			btmRightX = lastMove[X]+1; btmRightY = lastMove[Y]+1;
		}
		//botLeft
		if(gMat[btmLeftX][btmLeftY] == 0){
			for(int i = 1; i <=3; i++){
				gMat[btmLeftX][btmLeftY] = i;
				nLastMove[COLOR] = i; nLastMove[X] = btmLeftX; nLastMove[Y] = btmLeftY; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, btmLeftX, btmLeftY, 0));
				}
			}
			gMat[btmLeftX][btmLeftY] = 0;
		}
		//botRight
		if(gMat[btmRightX][btmRightY] == 0){
			for(int i = 1; i <=3; i++){
				gMat[btmRightX][btmRightY] = i;
				nLastMove[COLOR] = i; nLastMove[X] = btmRightX; nLastMove[Y] = btmRightY; nLastMove[Z] = 0;
				if(!isFinished(gMat, nLastMove)){
					playable.push(new Circle(i, btmRightX, btmRightY, 0));
				}
			}
			gMat[btmRightX][btmRightY] = 0;
		}
		return playable;
	}

	//Parses input gameState and lastMove
	public static void parser(String inputState){
		String [] splitLast = inputState.split("LastPlay:");
		splitLast[1] = splitLast[1].trim();
		if(!splitLast[1].equals("null")){
			splitLast[1] = splitLast[1].replaceAll("(\\(|\\))","");
			String[] tmp = splitLast[1].split(",");
			for(int i = 0; i < tmp.length; i++){
				lastM[i] = Integer.parseInt(tmp[i]);
			}
		}else{
			lastM = null;
		}
		String [] splitState = splitLast[0].split("(\\]\\[|\\[|\\])");
		
		int maxRowSize = 0;
		for(int i = 0; i < splitState.length; i++){
			if(splitState[i].length() > maxRowSize){
				maxRowSize = splitState[i].length();			
			}
		}

		gameState = new int[splitState.length-1][maxRowSize];
		for(int i = 1; i < splitState.length; i++){
			for(int j = 0; j < maxRowSize; j++){
				if(j < splitState[i].length()){
					gameState[i-1][j] = Integer.parseInt(splitState[i].charAt(j)+"");				
				}else{ gameState[i-1][j] = -1; }
			}
		}


	}


	//Heuristic Evaluation of gameState for minMax()
	//somewhat brute force evalation of board
	//checks: if next move is a lose, if next move can be anywhere
	//         what playable moves are next and if a loss will occur after
	//NEEDS IMPLEMENTATION: check
	public static int eval(int [][] gMat, int[] lastMove){
		int sum = 0;		
		Circles playable;
		if(isFinished(gMat, lastMove)){  //loss
			sum-=10000;
		}
		if(canPlayAnywhere(gMat, lastMove)){ //if i made last move and the next move is anywhere
			sum -= 700;
		}else{ //if one of us can't play anywhere find out where we can play
			playable = findMoves(gMat, lastMove);
		
			sum += playable.size * 100;

			//check if any play after this one would end the game
			Circle iter = playable.head;
			int [] nLM = new int[4];
			while(iter != null){
				nLM[COLOR] = iter.clr; nLM[X] = iter.x; nLM[Y] = iter.y;
				gMat[nLM[X]][nLM[Y]] = nLM[COLOR];
				if(isFinished(gMat, nLM)){
					sum+= 1500;
				}
				gMat[nLM[X]][nLM[Y]] = 0;
				iter = iter.next;
			}
		}
	
		return sum;
	}

	//prints gameState, mainly for testing
	public static void printMat(int[][] gameState){
		for(int i = 0; i < gameState.length; i++){
			for(int j = 0; j < gameState[0].length; j++){
				System.out.printf(gameState[i][j]+" ");
			}
			System.out.println();
		}

	}

	//MinMax alpha beta Pruning alg which calls eval at maxDepth (assigned at top of file)
	//after recieving linked list of playable circles from findMoves()
	//loops through these playable circles and goes through recursive calls for 
	//each playable move, which then finds playable moves for each simulated move
	//until maxDepth is reached and an evaulation of the state after maxDepths moves
	//then proceeds to alpha/beta pruning
	public static int minMax(int[][] gMat, int player, int[] lastMove, int depth, int alpha, int beta) {
    int max = Integer.MIN_VALUE;
    int min = Integer.MAX_VALUE;
    int bestCir = -1;
    int bestScore = 0;
    int [] nLastMove = new int[4];
      if (depth == maxDepth){
      	return eval(gMat,lastMove);
      } else {

    	Circles playableCircles = findMoves(gMat, lastMove);
    	Circle iter = playableCircles.head;
    	int i = 1;
    	while(iter != null){ //for each playable circle 
    	 gMat[iter.x][iter.y] = iter.clr;
    	 //update last move;
    	 nLastMove[COLOR] = iter.clr;
    	 nLastMove[X] = iter.x;
    	 nLastMove[Y] = iter.y;
    	 nLastMove[Z] = iter.z; //when we finally choose a move we calc z
         int temp = minMax(gMat, player% 2 + 1, nLastMove, depth + 1, alpha, beta);// compare this instance
		 gMat[iter.x][iter.y] = 0;
         if (player == 1) {
           if (temp > max) {                
             max = temp;
             bestCir = i;
             bestScore = max; // if max > alpha alpha = max
             if(max > alpha) {
                 alpha = max;
               }
               if (beta < alpha) {
                 break;
               }
           } //if beta less than alpha
         } else {
         if (temp < min){
        	 min = temp;
             bestCir = i; 
             bestScore = min;
             if(min > beta) {
            	 beta = min;
             }
             if (beta < alpha) {
            	 break;
             }
          }//if beta < alpha
        }

         iter = iter.next;
		 i++;
       }
      
      if (depth == 0) {
			//System.out.println("bestCir = "+bestCir+", score: "+bestScore);
            return bestCir; 
      }else{
            return bestScore;
      }
    }
  }


	public static String move(int[][] gMat, int player) { 
    int circInd = minMax(gMat, player, lastM, 0, alpha, beta);
	//System.out.println("circInd = "+circInd);
	Circles playables = findMoves(gMat, lastM); 
	//System.out.println("playables size = "+playables.size);
	//playables.printCircs();
	//System.out.println();
	
	Circle iter;
	if(playables.size == 0){ //ai lost...
		iter = findLossCirc(gMat, lastM);	
	}else{
		iter = playables.head;
		for(int i = 1; i <= playables.size; i++){
			if(i == circInd){
				break;
			}
			iter = iter.next;
		}
	}

	if(iter != null){
		iter = reCalc(gMat, iter);
	}

	String circString ="("+Integer.toString(iter.clr)+","+Integer.toString(iter.x)+","+Integer.toString(iter.y)+","+Integer.toString(iter.z)+")"; 
    return circString;
  }


}


