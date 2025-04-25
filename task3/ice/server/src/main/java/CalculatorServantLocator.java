package main.java;

import com.zeroc.Ice.*;
import com.zeroc.Ice.Object;

public class CalculatorServantLocator implements ServantLocator {
    private final String category;

    public CalculatorServantLocator(String category) {
        this.category = category;
    }

    @Override
    public LocateResult locate(Current current) {
        PlaceDistanceCalculatorI servant = new PlaceDistanceCalculatorI(current.id);
        current.adapter.add(servant, current.id);

        System.out.printf("[Locator:%s] Creating new servant instance %s for Identity '%s/%s'%n",
                category, servant.getClass().getSimpleName() + "@" + Integer.toHexString(servant.hashCode()),
                current.id.category, current.id.name);

        return new LocateResult(servant, null);
    }

    @Override
    public void finished(Current current, Object servant, java.lang.Object cookie) {
        PlaceDistanceCalculatorI calcServant = (PlaceDistanceCalculatorI) servant;
        System.out.printf("[Locator:%s] finished() called for Identity '%s/%s' on servant %s%n",
                category, current.id.category, current.id.name,
                servant.getClass().getSimpleName() + "@" + Integer.toHexString(servant.hashCode()));
    }

    @Override
    public void deactivate(String category) {
        System.out.printf("[Locator:%s] deactivate() called for category '%s'. Cleaning up.%n", this.category, category);
    }
}
