package ru.rsatu.seryakova;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class AppActive extends Application {
    @Override
    public Set<Class<?>> getClasses(){
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        set.add(MainClass.class);
        set.add(Authentification.class);
        set.add(ChangeRasp.class);
        return set;
    }
}
