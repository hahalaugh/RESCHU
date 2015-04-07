package reschu.constants;

public class MyURL {
    // final static public String URL_DOMAIN =
    // "http://www.mit.edu/~yalesong/halab/";
    // final static public String URL_DOMAIN = "http://127.0.0.1/halab/";
    final static public String URL_DOMAIN = "file:///"
	    + System.getProperty("user.dir") + "/halab/";
    final static public String URL_PREFIX = URL_DOMAIN + "img/";
    final static public String URL_PAYLOAD = URL_DOMAIN + "img/payload/";
    final static public String URL_VEHICLE = URL_DOMAIN + "img/vehicle/";
    final static public String URL_PAYLOAD_INFO = URL_PAYLOAD + "info.dat";
    final static public String URL_PAYLOAD_INFO_TRAIN = URL_PAYLOAD
	    + "info_train.dat";

    final static public String URL_TASK_SEQUENCE = URL_PAYLOAD
	    + "TaskSequence.dat";
    final static public String URL_HINTED_TASK = URL_PAYLOAD
	    + "HintedTasks.dat";
    final static public String URL_MAYBE_TASK_GOOD = URL_PAYLOAD
	    + "MaybeTasksGood.dat";
    final static public String URL_MAYBE_TASK_BAD = URL_PAYLOAD
	    + "MaybeTasksBad.dat";
    final static public String URL_NO_HINT_TASK = URL_PAYLOAD
	    + "NoHintTasks.dat";

}
