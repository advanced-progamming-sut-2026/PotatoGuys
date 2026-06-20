package Models.DataTypes;

public class Vector2 {
    public double x;
    public double y;

    public Vector2(double x, double y){
        this.x=x;
        this.y=y;
    }

    public double magnitude(){
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
    }

    public static double calculateDistance(Vector2 v1, Vector2 v2){
        return Math.sqrt(Math.pow(v2.x-v1.x,2)+Math.pow(v2.y-v1.x,2));
    }
}
