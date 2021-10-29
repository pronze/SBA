package io.github.pronze.sba.wrapper;

import io.github.pronze.sba.wrapper.event.BedWarsEventWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service(initAnother = {
        BedWarsEventWrapper.class
})
public class BedWarsAPIWrapper {

}
