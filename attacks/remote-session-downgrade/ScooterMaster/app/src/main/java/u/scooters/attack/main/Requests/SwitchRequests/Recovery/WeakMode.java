package u.scooters.attack.main.Requests.SwitchRequests.Recovery;

import java.util.Arrays;

import u.scooters.attack.main.IRequest;
import u.scooters.attack.main.RequestType;
import u.scooters.attack.util.Commands;
import u.scooters.attack.util.LegacyPacketBuilder;

public class WeakMode implements IRequest {
    private static int delay = 100;
    private final String requestBit = "7B";
    private final RequestType requestType = RequestType.NOCOUNT;
    private long startTime;

    public WeakMode() {
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public String getRequestString() {
        return new LegacyPacketBuilder()
                .setDirection(Commands.MASTER_TO_M365)
                .setRW(Commands.WRITE)
                .setPosition(0x7B)
                .setPayload(0x0000)
                .build();
    }

    @Override
    public String getRequestBit() {
        return requestBit;
    }

    @Override
    public String handleResponse(String[] request) {
        return Arrays.toString(request);
    }

    @Override
    public RequestType getRequestType() {
        return requestType;
    }
}
