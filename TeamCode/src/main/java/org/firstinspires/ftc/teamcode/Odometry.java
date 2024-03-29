package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.Range;

import java.util.ArrayList;
import java.util.List;

public class Odometry {

    public void Odometry(){

    }

    public double[] nowPos(double[] prevPos, double dR, double dL, double dS) {
        final double D = 34.5;
        double dC = (dR + dL) / 2;
        double deltaAngle = (dR - dL) / D;
        double alpha = deltaAngle/2;
        double rC = (dL / deltaAngle) + (D / 2);
        double h = Math.sqrt(2*Math.pow(rC, 2) - 2*Math.pow(rC,2)*Math.cos(deltaAngle));
        if(deltaAngle == 0) {
            h = dC;
        }

        double deltaX = (dC * Math.cos(alpha + prevPos[2])) + (dS * Math.sin(alpha + prevPos[2]));
        double deltaY = (dC * Math.sin(alpha + prevPos[2])) + (dS * Math.cos(alpha + prevPos[2]));

        double[] newPos = {prevPos[0] + deltaX, prevPos[1] + deltaY, prevPos[2] + deltaAngle};

        return newPos;
    }

    int cooldown = 0;
    boolean finalStage = false;
    double firstATT = 0;
    double firstHF = 0;
    double firstFT = 0;
    public double[] calc(double targetX, double targetY, double targetAngle, double nowX, double nowY, double nowAngle) {

        //TURN TO FACE TARGET, THEN DRIVE TO TARGET

        double[] power = {0, 0, 0, 0};

        double dX = targetX-nowX;
        double dY = targetY-nowY;
        double howFar = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY, 2));

        //targetAngle = targetAngle * Math.PI / 180;
        //nowAngle = nowAngle * Math.PI / 180;

        double angleToTarget = Math.atan(dY / dX);
        if(dX < 0) {
            angleToTarget += Math.PI;
        }
        double turn = nowAngle-angleToTarget;
        if(Math.abs(turn) >= (Math.PI/40) && cooldown == 0 && howFar >= 10) {
            if(firstATT == 0){
                firstATT = turn;
            }
            double progress = turn/firstATT;
            double p = Range.clip(full(progress, 0, 1, 1, 0), 0.2, 0.6);
            if(turn > 0) {
                return new double[]{p, p, -p, -p};
            }
            if(turn < 0){
                return new double[]{-p, -p, p, p};
            }
        } else if(howFar > 3 && Math.abs(turn) < (Math.PI/36)) { //tweak howFar
            cooldown += 1;
            if(cooldown == 100) {
                cooldown = 0;
            }
            if(firstHF == 0){
                firstHF = howFar;
            }
            double progress = howFar/firstHF;
            double p = Range.clip(full(progress, 0, 1, 1, 0.2), 0.2, 0.8);
            //double k = easy(turn/(Math.PI/20), 0, 1, 1)/10*p;
            double k = 0;
            if(turn > 0) {
                return new double[]{p, p, p-k, p-k};
            }
            if(turn < 0){
                return new double[]{p-k, p-k, p, p};
            }
            if(turn == 0){
                return new double[]{p, p, p, p};
            }
        } else {
            finalStage = true;
            double finalTurn = nowAngle - targetAngle;
            if (firstFT == 0){
                firstFT = finalTurn;
            }
            double progress = finalTurn/firstFT;
            double p;
            if (Math.abs(finalTurn) > Math.PI/180){
                p = Range.clip(full(progress, 0, 1, 1, 0), 0.15, 0.6);
            } else {
                p = Range.clip(full(progress, 0, 1, 1, 0), 0.08, 0.2);
                finalStage = false;
                firstATT = 0;
                firstHF = 0;
                firstFT = 0;
                cooldown = 0;
            }
            if(finalTurn > 0) {
                return new double[]{p, p, -p, -p};
            }
            if(finalTurn < 0){
                return new double[]{-p, -p, p, p};
            }
        }
        return power;
    }

    public boolean distanceCheck(double nowX, double nowY, double nowAngle, double targetX, double targetY, double targetAngle){
        boolean nextTarget = false;
        if((nowX - targetX)*(nowX - targetX)+(nowY - targetY)*(nowY - targetY) < 25){
            nextTarget = true;
        }
        return nextTarget;
    }

    public double[] calcM(double targetX, double targetY, double targetAngle, double nowX, double nowY, double nowAngle) {

        //DRIVE TO ANY POINT IN STRAIGHT LINE WITHOUT TURNING

        double dX = targetX-nowX;
        double dY = targetY-nowY;

        //targetAngle = targetAngle * Math.PI / 180;
        //nowAngle = nowAngle * Math.PI / 180;

        double angleToTarget = 0;
        if(dX != 0) {
            angleToTarget = Math.atan(dY / dX);
        }
        double turn = angleToTarget-nowAngle;

        if(dX > 0 || (dX == 0 && dY > 0)) {
            turn += Math.PI / 2;
        } else {
            turn += 3 * Math.PI / 2;
        }
        //positive x axis same as 1st quad.
        //1st quadrant -> +90°
        //positive y axis same as 1st quad.
        //2nd quadrant -> +270° (-90°)
        //negative x axis same as 3rd quad.
        //3rd quadrant -> +270° (-90°)
        //negative y axis same as 3rd quad.
        //4th quadrant -> +90°

        double x = Math.cos(turn);
        double y = Math.sin(turn);

        double iPower = y - x;
        double kPower = y + x;
        // Send calculated power to wheels

        return new double[]{iPower, kPower, kPower, iPower};
    }

    public double[] calcS(double targetX, double targetY, double nextX, double nextY, double nowX, double nowY, double nowAngle) { //Calc when using spline points

        //DRIVE IN A SPLINE THROUGH MULTIPLE POINTS ALWAYS FACING FORWARD
        double dX = targetX - nowX;
        double dY = targetY - nowY;
        double nextDX = nextX - nowX;
        double nextDY = nextY - nowY;

        //distance to points
        double toTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY, 2));
        double toNext = Math.sqrt(Math.pow(nextDX, 2)+Math.pow(nextDY, 2));

        double difference = toNext - toTarget;

        double angleToTarget = Math.atan(dY / dX);
        double turn = nowAngle-angleToTarget;

        double speedCoef = 50;
        double p = Range.clip(difference/speedCoef,-1,1);
        double k = Range.clip(Math.pow(15*turn, 2), 0, 0.1);

        if(turn > 0) {
            return new double[]{p, p, p-k, p-k};
        }
        if(turn < 0){
            return new double[]{p-k, p-k, p, p};
        }
        if(turn == 0) {
            return  new double[]{p,p,p,p};
        }
        return new double[]{p, p, p, p};
    }

    public boolean perpendicularLineCheck(double nowX, double nowY, double targetX, double targetY, double prevX, double prevY){
        boolean nextTarget = false;
        double m = (targetY - prevY) / (targetX - prevX);

        if((nowY - targetY) > -(1/m)*(nowX-targetX)){
            nextTarget = true;
        }
        return nextTarget;
    }

    public double[][] splinePath(double[][] points){

        List<double[]> path = new ArrayList<>();
        for(int i=0; i<points.length; i++) {
            if(i < points.length-1) {
                for(int k=0; k < 50; k++) {
                    double[] p = getSplinePoint(k/50, i, points);
                    path.add(p);
                }
            }
        }
        return path.toArray(new double[0][]);
    }

    public double[] getSplinePoint(int t, int i, double[][] points){
        int p0, p1, p2, p3;
        p0 = i-1;
        p1 = i;
        p2 = i+1;
        p3 = i+2;

        if(i == 0) {
            p0 = i;
            p1 = i;
            p2 = i+1;
            p3 = i+1;
        }
        if(i == points.length-2) {
            p0 = i-1;
            p1 = i;
            p2 = i+1;
            p3 = i+1;
        }

        double tt = t * t;
        double ttt = tt * t;

        double q1 = -ttt + 2*tt - t;
        double q2 = 3*ttt - 5*tt +2;
        double q3 = -3*ttt + 4*tt + t;
        double q4 = ttt - tt;

        double tx = 0.5 * (points[p0][0] * q1 + points[p1][0] * q2 + points[p2][0] * q3 + points[p3][0] * q4);
        double ty = 0.5 * (points[p0][1] * q1 + points[p1][1] * q2 + points[p2][1] * q3 + points[p3][1] * q4);

        double[] path = {tx, ty};
        return path;
    }

    double full(double t,double b,double c,double d, double floor) {
        if(t <= 0.75) {
            return easy(t*(1+(1/3)),b,c,d)*(1-floor) + floor;
        } else {
            return (1-floor) - easy(t*4-2.9,b,c,d)*(1-floor) + floor;
        }
    }

    double easy(double t, double b, double c, double d) {
        t /= d/2;
        if (t < 1) {
            return c / 2 * t * t + b;
        }
        t--;
        return -c/2 * (t*(t-2) - 1) + b;
    }
}
