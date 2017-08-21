package com.elvaco.mvp.meteringpoint;

import com.elvaco.mvp.user.User;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author petjan
 */
@RestController
public class MeteringPointRestController {

    private final MeteringPointRepository repository;

    @Autowired
    MeteringPointRestController(MeteringPointRepository repository) {
        this.repository = repository;
    }

    /**
     * Get metering point object from MOID.
     *
     * @param moid the MOID
     * @return the metering point object if it exists
     */
    @RequestMapping("/mp")
    public MeteringPoint meteringPoint(@RequestParam(value = "moid", defaultValue = "0") String moid) {
        return repository.findByMoid(moid);
    }

    /**
     * Get a list of all users in system.
     * <p>
     * TODO : Do we need this endpoint when going to production?
     * </p>
     *
     * @return a list of all defined users.
     */
    @RequestMapping("/mps")
    public Collection<MeteringPoint> meteringPoints() {
        return repository.findAll();
    }

}
