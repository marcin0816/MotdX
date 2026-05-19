package dc.marcin0816.motdX.model;

public record MotdFrame(
        String line1,
        String line2,
        Integer playersOnline,
        Integer playersMax
) {}
