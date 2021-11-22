package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="Basic Drive Mode", group="Linear Opmode")
public class BasicDriveMode extends LinearOpMode {
    HardwareRobot robot = new HardwareRobot();
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode(){
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        robot.init(hardwareMap);
        waitForStart();
        runtime.reset();

        while(opModeIsActive()){
            double x = gamepad1.left_stick_x;
            double y = gamepad1.left_stick_y;

            double speedCoe = Range.clip(1 - gamepad1.left_trigger, 0.3, 1.0);

            double frontBack = Range.clip((y - x) * speedCoe, -1.0, 1.0);
            double backFront = Range.clip((y + x) * speedCoe, -1.0, 1.0);

            robot.leftFront.setPower(frontBack);
            robot.rightBack.setPower(frontBack);
            robot.rightFront.setPower(backFront);
            robot.leftBack.setPower(backFront);
        }
    }
}
