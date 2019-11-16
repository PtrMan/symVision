package ptrman.FargGeneral;

public abstract class Codelet {
    public static class RunResult {
        public boolean putback;
        
        public RunResult(boolean putback) {
            this.putback = putback;
        }
    }
    
    public abstract RunResult run();
}
