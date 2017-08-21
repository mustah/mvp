package com.elvaco.mvp.meteringpoint;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MeteringPointDatabaseLoader implements CommandLineRunner {

    private final MeteringPointRepository repository;

    @Autowired
    public MeteringPointDatabaseLoader(MeteringPointRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<MeteringPoint> mps = new ArrayList<>();
        mps.add(new MeteringPoint("1"));
        mps.add(new MeteringPoint("2"));
        mps.add(new MeteringPoint("3"));
        mps.add(new MeteringPoint("4"));
        mps.add(new MeteringPoint("5"));
        mps.add(new MeteringPoint("6"));
        mps.add(new MeteringPoint("7"));
        mps.add(new MeteringPoint("8"));
        mps.add(new MeteringPoint("9"));
        mps.add(new MeteringPoint("10"));

        mps.stream().forEach((mp) -> {
            switch (mp.getMoid()) {
                case "3":
                    mp.setStatus(200);
                    mp.setMessage("Low battery.");
                    mp.setLatitude(57.505267);
                    mp.setLongitude(12.069423);
                    break;
                case "5":
                    mp.setStatus(300);
                    mp.setMessage("Failed to read meter.");
                    mp.setLatitude(57.49893);
                    mp.setLongitude(12.071531);
                    break;
                default:
                    mp.setStatus(0); // Not really needed since default is 0.
                    mp.setMessage("");
                    mp.setLatitude(57.505267);
                    mp.setLongitude(12.069423);
            }
            repository.save(mp);
        });

    }

}
