package reschu.game.controller;

import java.util.ArrayList;

import reschu.game.algorithm.CollisionZone;
import reschu.game.model.Payload;
import reschu.game.model.Vehicle;

public interface GUI_Listener {

    /** Creates a new instance of Gui_Listener */
    public void vehicle_location_changed();

    public void Clock_Tick(int milliseconds);

    public void Game_Start();

    public void Game_End();

    // Events from pnlPayloadControl
    public void Pan_Up_Selected();

    public void Pan_Down_Selected();

    public void Rotate_Clockwise_Selected();

    public void Rotate_Counter_Selected();

    public void Zoom_In();

    public void Zoom_Out();

    public void showMessageOnTopOfMap(String msg, int duration);

    // Events From pnlPayload
    public void Payload_Finished_From_pnlPayload(Vehicle v);

    public void Payload_Assigned_From_pnlPayload(Vehicle v, Payload p);

    public void Payload_Graphics_Update(); // For T3

    public void Payload_Submit(boolean submit); // For T3

    public void HitPayload(); // For T3

    public void CheckPayload();

    public void SafePayload();

    // Events From Vehicle
    // public void Vehicle_Reached_Target_From_Vehicle(Vehicle v);
    public void Vehicle_Damaged_By_Hazard_Area_From_Vehicle(Vehicle v);

    public void Target_Become_Visible_From_Vehicle(Vehicle v);

    public void Hide_Popup(Vehicle v);

    // Events From pnlMap
    public void Vehicle_Selected_From_pnlMap(int idx);

    public void Vehicle_Engage_From_pnlMap(Vehicle v);

    public void Vehicle_Unselected_From_pnlMap();

    public void Automation_Applied_From_pnlMap(Integer[] vIds);

    // Events From pnlControl
    public void Vehicle_Selected_From_pnlControl(int idx);

    public void Vehicle_Unselected_From_pnlControl();

    public void Vehicle_Goal_From_pnlControl(Vehicle v);

    public void Vehicle_WP_Add_From_pnlControl(Vehicle v);

    public void Vehicle_WP_Del_From_pnlControl(Vehicle v);

    public void Vehicle_Engage_From_pnlControl(Vehicle v);

    public void Automation_Applied_From_pnlControl(Integer[] vIds);

    // Events From Timeline
    public void Vehicle_Selected_From_pnlTimeLine(int idx);

    // Database
    public void EVT_WP_AddWP_Start(int vIdx);

    public void EVT_WP_AddWP_End(int vIdx, int mouseCoordX, int mouseCoordY);

    public void EVT_WP_MoveWP_Start(int vIdx, int mouseCoordX, int mouseCoordY);

    public void EVT_WP_MoveWP_End(int vIdx, int mouseCoordX, int mouseCoordY);

    public void EVT_WP_DeleteWP_Start(int vIdx);

    public void EVT_WP_DeleteWP_End(int vIdx, int mouseCoordX, int mouseCoordY);

    public void EVT_WP_AddWP_Cancel(int vIdx);

    public void EVT_GP_SetGP_by_System(int vIdx, String targetName);

    public void EVT_GP_SetGP_Start(int vIdx);

    public void EVT_GP_SetGP_End_Assigned(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName);

    public void EVT_GP_SetGP_End_Unassigned(int vIdx, int mouseCoordX,
	    int mouseCoordY);

    public void EVT_GP_ChangeGP_Start(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName);

    public void EVT_GP_ChangeGP_End_Assigned(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName);

    public void EVT_GP_ChangeGP_End_Unassigned(int vIdx, int mouseCoordX,
	    int mouseCoordY);

    public void EVT_Target_Generated(String targetName, int[] targetPos,
	    boolean visibility);

    public void EVT_Target_BecameVisible(String targetName, int[] targetPos);

    public void EVT_Target_Disappeared(String targetName, int[] targetPos);

    public void EVT_Payload_EngagedAndFinished_COMM(int vIdx, String targetName);

    public void EVT_Payload_Engaged(int vIdx, String targetName,
	    String description);

    public void EVT_Payload_Finished_Correct(int vIdx, String targetName);

    public void EVT_Payload_Finished_Incorrect(int vIdx, String targetName);

    public void EVT_Vehicle_Damaged(int vIdx, int haX, int haY); // Not gonna
								 // use this.

    public void EVT_Vehicle_SpeedDecreased(int vIdx, int curSpeed);

    public void EVT_Vehicle_ArrivesToTarget(int vIdx, String targetname, int x,
	    int y);

    public void EVT_Vehicle_IntersectHazardArea(int vIdx, int[] threat);

    public void EVT_Vehicle_EscapeHazardArea(int vIdx);

    public void EVT_HazardArea_Generated(int[] pos);

    public void EVT_HazardArea_Disappeared(int[] pos);

    public void EVT_System_GameStart();

    public void EVT_System_GameEnd(int workload, int automation, int correct,
	    int incorrect, double vvDamage, double vhDamage);

    /**
     * For Yves
     */
    public void EVT_VSelect_Map_LBtn(int vIdx);

    public void EVT_VSelect_Map_RBtn(int vIdx);

    public void EVT_VSelect_Tab(int vIdx);

    public void EVT_VSelect_Tab_All();

    public void Update_Collision_Info(ArrayList<CollisionZone> cz);

    // Newly added
    public void Automation_Application_Failed(Integer[] vIds);

    public void Automation_Application_Canceled(Integer[] vIds);

    public void Event_Procedure_Switch(String from, String to);

    public void UpdateMapLogData(String mapData);

    public void HideSystemWindow();

    public void TutorialAddVehicle();

    public void CollisionZoneDecreased(int currentNumber);

    public void CollisionZoneIncreased(int currentNumber);

    public void UpdateFakeCollisionZoneListAuto(Integer[] vIds);

    public void UpdateFakeCollisionZoneListEnter();

    public void UpdateFakeCollisionZoneListTrack(Integer vId);

    public boolean IsFakedCollisionZonePair(Integer vId1, Integer vId2);
    
    public void Update_Hazard_Highlight_Info(String highlightList);
}
