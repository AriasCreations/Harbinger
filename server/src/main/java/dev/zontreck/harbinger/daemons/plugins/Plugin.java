package dev.zontreck.harbinger.daemons.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention (RetentionPolicy.CLASS)
@Target (ElementType.MODULE)
public @interface Plugin {
	String name ( );
}
