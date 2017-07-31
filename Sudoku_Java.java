import java.util.*;

public class Sudoku_Java {

    static sudokuCell [][] sudokuNumbers=new sudokuCell[9][9];
    static int solvedNumbers=0;

    public static void main(String[] args) {
        String sudokuNumberString="000820090500000000308040007100000040006402503000090010093004000004035200000700900";
        makeSudoku(sudokuNumberString);
        printSudoku(sudokuNumbers);
        while (solvedNumbers<81){
            compareLineColumnSquare();
            comparePossibleNumbers();
        }
        printSudoku(sudokuNumbers);
    }

    public static void compareLineColumnSquare () {
        for (int line = 0; line < 9; line++) {
            for (int column = 0; column < 9; column++) {
                int nr = sudokuNumbers[line][column].number;
                if (nr == 0) {                                                      // delete numbers that are not possible from possibleNumbers
                    for (int lineOrColumn = 0; lineOrColumn < 9; lineOrColumn++) {
                        removeWhenNotPossible(line, column, lineOrColumn, column);  // check in same column
                        removeWhenNotPossible(line, column, line, lineOrColumn);    // check in same line
                    }

                    int thisLine=calculateLineOrColumn(line);
                    int thisColumn=calculateLineOrColumn(column);

                    for (int k = thisLine - 3; k < thisLine; k++) {                 // check in same square
                        for (int l = thisColumn - 3; l < thisColumn; l++) {
                            removeWhenNotPossible(line, column, k, l);
                        }
                    }                                                               // all values that are the same as another value in the same line, column or square are removed
                } else {
                    sudokuNumbers[line][column].possibleNumbers.clear();
                }

                if (sudokuNumbers[line][column].possibleNumbers.size()==1){         // if only 1 number is possible, that number will be filled in
                    fillInNumber(line, column, sudokuNumbers[line][column].possibleNumbers.get(0));
                }
            }
        }
    }

    public static void removeWhenNotPossible(int lineOuter, int columnOuter, int lineInner, int columnInner){
        if (sudokuNumbers[lineInner][columnInner].number != 0) {
            int numberDelete = sudokuNumbers[lineInner][columnInner].number;
            sudokuNumbers[lineOuter][columnOuter].possibleNumbers.remove(Integer.valueOf(numberDelete));
        }
    }

    public static void comparePossibleNumbers() {
        for (int line = 0; line < 9; line++) {
            outerloop:
            for (int column = 0; column < 9; column++) {
                int nr = sudokuNumbers[line][column].number;

                if (nr == 0) {                                                                                     // is done for each cell
                    if (comparePossibleWithLineAndColumn(line, column, true)){
                        continue outerloop;
                    }
                    if (comparePossibleWithLineAndColumn(line, column, false)){
                        continue outerloop;
                    }
                    if (comparePossibleWithSquare(line, column)){
                        continue outerloop;
                    }
                }
            }
        }
    }

    public static Boolean comparePossibleWithLineAndColumn(int line, int column, Boolean forLine){
        ArrayList<Integer> uniquePossibleNumbers = new ArrayList<>(sudokuNumbers[line][column].possibleNumbers);

        for (int lineOrColumn = 0; lineOrColumn < 9; lineOrColumn++) {
            int lineToCompare;
            int columnToCompare;

            if (forLine){
                lineToCompare=line;
                columnToCompare=lineOrColumn;
            } else {
                lineToCompare=lineOrColumn;
                columnToCompare=column;
            }

            if (sudokuNumbers[lineToCompare][columnToCompare].possibleNumbers.size() > 0 && (lineToCompare!=line || columnToCompare!=column)) {
                uniquePossibleNumbers=checkSameNumbers(lineToCompare, columnToCompare, uniquePossibleNumbers);              // checks if this number is unique for its line or column or not
            }
        }
        return checkOnlyPossible(line, column, uniquePossibleNumbers);
    }

    public static Boolean comparePossibleWithSquare(int line, int column){
        int squareLine=calculateLineOrColumn(line);
        int squareColumn=calculateLineOrColumn(column);
        ArrayList<Integer> uniquePossibleNumbers = new ArrayList<>(sudokuNumbers[line][column].possibleNumbers);

        for (int k = squareLine - 3; k < squareLine; k++) {
            for (int l = squareColumn - 3; l < squareColumn; l++) {
                if (sudokuNumbers[k][l].possibleNumbers.size() > 0 && (line!=k || column!=l)) {
                    uniquePossibleNumbers=checkSameNumbers(k, l, uniquePossibleNumbers);
                }
            }
        }
        return checkOnlyPossible(line, column, uniquePossibleNumbers);
    }


    public static Boolean checkOnlyPossible(int line, int column, ArrayList<Integer> uniquePossibleNumbers){
        if (uniquePossibleNumbers.size()==1){                                        // if only 1 number is possible, that number will be filled in
            fillInNumber(line, column, uniquePossibleNumbers.get(0));
            return true;
        }
        return false;
    }

    public static ArrayList<Integer> checkSameNumbers(int line, int column, ArrayList<Integer> uniquePossibleNumbers){
        for (int possibleNumber=0;possibleNumber<sudokuNumbers[line][column].possibleNumbers.size();possibleNumber++){
            int possibleNumber1=sudokuNumbers[line][column].possibleNumbers.get(possibleNumber);
            if (uniquePossibleNumbers.contains(possibleNumber1)) {
                uniquePossibleNumbers.remove(Integer.valueOf(possibleNumber1));
            }
        }
        return uniquePossibleNumbers;
    }

    public static int calculateLineOrColumn(int lineOrColumn){
        if (lineOrColumn < 3) {
            return 3;
        } else if (lineOrColumn < 6) {
            return 6;
        } else if (lineOrColumn < 9) {
            return 9;
        }
        return 0;
    }

    public static void fillInNumber(int line, int column, int theNumber){
        sudokuNumbers[line][column].number=theNumber;
        sudokuNumbers[line][column].possibleNumbers.clear();
        sudokuNumbers[line][column].possibleNumbers.add(theNumber);
        solvedNumbers++;
    }

    public static void makeSudoku(String numbersString){
        int nr;
        int numberColumn=0;
        int numberLine=0;

        for (int i=0;i<numbersString.length();i++){
            nr=numbersString.charAt(i)-'0';
            if (i%9==0 && i!=0){
                numberLine++;
                numberColumn=0;
            }
            if(nr!=0){
                solvedNumbers++;
            }

            sudokuNumbers[numberLine][numberColumn]=new sudokuCell(nr);
            numberColumn++;
        }
    }

    public static void printSudoku (sudokuCell[][] sudoku) {
        String horizontalLine="-------------------";
        System.out.println("");
        System.out.println(horizontalLine);
        for (int line=0;line<9;line++){
            for (int row=0;row<9;row++) {
                if (sudoku[line][row].number == 0) {
                    System.out.print("| ");
                } else {
                    System.out.print("|" + sudoku[line][row].number);
                }
            }
            System.out.println("|");
            System.out.println(horizontalLine);
        }
    }
}

class sudokuCell{
    int number;
    ArrayList<Integer> possibleNumbers;

    public sudokuCell(int numberIN){
        number=numberIN;
        possibleNumbers=new ArrayList();
        for (int i=1;i<10;i++){
            possibleNumbers.add(i);
        }
    }
}