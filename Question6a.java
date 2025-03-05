class NumberPrinter {
    public void printNumber(int number) {
        System.out.print(number);
    }
}
class ThreadController {
    private final NumberPrinter numberPrinter;
    private int count = 0; 

    public ThreadController() {
        this.numberPrinter = new NumberPrinter();
    }
    public void printNumbers(int n) {
        for (int i = 0; i <= n; i++) {
            if (i == 0) {
                numberPrinter.printNumber(0); 
            } else if (i % 2 == 1) {
                numberPrinter.printNumber(i); 
            } else {
                numberPrinter.printNumber(i); 
            }
        }
    }
}
public class Question6a {
    public static void main(String[] args) {
        int n = 5; 
        ThreadController controller = new ThreadController();
        controller.printNumbers(n);
    }
}
