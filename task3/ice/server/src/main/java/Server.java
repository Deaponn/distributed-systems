package main.java;
import com.zeroc.Ice.*;
import com.zeroc.Ice.Exception;

public class Server {

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "config.server")) {
            String endpoint = communicator
                    .getProperties()
                    .getPropertyWithDefault("CalculatorAdapter.Endpoints", "default -p 10000");
            System.out.println("Adapter Endpoints configuration: " + endpoint);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CalculatorAdapter", endpoint);

            DistanceCalculatorI sharedServant = new DistanceCalculatorI();
            adapter.addDefaultServant(sharedServant, "SharedCalc");

            String placeCalcCategory = "PlaceCalc";
            CalculatorServantLocator locator = new CalculatorServantLocator(placeCalcCategory);
            adapter.addServantLocator(locator, placeCalcCategory);

            adapter.activate();

            System.out.println("Server started successfully. Waiting for requests...");

            communicator.waitForShutdown();

        } catch (Exception e) {
            System.out.println("Server encountered an error" + e);
            System.exit(1);
        }
    }
}
