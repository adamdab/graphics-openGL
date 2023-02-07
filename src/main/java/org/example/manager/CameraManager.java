package org.example.manager;

import org.example.camera.Camera;
import org.example.camera.CameraMovementType;
import static org.example.camera.CameraMovementType.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;
public class CameraManager {
    private final Camera camera = new Camera();
    private CameraMovementType cameraMovementType = TPV;

    public void changeCameraMovementType(CameraMovementType type, long window) {
        cameraMovementType = type;
        switch(type) {
            case STATIC :
                camera.stopProcessingMouseMovement(window);
                camera.setCameraPos(new Vector3f(-1.9470121f, 7.8995733f, 25.64252f));
                camera.setCameraFront( new Vector3f(0.110537454f, -0.3583683f, -0.9270134f).normalize());
                break;
            case TPV :
                camera.stopProcessingMouseMovement(window);
                break;
            case FPV :
                camera.stopProcessingMouseMovement(window);
                break;
        }
    }

    public void moveCamera(Vector3f spaceShipPosition, Vector2f nextShipCameraFront, float dt, long window) {
        switch(cameraMovementType) {
            case FPV:
                camera.setCameraPos(new Vector3f(0f,0.3f,0f).add(spaceShipPosition));
                camera.setCameraFront(new Vector3f(nextShipCameraFront.x, 0f , nextShipCameraFront.y));
                break;
            case TPV:
                camera.setCameraPos(new Vector3f(0f,0.5f,0f)
                        .add(new Vector3f(-nextShipCameraFront.x * 3, 0f , -nextShipCameraFront.y * 3))
                        .add(spaceShipPosition));
                camera.setCameraFront(new Vector3f(nextShipCameraFront.x, 0f , nextShipCameraFront.y));
                break;
            case FREE:
                camera.processInput(window, dt);
                camera.startProcessingMouseMovement(window);
                break;
        }
    }

    public Matrix4f getView() {
        return camera.getViewMatrix();
    }

    public float getFov() {
        return camera.getFov();
    }

    public Vector3f getPosition() {
        return camera.getCameraPos();
    }

}
