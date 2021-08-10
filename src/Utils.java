class Utils {
    /**
     * Move represents one move of a checker.
     */
    class Move {
        private int startPoint;
        private int endPoint;

        public Move(int startPoint, int endPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        public int getStartPoint() {
            return startPoint;
        }

        public int getEndPoint() {
            return endPoint;
        }

    }
}
