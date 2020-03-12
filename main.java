import java.io.*;
import java.util.Scanner;
import java.lang.*;
public class main {
    public static void main(String[] args) throws FileNotFoundException{
        File file = new File(args[0]);
        Scanner input = new Scanner(file);
        PrintWriter robertOut = new PrintWriter(args[1]);
        PrintWriter sobelOut = new PrintWriter(args[2]);
        PrintWriter gradOut = new PrintWriter(args[3]);
        PrintWriter prettyOut = new PrintWriter(args[4]);
        PrintWriter debugOut = new PrintWriter(args[5]);
        // step 0
        int row = input.nextInt();
        int col = input.nextInt();
        int min = input.nextInt();
        int max = input.nextInt();
        image data = new image(row,col,min,max);
        // step 1
        data.loadImage(input);
        //step 2
        data.frameMirror();
        //step 3-4
        for(int i = 1; i < data.numRows+1; i++){
            for(int j = 1; j < data.numCols+1; j++){
                data.robertRight[i][j] = Math.abs(data.convoRobert(i,j, data.maskRobertRight));
                data.robertLeft[i][j] = Math.abs( data.convoRobert(i,j, data.maskRobertLeft) );

                data.sobelRight[i][j] = Math.abs( data.convoSobel(i,j, data.maskSobelRight) );
                data.sobelLeft[i][j] = Math.abs( data.convoSobel(i,j, data.maskSobelLeft) );

                data.gradEdge[i][j] = ( data.computeGrad(i,j));
            }
        }
        //step 5
        data.addTwoArrays(data.robertRight, data.robertLeft, data.edgeSum);
        // output robertRight to debug with caption
        debugOut.println("/////RobertRightDiagonal");
        data.printArray(data.robertRight, debugOut);
        debugOut.println("/////RobertLeftDiagonal");
        data.printArray(data.robertLeft, debugOut);
        robertOut.println(data.numRows+" "+data.numCols+" "+data.newMin+" "+data.newMax);
        data.printArray2(data.edgeSum, robertOut);

        //step 6
        data.addTwoArrays(data.sobelLeft, data.sobelRight, data.edgeSum);
        // output sobel to debug
        debugOut.println("/////SobelRightDiagonal");
        data.printArray(data.sobelRight, debugOut);
        debugOut.println("/////SobelLeftDiagonal");
        data.printArray(data.sobelLeft, debugOut);
        sobelOut.println(data.numRows+" "+data.numCols+" "+data.newMin+" "+data.newMax);
        data.printArray2(data.edgeSum, sobelOut);

        //step 7
        gradOut.println(data.numRows+" "+data.numCols+" "+ data.gradMin +" "+ data.gradMax );
        data.printArray2(data.gradEdge, gradOut);
        //step 8
        input.close();
        robertOut.close();
        sobelOut.close();
        gradOut.close();
        prettyOut.close();
        debugOut.close();
    }
}

class image{
    int numRows;
    int numCols;
    int minVal;
    int maxVal;
    int newMin;
    int newMax;
    int gradMin = Integer.MAX_VALUE;
    int gradMax = Integer.MIN_VALUE;
    int[][] mirrorFramed;
    int[][] maskRobertRight;
    int[][] maskRobertLeft;
    int[][] maskSobelRight;
    int[][] maskSobelLeft;

    int[][] robertRight;
    int[][] robertLeft;
    int[][] sobelRight;
    int[][] sobelLeft;
    int[][] gradEdge;
    int[][] edgeSum;
    public image(int row,int col, int min, int max){
        numRows = row;
        numCols = col;
        minVal = min;
        maxVal = max;
        mirrorFramed = new int[row+2][col+2];
        maskRobertLeft = new int[][]{{-1,1},
                                     {1,-1}};
        maskRobertRight = new int[][]{{1,-1},
                                      {-1,1}};
        maskSobelLeft = new int[][]{
                {2,1,0},
                {1,0,-1},
                {0,-1,-2}
        };
        maskSobelRight = new int[][]{
                {0,1,2},
                {-1,0,1},
                {-2,-1,0},
        };

        robertRight = new int[numRows+2][numCols+2];
        robertLeft = new int[numRows+2][numCols+2];
        sobelRight = new int[numRows+2][numCols+2];
        sobelLeft = new int[numRows+2][numCols+2];
        gradEdge = new int[numRows+2][numCols+2];
        edgeSum = new int[numRows+2][numCols+2];
    }
    void loadImage(Scanner input){
        for(int i = 1; i < numRows+1; i++){
            for(int j =1; j < numCols+1; j++){
                int t = input.nextInt();
                mirrorFramed[i][j] = t;
            }
        }
    }
    void frameMirror(){
        for(int i = 0; i < numRows+2; i++){
            mirrorFramed[i][0] = mirrorFramed[i][1];
            mirrorFramed[i][numCols+1] = mirrorFramed[i][numCols];
        }
        for(int i = 0; i < numCols+2; i++){
            mirrorFramed[0][i] = mirrorFramed[1][i];
            mirrorFramed[numRows+1][i] = mirrorFramed[numRows][i];
        }
    }
    int convoRobert(int i, int j, int[][] mask){
        int sum = 0;
        for(int a = 0; a < 2; a++){
            for(int b = 0; b < 2; b++){
                sum += mirrorFramed[i+a][j+b] * mask[a][b];
            }
        }
        return sum;
    }
    int convoSobel(int i, int j, int[][] mask){
        int sum = 0;
        int r = i-1;
        int c = j-1;
        for(int a = 0; a < 3; a++){
            for(int b = 0; b < 3; b++){
                if(mask[a][b] == 0)
                    continue;
                sum += mirrorFramed[r+a][c+b] * mask[a][b];
            }
        }
        return sum;
    }
    int computeGrad(int i, int j){
        double sum = 0;
        sum += Math.sqrt(  Math.pow(  (double) (mirrorFramed[i][j] - mirrorFramed[i+1][j]) ,2) + Math.pow( (double) (mirrorFramed[i][j] - mirrorFramed[i][j+1]) ,2)      );
        int temp = (int) sum;
        if(temp < gradMin)
            gradMin = temp;
        if(temp > gradMax)
            gradMax =  temp;
        return (int) sum;
    }
    void addTwoArrays(int[][] arr1, int[][] arr2, int[][] arr3){
        newMin = Integer.MAX_VALUE;
        newMax = Integer.MIN_VALUE;
        for(int i = 1; i < numRows+1; i++){
            for(int j = 1; j < numCols+1; j++){
                arr3[i][j] = arr1[i][j] + arr2[i][j];
                if(arr3[i][j] > newMax)
                    newMax = arr3[i][j];
                if(arr3[i][j] < newMin)
                    newMin = arr3[i][j];
            }
        }

    }
    void printArray(int[][] input, PrintWriter debug){
        for(int i = 0; i < numRows+2; i++){
            for(int j = 0; j < numCols+2; j++){
                debug.print( input[i][j]+" " );
            }
            debug.println();
            debug.flush();
        }
    }
    void printArray2(int[][] input, PrintWriter debug){
        for(int i = 1; i < numRows+1; i++){
            for(int j = 1; j < numCols+1; j++){
                debug.print( input[i][j]+" " );
            }
            debug.println();
            debug.flush();
        }
    }
}
