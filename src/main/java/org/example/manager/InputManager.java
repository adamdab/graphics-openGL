package org.example.manager;

import org.example.window.Window;
import org.example.manager.Action;
import org.example.camera.CameraMovementType;
import org.example.camera.CameraMovementType.*;
import org.example.shader.ShaderType;
import org.example.shader.ShaderType.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
public class InputManager {
    private Window window;
    private LightManager lightManager;
    private CameraManager cameraManager;

    public InputManager(Window window, CameraManager cameraManager, LightManager lightManager) {
        this.cameraManager = cameraManager;
        this.lightManager = lightManager;
        this.window = window;
    }

    private Action getAction() {
        Action action = Action.OTHER;
        // camera actions
        if(window.checkIfPressed(GLFW_KEY_1))
            action = Action.CAM_STATIC;
        else if(window.checkIfPressed(GLFW_KEY_2))
            action = Action.CAM_TPV;
        else if(window.checkIfPressed(GLFW_KEY_3))
            action = Action.CAM_FPV;
        else if(window.checkIfPressed(GLFW_KEY_4))
            action = Action.CAM_FREE;
        else if(window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_UP))
            action = Action.FOG_IN;
        else if(window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_DOWN))
            action = Action.FOG_DE;
        else if(window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_UP))
            action = Action.LIGHT_IN;
        else if(window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_DOWN))
            action = Action.LIGHT_DE;
        else if(window.checkIfPressed(GLFW_KEY_UP))
            action = Action.LIGHT_UP;
        else if(window.checkIfPressed(GLFW_KEY_DOWN))
            action = Action.LIGHT_LOW;
        else if(window.checkIfPressed(GLFW_KEY_P))
            action = Action.SHADER_P;
        else if(window.checkIfPressed(GLFW_KEY_G))
            action = Action.SHADER_G;
        else if(window.checkIfPressed(GLFW_KEY_F))
            action = Action.SHADER_F;

        if (window.checkIfPressed(GLFW_KEY_ESCAPE))
            window.exit();

        return action;
    }

    private void execute(Action action, float deltaTime) {
        switch(action) {
            case CAM_STATIC:
                cameraManager.changeCameraMovementType(CameraMovementType.STATIC, window.getHandler());
                break;
            case CAM_TPV:
                cameraManager.changeCameraMovementType(CameraMovementType.TPV, window.getHandler());
                break;
            case CAM_FPV:
                cameraManager.changeCameraMovementType(CameraMovementType.FPV, window.getHandler());
                break;
            case CAM_FREE:
                cameraManager.changeCameraMovementType(CameraMovementType.FREE, window.getHandler());
                break;
            case FOG_IN:
                lightManager.increaseFog(deltaTime);
                break;
            case FOG_DE:
                lightManager.decreaseFog(deltaTime);
                break;
            case LIGHT_IN:
                lightManager.increaseLightIntensity(deltaTime);
                break;
            case LIGHT_DE:
                lightManager.decreaseLightIntensity(deltaTime);
                break;
            case LIGHT_UP:
                lightManager.liftSpotLights(deltaTime);
                break;
            case LIGHT_LOW:
                lightManager.lowerSpotLights(deltaTime);
                break;
            case SHADER_P:
                window.changeCurrentShader(ShaderType.PHONG);
                break;
            case SHADER_G:
                window.changeCurrentShader(ShaderType.GOURAUD);
                break;
            case SHADER_F:
                window.changeCurrentShader(ShaderType.FLAT);
                break;
        }
    }

    public void processInput(float dt) {
        Action action = getAction();
        execute(action, dt);
    }
}
