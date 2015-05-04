package reschu.constants;

public class MyURL {
    // final static public String URL_DOMAIN =
    // "http://www.mit.edu/~yalesong/halab/";
    // final static public String URL_DOMAIN = "http://127.0.0.1/halab/";
    final static public String URL_DOMAIN = "file:///"
	    + System.getProperty("user.dir") + "/halab/";

    final static public String LOCAL_DOMAIN = "./halab/";
    final static public String URL_PREFIX = URL_DOMAIN + "img/";
    final static public String URL_TUTORIAL = URL_DOMAIN + "tutorial/";
    final static public String URL_PAYLOAD = URL_DOMAIN + "img/payload/";
    final static public String LOCAL_PAYLOAD = LOCAL_DOMAIN + "img/payload/";
    final static public String URL_VEHICLE = URL_DOMAIN + "img/vehicle/";
    final static public String URL_PAYLOAD_INFO = URL_PAYLOAD + "info.dat";
    final static public String URL_PAYLOAD_INFO_TRAIN = URL_PAYLOAD
	    + "info_train.dat";

    final static public String URL_TASK_SEQUENCE = URL_PAYLOAD
	    + "TaskSequence.dat";
    final static public String URL_TUTORIAL_TASK = URL_PAYLOAD
	    + "TutorialTasks.dat";
    final static public String URL_HINTED_TASK = URL_PAYLOAD
	    + "HintedTasks.dat";
    final static public String URL_MAYBE_TASK_GOOD = URL_PAYLOAD
	    + "MaybeTasksGood.dat";
    final static public String URL_MAYBE_TASK_BAD = URL_PAYLOAD
	    + "MaybeTasksBad.dat";
    final static public String URL_NO_HINT_TASK = URL_PAYLOAD
	    + "NoHintTasks.dat";

    final static public String LOCAL_TASK_SEQUENCE = LOCAL_PAYLOAD
	    + "TaskSequence.dat";
    final static public String LOCAL_TUTORIAL_TASK = LOCAL_PAYLOAD
	    + "TutorialTasks.dat";
    final static public String LOCAL_HINTED_TASK = LOCAL_PAYLOAD
	    + "HintedTasks.dat";
    final static public String LOCAL_MAYBE_TASK_GOOD = LOCAL_PAYLOAD
	    + "MaybeTasksGood.dat";
    final static public String LOCAL_MAYBE_TASK_BAD = LOCAL_PAYLOAD
	    + "MaybeTasksBad.dat";
    final static public String LOCAL_NO_HINT_TASK = LOCAL_PAYLOAD
	    + "NoHintTasks.dat";

}
